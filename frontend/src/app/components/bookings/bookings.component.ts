import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BookingService } from '../../services/booking.service';
import { AuthService } from '../../services/auth.service';
import { Booking } from '../../models/booking.model';

@Component({
  standalone: false,
  selector: 'app-bookings',
  templateUrl: './bookings.component.html',
  styleUrls: ['./bookings.component.css']
})
export class BookingsComponent implements OnInit {
  bookings: Booking[] = [];
  loading = true;
  selectedBooking: Booking | null = null;
  page = 0;
  pageSize = 5;
  services: any[] = [];
  editingBooking: Booking | null = null;
  editStart = '';
  editEnd = '';
  editError = '';
  editAvailable = true;
  editAvailabilityChecked = false;
  allServices: any[] = [];
  editServices: Map<string, number> = new Map();
  editServicesQuantities: { [key: string]: number } = {};
  editServicesLoading = false;
  editWorkplacePricePerHour: number = 0;
  workplacePricePerHour: number = 0;
  Math = Math;
  workplacePricesMap: Map<string, number> = new Map();
  bookingServicesTotal: Map<string, number> = new Map();

  constructor(
    private bookingService: BookingService,
    public authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadBookings();
  }

  loadBookings(): void {
  this.loading = true;
  this.bookingService.getWorkplaces().subscribe({
    next: (workplaces) => {
      workplaces.forEach(wp => this.workplacePricesMap.set(wp.id, wp.priceForHour));
    }
  });
  this.bookingService.getMyBookings().subscribe({
    next: (data) => { 
      this.bookings = data;
      data.forEach(booking => {
        this.bookingService.getServicesByBooking(booking.id).subscribe({
          next: (services) => {
            this.bookingServicesTotal.set(booking.id,
              services.reduce((sum, s) => sum + s.priceAtBookingTime * s.quantity, 0));
          }
        });
      });
      this.loading = false;
    },
    error: () => { this.loading = false; }
  });
}

  formatDate(dateStr: string): string {
    const d = new Date(dateStr);
    return d.toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
  }

 getBookingStatus(booking: Booking): string {
  const paymentStatus = booking.paymentStatus;
  const endTime = new Date(booking.endTime);
  const now = new Date();
  
  if (paymentStatus === 'CANCELLED') return 'CANCELLED';
  if (paymentStatus === 'PAID') {
    return endTime < now ? 'COMPLETED' : 'PAID';
  }
  if (paymentStatus === 'PENDING') {
    return endTime < now ? 'EXPIRED' : 'PENDING';
  }
  return endTime < now ? 'EXPIRED' : 'UNPAID';
}
canEdit(booking: Booking): boolean {
  const paymentStatus = booking.paymentStatus;
  const endTime = new Date(booking.endTime);
  const now = new Date();
  return (paymentStatus === 'PENDING' || paymentStatus === undefined) && endTime > now;
}
canPay(booking: Booking): boolean {
  const paymentStatus = booking.paymentStatus;
  const endTime = new Date(booking.endTime);
  const now = new Date();
  
  return paymentStatus === 'PENDING' && endTime > now;
}
isCancelable(booking: Booking): boolean {
  const paymentStatus = booking.paymentStatus;
  const endTime = new Date(booking.endTime);
  const now = new Date();
  return (paymentStatus === 'PENDING' || paymentStatus === undefined) && endTime > now;
}
  openDetails(booking: Booking): void {
    this.selectedBooking = booking;
    this.services = [];
    this.bookingService.getWorkplaceById(booking.workPlaceId).subscribe({
      next: (wp) => { this.workplacePricePerHour = wp.priceForHour; }
    });
    this.bookingService.getServicesByBooking(booking.id).subscribe({
      next: (data) => { this.services = data; }
    });
  }

  closeDetails(): void {
    this.selectedBooking = null;
    this.services = [];
  }

  workplaceCost(booking: Booking): number {
    const start = new Date(booking.startTime);
    const end = new Date(booking.endTime);
    const hours = Math.max(1, Math.round((end.getTime() - start.getTime()) / 3600000));
    return hours * this.workplacePricePerHour;
  }

