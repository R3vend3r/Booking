import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../../services/booking.service';
import { WorkPlace, Location, AdditionalService } from '../../models/booking.model';
import { AuthService } from '../../services/auth.service';

@Component({
  standalone: false,
  selector: 'app-workplaces',
  template: `
    <div class="container">
      <div style="margin-bottom: 24px;">
        <a routerLink="/" style="color: var(--primary); text-decoration: none;">
          ← На главную
        </a>
      </div>
      
      <div *ngIf="location" style="margin-bottom: 32px;">
        <h1>{{ location.name }}</h1>
        <p style="color: #64748b;">{{ location.address }} | {{ location.city }}</p>
      </div>
      
      <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 32px;">
        <div>
          <h2 style="margin-bottom: 16px;">Рабочие места</h2>
          
          <div *ngIf="loading" class="loading">
            <p>Загрузка...</p>
          </div>
          
          <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 16px;">
            <div *ngFor="let workplace of workplaces" class="card">
              <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                <h3 style="font-size: 16px;">{{ workplace.name }}</h3>
                <span *ngIf="!workplace.available" 
                      style="background: #fee2e2; color: #dc2626; padding: 2px 8px; border-radius: 4px; font-size: 12px;">
                  Занято
                </span>
              </div>
              
              <p style="color: #64748b; font-size: 14px; margin-bottom: 8px;">
                Вместимость: {{ workplace.capacity }} чел.
              </p>
              
              <p *ngIf="workplace.description" style="color: #64748b; font-size: 14px; margin-bottom: 12px;">
                {{ workplace.description }}
              </p>
              
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-size: 18px; font-weight: 600; color: var(--primary);">
                  {{ workplace.priceForHour }} ₽/час
                </span>
                
                <button class="btn btn-primary" 
                        style="padding: 8px 16px;"
                        (click)="selectWorkplace(workplace)"
                        [disabled]="!workplace.available">
                  Выбрать
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div>
          <div *ngIf="selectedWorkplace" class="card" style="position: sticky; top: 24px;">
            <h3 style="margin-bottom: 16px;">Бронирование</h3>
            
            <p style="margin-bottom: 8px;"><strong>{{ selectedWorkplace.name }}</strong></p>
            <p style="color: #64748b; margin-bottom: 16px;">{{ selectedWorkplace.priceForHour }} ₽/час</p>
            
            <div class="form-group">
              <label>Дата и время начала</label>
              <input type="datetime-local" [(ngModel)]="bookingStart" (ngModelChange)="onTimesChange()" />
            </div>
            
            <div class="form-group">
              <label>Дата и время окончания</label>
              <input type="datetime-local" [(ngModel)]="bookingEnd" (ngModelChange)="onTimesChange()" />
            </div>
            
            <div *ngIf="availabilityChecked && !available" class="error" style="margin: 8px 0;">
              Это время уже занято
            </div>
            
            <div style="margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--border);">
              <h4 style="margin-bottom: 12px;">Дополнительные услуги</h4>
              
              <div *ngFor="let service of services" style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                <label style="display: flex; align-items: center; gap: 8px;">
                  <input type="checkbox" 
                         [checked]="selectedServices.has(service.id)"
                         (change)="toggleService(service)" />
                  {{ service.name }}
                </label>
                <span style="color: #64748b;">{{ service.price }} ₽</span>
              </div>
            </div>
            
            <div *ngIf="error" class="error" style="margin: 16px 0;">
              {{ error }}
            </div>
            
            <button class="btn btn-primary" 
                    style="width: 100%; margin-top: 16px;"
                    (click)="createBooking()"
                    [disabled]="!canBook()">
              Забронировать
            </button>
          </div>
          
          <div *ngIf="!selectedWorkplace" class="card">
            <p style="color: #64748b; text-align: center;">
              Выберите рабочее место для бронирования
            </p>
          </div>
        </div>
      </div>
    </div>
  `
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
  error = '';
  loading = true;
  available = true;
  availabilityChecked = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.locationId = this.route.snapshot.paramMap.get('id');
    
    if (this.locationId) {
      this.loadData();
    }
  }

  loadData(): void {
    if (this.locationId) {
      this.bookingService.getLocationById(this.locationId).subscribe(loc => this.location = loc);
      this.bookingService.getWorkplacesByLocation(this.locationId).subscribe(data => {
        this.workplaces = data;
        this.loading = false;
      });
    }
    
    this.bookingService.getServices().subscribe(services => {
      this.services = services;
    });
  }

  private formatLocal(d: Date): string {
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  selectWorkplace(workplace: WorkPlace): void {
    this.selectedWorkplace = workplace;
    this.error = '';
    this.availabilityChecked = false;
    
    const now = new Date();
    const later = new Date(now.getTime() + 2 * 60 * 60 * 1000);
    this.bookingStart = this.formatLocal(now);
    this.bookingEnd = this.formatLocal(later);
    this.onTimesChange();
  }

  onTimesChange(): void {
    this.availabilityChecked = false;
    if (!this.selectedWorkplace || !this.bookingStart || !this.bookingEnd) return;
    this.bookingService.checkWorkplaceAvailability(this.selectedWorkplace.id, this.bookingStart, this.bookingEnd).subscribe({
      next: (isAvailable) => {
        this.available = isAvailable;
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

  canBook(): boolean {
    return !!this.selectedWorkplace && !!this.bookingStart && !!this.bookingEnd && this.authService.isAuthenticated() && this.available;
  }

  createBooking(): void {
    if (!this.selectedWorkplace || !this.bookingStart || !this.bookingEnd) return;

    this.error = '';
    
    this.bookingService.createBooking({
      clientId: '',
      workPlaceId: this.selectedWorkplace.id,
      startTime: this.bookingStart,
      endTime: this.bookingEnd
    }).subscribe({
      next: (booking) => {
        this.addServicesToBooking(booking.id);
      },
      error: (err) => {
        this.error = err.error?.message || 'Ошибка бронирования';
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
