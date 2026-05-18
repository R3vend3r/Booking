import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../../services/booking.service';
import { WorkPlace, Location, AdditionalService } from '../../models/booking.model';
import { AuthService } from '../../services/auth.service';

@Component({
  standalone: false,
  selector: 'app-workplaces',
  templateUrl: './workplaces.component.html',
  styleUrls: ['./workplaces.component.css']
})
export class WorkplacesComponent implements OnInit {
  locationId: string | null = null;
  location: Location | null = null;
  workplaces: WorkPlace[] = [];
  services: AdditionalService[] = [];
  selectedWorkplace: WorkPlace | null = null;
  selectedServices = new Set<string>();

  bookingStart = '';
  bookingEnd = '';
  filterStart = '';
  filterEnd = '';
  filterActive = false;
  error = '';
  bookingError = '';
  loading = true;
  available = true;
  availabilityChecked = false;

  minDateTime = '';
  maxDateTime = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.locationId = this.route.snapshot.paramMap.get('id');

    const now = new Date();
    const nextHour = this.roundToNextHour(now);
    this.minDateTime = this.formatLocalHour(nextHour);

    const maxDate = new Date();
    maxDate.setMonth(maxDate.getMonth() + 3);
    this.maxDateTime = this.formatLocalHour(maxDate);