  servicesTotal(): number {
    return this.services.reduce((sum, s) => sum + s.priceAtBookingTime * s.quantity, 0);
  }

  totalBookingPrice(booking: Booking): number {
    const start = new Date(booking.startTime);
    const end = new Date(booking.endTime);
    const hours = Math.max(1, Math.round((end.getTime() - start.getTime()) / 3600000));
    const wpPrice = (this.workplacePricesMap.get(booking.workPlaceId) || 0) * hours;
    const servicesPrice = this.bookingServicesTotal.get(booking.id) || 0;
    return wpPrice + servicesPrice;
  }

  startEdit(booking: Booking): void {
    this.editingBooking = booking;
    this.editError = '';
    this.editAvailable = true;
    this.editAvailabilityChecked = false;
    
    const start = new Date(booking.startTime);
    const end = new Date(booking.endTime);
    this.loadAllServicesForEdit();
    this.loadCurrentBookingServices(booking.id);
    this.bookingService.getWorkplaceById(booking.workPlaceId).subscribe(workplace => {
      this.editWorkplacePricePerHour = workplace.priceForHour;
    });
    start.setMinutes(0, 0, 0);
    end.setMinutes(0, 0, 0);
    if (end <= start) {
      end.setHours(start.getHours() + 1);
    }
    
    this.editStart = this.formatDateTimeLocal(start);
    this.editEnd = this.formatDateTimeLocal(end);
    this.checkEditAvailability();
  }
formatDateTimeLocal(date: Date): string {
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:00`;
  }

  onEditTimesChange(): void {
    this.checkEditAvailability();
  }

 checkEditAvailability(): void {
    if (!this.editingBooking || !this.editStart || !this.editEnd) return;
    
    this.editAvailabilityChecked = false;
    this.bookingService.checkWorkplaceAvailability(
        this.editingBooking.workPlaceId,
        this.editStart,
        this.editEnd,
        this.editingBooking.id
    ).subscribe({
        next: (isAvailable) => {
            this.editAvailable = isAvailable;
            this.editAvailabilityChecked = true;
        }
    });
}
  cancelEdit(): void {
    this.editingBooking = null;
    this.editError = '';
  }

  saveEdit(): void {
  if (!this.editingBooking || !this.editStart || !this.editEnd) return;
  
  if (!this.editAvailable) {
    this.editError = 'Это время уже занято';
    return;
  }
  

  this.bookingService.updateBooking(this.editingBooking.id, {
    clientId: this.editingBooking.clientId,
    workPlaceId: this.editingBooking.workPlaceId,
    startTime: this.editStart,
    endTime: this.editEnd
  }).subscribe({
    next: () => {

      this.updateBookingServices();
    },
    error: (err) => { 
      this.editError = err.error?.message || 'Ошибка при обновлении'; 
    }
  });
}

updateBookingServices(): void {
  if (!this.editingBooking) return;
  

  this.bookingService.getServicesByBooking(this.editingBooking.id).subscribe({
    next: (currentServices) => {
      const currentServiceIds = new Set(currentServices.map(s => s.serviceId));
      const newServiceIds = new Set(this.editServices.keys());
      

      currentServiceIds.forEach(serviceId => {
        if (!newServiceIds.has(serviceId)) {
          this.bookingService.removeServiceFromBooking(this.editingBooking!.id, serviceId).subscribe();
        }
      });
      

      newServiceIds.forEach(serviceId => {
        const newQuantity = this.editServices.get(serviceId)!;
        if (currentServiceIds.has(serviceId)) {

          this.bookingService.updateServiceQuantity(this.editingBooking!.id, serviceId, newQuantity).subscribe();
        } else {

          this.bookingService.addServiceToBooking(this.editingBooking!.id, serviceId, newQuantity).subscribe();
        }
      });
      

      this.cancelEdit();
      this.loadBookings();
    }
  });
}

  cancelBooking(booking: Booking): void {
    if (!confirm('Отменить бронирование?')) return;
    this.bookingService.cancelBooking(booking.id).subscribe({
      next: () => { this.loadBookings(); },
      error: (err) => { alert(err.error?.message || 'Ошибка при отмене'); }
    });
  }

  payBooking(booking: Booking): void {
    if (!confirm('Оплатить бронирование?')) return;
    if (booking.contractId) {
      this.bookingService.payContract(booking.contractId, 'CARD').subscribe({
        next: () => { this.loadBookings(); },
        error: (err) => { alert(err.error?.message || 'Ошибка оплаты'); }
      });
    } else {
      this.bookingService.createContract(booking.id).subscribe({
        next: (contract) => {
          this.bookingService.payContract(contract.id, 'CARD').subscribe({
            next: () => { this.loadBookings(); },
            error: (err) => { alert(err.error?.message || 'Ошибка оплаты'); }
          });
        },
        error: (err) => { alert(err.error?.message || 'Ошибка создания договора'); }
      });
    }
  }
loadAllServicesForEdit(): void {
  this.editServicesLoading = true;
  this.bookingService.getServices().subscribe({
    next: (services) => {
      this.allServices = services;
      this.editServicesLoading = false;
    },
    error: () => { this.editServicesLoading = false; }
  });
}

loadCurrentBookingServices(bookingId: string): void {
  console.log('=== loadCurrentBookingServices START ===');
  console.log('bookingId:', bookingId);
  
  this.bookingService.getServicesByBooking(bookingId).subscribe({
    next: (services) => {
      console.log('getServicesByBooking ответ:', services);
      console.log('Тип ответа:', typeof services);
      console.log('Массив?', Array.isArray(services));
      
      if (!services || services.length === 0) {
        console.log('Нет услуг для этого бронирования');
        return;
      }
      
      services.forEach((service, index) => {
        console.log(`Услуга ${index}:`, service);
        console.log(`service.serviceId = ${service.serviceId}`);
        console.log(`service.quantity = ${service.quantity}`);
        
        if (service.serviceId) {
          this.editServices.set(service.serviceId, service.quantity);
          this.editServicesQuantities[service.serviceId] = service.quantity;
          console.log(`Добавлено в editServices: ключ=${service.serviceId}, значение=${service.quantity}`);
        } else {
          console.error('Нет serviceId у услуги!', service);
        }
      });
      
      console.log('Итоговый editServices:', Array.from(this.editServices.entries()));
      console.log('=== loadCurrentBookingServices END ===');
    },
    error: (err) => {
      console.error('Ошибка загрузки услуг:', err);
    }
  });
}
isServiceSelected(serviceId: string): boolean {
  return this.editServices.has(serviceId);
}

toggleEditService(service: any): void {
  const serviceId = service.id;
  if (this.editServices.has(serviceId)) {
    this.editServices.delete(serviceId);
    delete this.editServicesQuantities[serviceId];
  } else {
    this.editServices.set(serviceId, 1);
    this.editServicesQuantities[serviceId] = 1;
  }
}

updateEditServiceQuantity(serviceId: string, quantity: number): void {
  if (quantity > 0) {
    this.editServices.set(serviceId, quantity);
    this.editServicesQuantities[serviceId] = quantity;
  }
}

getEditServicesTotal(): number {
  let total = 0;
  this.editServices.forEach((quantity, serviceId) => {
    const service = this.allServices.find(s => s.id === serviceId);
    if (service) {
      total += service.price * quantity;
    }
  });
  return total;
}

getEditWorkplacePrice(): number {
  if (!this.editingBooking || !this.editStart || !this.editEnd) return 0;
  if (this.editWorkplacePricePerHour === 0) return 0;
  
  const start = new Date(this.editStart);
  const end = new Date(this.editEnd);
  const hours = Math.max(1, Math.round((end.getTime() - start.getTime()) / 3600000));
  
  return this.editWorkplacePricePerHour * hours;
 }

  get paginatedBookings(): Booking[] {
    return this.bookings.slice(this.page * this.pageSize, (this.page + 1) * this.pageSize);
  }

  prevPage(): void {
    if (this.page > 0) this.page--;
  }

  nextPage(): void {
    if ((this.page + 1) * this.pageSize < this.bookings.length) this.page++;
  }
}