    if (this.locationId) {
      this.loadData();
    }
  }

  private selectWorkplaceById(id: string): void {
    const wp = this.workplaces.find(w => w.id === id);
    if (wp) {
      this.selectWorkplace(wp);
    }
  }

  private roundToNextHour(date: Date): Date {
    const rounded = new Date(date);
    rounded.setMinutes(0, 0, 0);
    if (date.getMinutes() > 0 || date.getSeconds() > 0) {
      rounded.setHours(rounded.getHours() + 1);
    }
    return rounded;
  }

  private formatLocalHour(d: Date): string {
    const pad = (n: number) => n.toString().padStart(2, '0');
    const year = d.getFullYear();
    const month = pad(d.getMonth() + 1);
    const day = pad(d.getDate());
    const hours = pad(d.getHours());
    return `${year}-${month}-${day}T${hours}:00`;
  }

  loadData(): void {
    if (this.locationId) {
      this.bookingService.getLocationById(this.locationId).subscribe(loc => this.location = loc);
      this.bookingService.getWorkplacesWithStatusByLocation(this.locationId).subscribe(data => {
        this.workplaces = data;
        this.loading = false;
        const selectedId = this.route.snapshot.queryParamMap.get('selected');
        if (selectedId) {
          this.selectWorkplaceById(selectedId);
        }
      });
    }

    this.bookingService.getServices().subscribe(services => {
      this.services = services;
    });

    const now = new Date();
    const nextHour = this.roundToNextHour(now);
    const later = new Date(nextHour.getTime() + 2 * 60 * 60 * 1000);
    this.filterStart = this.formatLocalHour(nextHour);
    this.filterEnd = this.formatLocalHour(later);
  }

  validateFilterDates(): void {
    if (!this.filterStart || !this.filterEnd) return;

    const start = new Date(this.filterStart);
    const end = new Date(this.filterEnd);
    const minStart = this.roundToNextHour(new Date());

    if (start < minStart) {
      this.filterStart = this.formatLocalHour(minStart);
      this.error = 'Время начала не может быть в прошлом. Установлено текущее время';
      return;
    }

    if (end <= start) {
      const newEnd = new Date(start.getTime() + 2 * 60 * 60 * 1000);
      this.filterEnd = this.formatLocalHour(newEnd);
      this.error = 'Время окончания должно быть позже начала. Автоматически установлено +2 часа';
    } else if (this.error) {
      this.error = '';
    }
  }

  isFilterInvalid(): boolean {
    if (!this.filterStart || !this.filterEnd) return true;
    const start = new Date(this.filterStart);
    const end = new Date(this.filterEnd);
    const minStart = this.roundToNextHour(new Date());
    return start < minStart || end <= start;
  }

  applyFilter(): void {
    if (!this.locationId || !this.filterStart || !this.filterEnd) return;

    const startDate = new Date(this.filterStart);
    const endDate = new Date(this.filterEnd);
    const minStart = this.roundToNextHour(new Date());

    if (startDate < minStart) {
      this.error = 'Время начала не может быть в прошлом';
      this.loading = false;
      return;
    }

    if (endDate <= startDate) {
      this.error = 'Время окончания не может быть раньше или равно времени начала';
      return;
    }
    if(!this.isFilterTimeInWorkingHours()){
      this.error = 'Локация закрыта в выбранное время';
      this.loading = false;
      this.filterActive = false;
      return;
    }
    this.error = '';
    this.loading = true;
    this.filterActive = true;

    let start = new Date(this.filterStart);
    let end = new Date(this.filterEnd);
    start.setMinutes(0, 0, 0);
    end.setMinutes(0, 0, 0);

    const startStr = this.formatLocalHour(start);
    const endStr = this.formatLocalHour(end);

    this.filterStart = startStr;
    this.filterEnd = endStr;

    this.bookingService.getWorkplacesByLocation(this.locationId).subscribe({
      next: (allWorkplaces) => {
        const availablePromises = allWorkplaces.map(async (workplace) => {
          try {
            const isAvailable = await this.bookingService.checkWorkplaceAvailability(
              workplace.id, startStr, endStr
            ).toPromise();
            return { workplace, available: isAvailable };
          } catch {
            return { workplace, available: false };
          }
        });

        Promise.all(availablePromises).then(results => {
          this.workplaces = results
            .filter(r => r.available && r.workplace.available)
            .map(r => r.workplace);
          this.loading = false;

          if (this.workplaces.length === 0) {
            this.error = 'Нет свободных мест на выбранное время';
          }
        });
      },
      error: () => {
        this.loading = false;
        this.error = 'Ошибка при загрузке рабочих мест';
      }
    });
  }

  clearFilter(): void {
    this.filterActive = false;
    this.error = '';
    if (!this.locationId) return;
    this.loading = true;
    this.bookingService.getWorkplacesWithStatusByLocation(this.locationId).subscribe(data => {
      this.workplaces = data;
      this.loading = false;
    });

    const now = new Date();
    const nextHour = this.roundToNextHour(now);
    const later = new Date(nextHour.getTime() + 2 * 60 * 60 * 1000);
    this.filterStart = this.formatLocalHour(nextHour);
    this.filterEnd = this.formatLocalHour(later);
  }

  selectWorkplace(workplace: WorkPlace): void {
    this.selectedWorkplace = workplace;
    this.bookingError = '';
    this.availabilityChecked = false;
    this.selectedServices.clear();

    const now = new Date();
    const roundedStart = this.roundToNextHour(now);
    const endTime = new Date(roundedStart.getTime() + 2 * 60 * 60 * 1000);
    this.bookingStart = this.formatLocalHour(roundedStart);
    this.bookingEnd = this.formatLocalHour(endTime);
    this.onTimesChange();
  }

  clearSelection(): void {
    this.selectedWorkplace = null;
    this.selectedServices.clear();
    this.availabilityChecked = false;
    this.bookingError = '';
  }

  onTimesChange(): void {
    this.availabilityChecked = false;
    this.bookingError = '';

    if (!this.selectedWorkplace || !this.bookingStart || !this.bookingEnd) return;

    let startDate = new Date(this.bookingStart);
    let endDate = new Date(this.bookingEnd);
    const minStart = this.roundToNextHour(new Date());

    if (startDate < minStart) {
      startDate = new Date(minStart);
      this.bookingStart = this.formatLocalHour(startDate);
      this.bookingError = 'Время начала не может быть в прошлом. Установлено текущее время';
    }

    if (endDate <= startDate) {
      endDate = new Date(startDate.getTime() + 3600000);
      this.bookingEnd = this.formatLocalHour(endDate);
      this.bookingError = 'Время окончания должно быть позже начала. Автоматически установлено +1 час';
    }

    this.bookingStart = this.formatLocalHour(startDate);
    this.bookingEnd = this.formatLocalHour(endDate);

    this.bookingService.checkWorkplaceAvailability(
      this.selectedWorkplace.id,
      this.bookingStart,
      this.bookingEnd
    ).subscribe({
      next: (isAvailable) => {
        this.available = isAvailable;
        this.availabilityChecked = true;
      },
      error: () => {
        this.available = false;
        this.availabilityChecked = true;
      }
    });
  }

  toggleService(service: AdditionalService): void {
    if (this.selectedServices.has(service.id)) {
      this.selectedServices.delete(service.id);
    } else {
      this.selectedServices.add(service.id);
    }
  }

  getWorkplacePrice(): number {
    if (!this.selectedWorkplace || !this.bookingStart || !this.bookingEnd) return 0;
    const start = new Date(this.bookingStart);
    const end = new Date(this.bookingEnd);
    const diffMs = end.getTime() - start.getTime();
    const hours = Math.max(1, Math.round(diffMs / 3600000));
    return this.selectedWorkplace.priceForHour * hours;
  }

  getServicesTotal(): number {
    let total = 0;
    this.services.forEach(service => {
      if (this.selectedServices.has(service.id)) {
        total += service.price;
      }
    });
    return total;
  }


  isLocationOpenNow(): boolean {
    if (!this.location?.openingTime || !this.location?.closingTime)
      return true;
    const now = new Date();
    const currMinutes = now.getHours() * 60+ now.getMinutes();
    const openParts = this.location.openingTime.split(':');
    const closeParts = this.location.closingTime.split(':');
    const openMinutes = parseInt(openParts[0]) * 60 + parseInt(openParts [1] || '0');
    const closeMinutes = parseInt(closeParts[0]) * 60 + parseInt(closeParts[1] || '0');
    return currMinutes >= openMinutes && currMinutes <= closeMinutes;
  }

  isFilterTimeInWorkingHours(): boolean {
  if (!this.location?.openingTime || !this.location?.closingTime || !this.filterStart || !this.filterEnd) return true;
  const parse = (t: string) => { const p = t.split(':'); return parseInt(p[0]) * 60+ parseInt(p[1] || '0'); };
  const open = parse (this.location.openingTime);
  const close = parse(this.location.closingTime);
  const start = new Date(this.filterStart);
  const end = new Date(this.filterEnd);
  const startM = start.getHours() * 60+ start.getMinutes();
  const endM = end.getHours() * 60+ start.getMinutes();
  return startM >= open && startM <= close && endM >= open && endM <= close;
  }

  getTotalPrice(): number {
    return this.getWorkplacePrice() + this.getServicesTotal();
  }

  isWithinWorkingHours(): boolean {
    if (!this.location || !this.bookingStart) return true;

    const parse = (t: string) => {const p = t.split(':'); return parseInt(p[0]) * 60 + parseInt(p[1] || '0'); };
    const open = parse(this.location.openingTime || '0:00');
    const close = parse(this.location.closingTime || '23:59');
    const start = new Date(this.bookingStart);

    const startM = start.getHours()*60+start.getMinutes();
    let endM= startM + 60;

    if(this.bookingEnd){
      const end = new Date(this.bookingEnd);
      endM = end.getHours()* 60 + end.getMinutes();
    }


    return startM >= open && startM <= close && endM >= open && endM <= close;
  }

  getBookingValidationError(): string | null {
    if (!this.bookingStart || !this.bookingEnd) return null;

    const start = new Date(this.bookingStart);
    const end = new Date(this.bookingEnd);
    const now = new Date();
    const minStart = this.roundToNextHour (now);
    if (start < minStart) {
      return 'Время начала не может быть в прошлом';
    }

    if (end <= start) {
      return 'Время окончания должно быть позже времени начал';
    }
    return null;
  }

  canBook(): boolean {
    return !!this.selectedWorkplace &&
           !!this.bookingStart &&
           !!this.bookingEnd &&
           this.authService.isAuthenticated() &&
           this.available &&
           this.isWithinWorkingHours() &&
           !this.getBookingValidationError() &&
           !this.bookingError;
  }

 createBooking(): void {
  if (!this.selectedWorkplace || !this.bookingStart || !this.bookingEnd) return;

  this.bookingError = '';

  this.bookingService.createBooking({
    workPlaceId: this.selectedWorkplace.id,
    startTime: this.bookingStart,
    endTime: this.bookingEnd
  }).subscribe({
    next: (booking) => {
      this.bookingService.createContract(booking.id).subscribe({
        next: (contract) => {
          console.log('Договор создан:', contract);
          this.addServicesToBooking(booking.id);
        },
        error: (err) => {
          console.error('Ошибка создания договора:', err);
          this.addServicesToBooking(booking.id);
        }
      });
    },
    error: (err) => {
      this.bookingError = err.error?.message || 'Ошибка бронирования';
    }
  });
}

  addServicesToBooking(bookingId: string): void {
    const services = Array.from(this.selectedServices);

    if (services.length === 0) {
      this.router.navigate(['/'], { queryParams: { booked: 'true' } });
      return;
    }

    let completed = 0;
    services.forEach(serviceId => {
      this.bookingService.addServiceToBooking(bookingId, serviceId, 1).subscribe({
        next: () => {
          completed++;
          if (completed === services.length) {
            this.router.navigate(['/'], { queryParams: { booked: 'true' } });
          }
        },
        error: () => {
          completed++;
          if (completed === services.length) {
            this.router.navigate(['/'], { queryParams: { booked: 'true' } });
          }
        }
      });
    });
  }
}