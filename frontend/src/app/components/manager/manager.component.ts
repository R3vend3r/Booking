import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

interface LocationItem {
  id: string;
  name: string;
  address: string;
  city: string;
  openingTime: string;
  closingTime: string;
  contactPhone: string;
  workplacesCount?: number;
}

interface WorkPlaceItem {
  id: string;
  name: string;
  capacity: number;
  description: string;
  locationId: string;
  priceForHour: number;
  available: boolean;
  locationName?: string;
}

interface ServiceItem {
  id: string;
  name: string;
  description: string;
  price: number;
}

interface BookingItem {
  id: string;
  startTime: string;
  endTime: string;
  clientId: string;
  workPlaceId: string;
}

interface ContractItem {
  id: string;
  contractNumber: string;
  totalAmount: number;
  paymentStatus: string;
  paymentDate: string;
  paymentMethod: string;
  bookingId: string;
}

@Component({
  standalone: false,
  selector: 'app-manager',
  template: `
    <div class="container" style="padding-top: 32px;">
      <h1 style="margin-bottom: 24px;">Панель менеджера</h1>

      <div style="display: flex; gap: 8px; margin-bottom: 24px; flex-wrap: wrap;">
        <button class="btn" [class.btn-primary]="activeTab === 'locations'" (click)="activeTab = 'locations'">Локации</button>
        <button class="btn" [class.btn-primary]="activeTab === 'workplaces'" (click)="activeTab = 'workplaces'">Рабочие места</button>
        <button class="btn" [class.btn-primary]="activeTab === 'services'" (click)="activeTab = 'services'">Услуги</button>
        <button class="btn" [class.btn-primary]="activeTab === 'bookings'" (click)="activeTab = 'bookings'">Бронирования</button>
        <button class="btn" [class.btn-primary]="activeTab === 'contracts'" (click)="activeTab = 'contracts'">Контракты</button>
      </div>

      <!-- ===== LOCATIONS TAB ===== -->
      <div *ngIf="activeTab === 'locations'">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
          <h2>Локации</h2>
          <button class="btn btn-primary" (click)="startAddLocation()">+ Добавить</button>
        </div>

        <div *ngIf="locationForm" class="card" style="margin-bottom: 16px;">
          <h3 style="margin-bottom: 16px;">{{ editingLocationId ? 'Редактировать' : 'Новая' }} локация</h3>
          <div class="form-group">
            <label>Название</label>
            <input type="text" [(ngModel)]="locationForm.name" />
          </div>
          <div class="form-group">
            <label>Адрес</label>
            <input type="text" [(ngModel)]="locationForm.address" />
          </div>
          <div class="form-group">
            <label>Город</label>
            <input type="text" [(ngModel)]="locationForm.city" />
          </div>
          <div class="form-group">
            <label>Время открытия</label>
            <input type="time" [(ngModel)]="locationForm.openingTime" />
          </div>
          <div class="form-group">
            <label>Время закрытия</label>
            <input type="time" [(ngModel)]="locationForm.closingTime" />
          </div>
          <div class="form-group">
            <label>Телефон</label>
            <input type="text" [(ngModel)]="locationForm.contactPhone" />
          </div>
          <div style="display: flex; gap: 8px;">
            <button class="btn btn-primary" (click)="saveLocation()">Сохранить</button>
            <button class="btn btn-secondary" (click)="cancelLocationForm()">Отмена</button>
          </div>
        </div>

        <div *ngIf="locationsLoading" class="loading"><p>Загрузка...</p></div>
        <div *ngIf="!locationsLoading && locations.length === 0" style="text-align: center; padding: 40px; color: #64748b;">
          Локации не найдены
        </div>
        <div *ngIf="!locationsLoading && locations.length > 0" style="display: grid; gap: 12px;">
          <div *ngFor="let loc of locations" class="card" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <h3 style="font-size: 16px;">{{ loc.name }}</h3>
              <p style="color: #64748b; font-size: 13px;">{{ loc.address }}, {{ loc.city }}</p>
              <p style="color: #64748b; font-size: 13px;">{{ loc.openingTime }} — {{ loc.closingTime }} | {{ loc.contactPhone }}</p>
            </div>
            <div style="display: flex; gap: 8px;">
              <button class="btn btn-secondary" (click)="startEditLocation(loc)">✏️</button>
              <button class="btn" style="background: #fee2e2; color: #dc2626;" (click)="deleteLocation(loc.id)">🗑️</button>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== WORKPLACES TAB ===== -->
      <div *ngIf="activeTab === 'workplaces'">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 8px;">
          <h2>Рабочие места</h2>
          <div style="display: flex; gap: 8px;">
            <select [(ngModel)]="wpFilterLocation" (change)="loadWorkplaces()" style="padding: 8px; border: 1px solid var(--border); border-radius: 8px;">
              <option value="">Все локации</option>
              <option *ngFor="let loc of locations" [value]="loc.id">{{ loc.name }}</option>
            </select>
            <button class="btn btn-primary" (click)="startAddWorkplace()">+ Добавить</button>
          </div>
        </div>

        <div *ngIf="wpForm" class="card" style="margin-bottom: 16px;">
          <h3 style="margin-bottom: 16px;">{{ editingWpId ? 'Редактировать' : 'Новое' }} рабочее место</h3>
          <div class="form-group">
            <label>Название</label>
            <input type="text" [(ngModel)]="wpForm.name" />
          </div>
          <div class="form-group">
            <label>Локация</label>
            <select [(ngModel)]="wpForm.locationId">
              <option value="">Выберите локацию</option>
              <option *ngFor="let loc of locations" [value]="loc.id">{{ loc.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Вместимость (чел.)</label>
            <input type="number" [(ngModel)]="wpForm.capacity" />
          </div>
          <div class="form-group">
            <label>Описание</label>
            <textarea [(ngModel)]="wpForm.description" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>Цена за час (₽)</label>
            <input type="number" [(ngModel)]="wpForm.priceForHour" />
          </div>
          <div style="display: flex; gap: 8px;">
            <button class="btn btn-primary" (click)="saveWorkplace()">Сохранить</button>
            <button class="btn btn-secondary" (click)="cancelWpForm()">Отмена</button>
          </div>
        </div>

        <div *ngIf="wpLoading" class="loading"><p>Загрузка...</p></div>
        <div *ngIf="!wpLoading && workplaces.length === 0" style="text-align: center; padding: 40px; color: #64748b;">
          Рабочие места не найдены
        </div>
        <div *ngIf="!wpLoading && workplaces.length > 0" style="display: grid; gap: 12px;">
          <div *ngFor="let wp of workplaces" class="card" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <h3 style="font-size: 16px;">{{ wp.name }}</h3>
              <p style="color: #64748b; font-size: 13px;">{{ wp.locationName }} | Вместимость: {{ wp.capacity }} чел. | {{ wp.priceForHour }} ₽/час</p>
              <p *ngIf="wp.description" style="color: #94a3b8; font-size: 12px;">{{ wp.description }}</p>
            </div>
            <div style="display: flex; gap: 8px; align-items: center;">
              <span *ngIf="wp.available" style="color: #22c55e; font-size: 13px;">Доступно</span>
              <span *ngIf="!wp.available" style="color: #ef4444; font-size: 13px;">Занято</span>
              <button class="btn btn-secondary" style="padding: 6px 12px; font-size: 12px;" (click)="toggleAvailability(wp)">Переключить</button>
              <button class="btn btn-secondary" (click)="startEditWorkplace(wp)">✏️</button>
              <button class="btn" style="background: #fee2e2; color: #dc2626;" (click)="deleteWorkplace(wp.id)">🗑️</button>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== SERVICES TAB ===== -->
      <div *ngIf="activeTab === 'services'">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
          <h2>Услуги</h2>
          <button class="btn btn-primary" (click)="startAddService()">+ Добавить</button>
        </div>

        <div *ngIf="svcForm" class="card" style="margin-bottom: 16px;">
          <h3 style="margin-bottom: 16px;">{{ editingSvcId ? 'Редактировать' : 'Новая' }} услуга</h3>
          <div class="form-group">
            <label>Название</label>
            <input type="text" [(ngModel)]="svcForm.name" />
          </div>
          <div class="form-group">
            <label>Описание</label>
            <textarea [(ngModel)]="svcForm.description" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>Цена (₽)</label>
            <input type="number" [(ngModel)]="svcForm.price" />
          </div>
          <div style="display: flex; gap: 8px;">
            <button class="btn btn-primary" (click)="saveService()">Сохранить</button>
            <button class="btn btn-secondary" (click)="cancelSvcForm()">Отмена</button>
          </div>
        </div>

        <div *ngIf="svcLoading" class="loading"><p>Загрузка...</p></div>
        <div *ngIf="!svcLoading && services.length === 0" style="text-align: center; padding: 40px; color: #64748b;">
          Услуги не найдены
        </div>
        <div *ngIf="!svcLoading && services.length > 0" style="display: grid; gap: 12px;">
          <div *ngFor="let svc of services" class="card" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <h3 style="font-size: 16px;">{{ svc.name }}</h3>
              <p style="color: #64748b; font-size: 13px;">{{ svc.description }}</p>
              <p style="font-weight: 600; color: var(--primary); font-size: 14px;">{{ svc.price }} ₽</p>
            </div>
            <div style="display: flex; gap: 8px;">
              <button class="btn btn-secondary" (click)="startEditService(svc)">✏️</button>
              <button class="btn" style="background: #fee2e2; color: #dc2626;" (click)="deleteService(svc.id)">🗑️</button>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== BOOKINGS TAB ===== -->
      <div *ngIf="activeTab === 'bookings'">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 8px;">
          <h2>Бронирования</h2>
          <div style="display: flex; gap: 8px; align-items: center;">
            <label style="font-size: 13px;">С:</label>
            <input type="datetime-local" [(ngModel)]="dateStart" style="padding: 6px; border: 1px solid var(--border); border-radius: 8px;" />
            <label style="font-size: 13px;">По:</label>
            <input type="datetime-local" [(ngModel)]="dateEnd" style="padding: 6px; border: 1px solid var(--border); border-radius: 8px;" />
            <button class="btn btn-secondary" style="padding: 6px 12px; font-size: 12px;" (click)="loadBookingsByDateRange()">Поиск</button>
            <button class="btn btn-secondary" style="padding: 6px 12px; font-size: 12px;" (click)="loadActiveBookings()">Активные</button>
          </div>
        </div>

        <div *ngIf="bkLoading" class="loading"><p>Загрузка...</p></div>
        <div *ngIf="!bkLoading && bookings.length === 0" style="text-align: center; padding: 40px; color: #64748b;">
          Бронирования не найдены
        </div>
        <div *ngIf="!bkLoading && bookings.length > 0" style="display: grid; gap: 12px;">
          <div *ngFor="let bk of bookings" class="card" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <p style="font-size: 14px; font-weight: 500;">Рабочее место: {{ bk.workPlaceId }}</p>
              <p style="color: #64748b; font-size: 13px;">{{ formatDate(bk.startTime) }} — {{ formatDate(bk.endTime) }}</p>
              <p style="color: #94a3b8; font-size: 12px;">Клиент: {{ bk.clientId }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- ===== CONTRACTS TAB ===== -->
      <div *ngIf="activeTab === 'contracts'">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
          <h2>Ожидающие контракты</h2>
          <button class="btn btn-secondary" (click)="loadPendingContracts()">Обновить</button>
        </div>

        <div *ngIf="ctLoading" class="loading"><p>Загрузка...</p></div>
        <div *ngIf="!ctLoading && contracts.length === 0" style="text-align: center; padding: 40px; color: #64748b;">
          Ожидающие контракты не найдены
        </div>
        <div *ngIf="!ctLoading && contracts.length > 0" style="display: grid; gap: 12px;">
          <div *ngFor="let ct of contracts" class="card" style="display: flex; justify-content: space-between; align-items: center;">
            <div>
              <p style="font-size: 14px; font-weight: 500;">Контракт #{{ ct.contractNumber }}</p>
              <p style="color: #64748b; font-size: 13px;">Сумма: {{ ct.totalAmount }} ₽</p>
              <p style="color: #94a3b8; font-size: 12px;">Статус: {{ getPaymentStatusLabel(ct.paymentStatus) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ManagerComponent implements OnInit {
  private apiUrl = 'http://localhost:8080/api';
  activeTab = 'locations';

  // Locations
  locations: LocationItem[] = [];
  locationsLoading = true;
  locationForm: any = null;
  editingLocationId: string | null = null;

  // Workplaces
  workplaces: WorkPlaceItem[] = [];
  wpLoading = true;
  wpForm: any = null;
  editingWpId: string | null = null;
  wpFilterLocation = '';

  // Services
  services: ServiceItem[] = [];
  svcLoading = true;
  svcForm: any = null;
  editingSvcId: string | null = null;

  // Bookings
  bookings: BookingItem[] = [];
  bkLoading = true;
  dateStart = '';
  dateEnd = '';

  // Contracts
  contracts: ContractItem[] = [];
  ctLoading = true;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadLocations();
    this.loadServices();
    this.loadWorkplaces();
  }

  private getHeaders() {
    const user = localStorage.getItem('currentUser');
    const token = user ? JSON.parse(user).token : '';
    return { Authorization: `Bearer ${token}` };
  }

  // ============ LOCATIONS ============

  loadLocations(): void {
    this.locationsLoading = true;
    this.http.get<LocationItem[]>(`${this.apiUrl}/locations`).subscribe({
      next: (data) => { this.locations = data; this.locationsLoading = false; },
      error: () => this.locationsLoading = false
    });
  }

  startAddLocation(): void {
    this.editingLocationId = null;
    this.locationForm = { name: '', address: '', city: '', openingTime: '09:00', closingTime: '21:00', contactPhone: '' };
  }

  startEditLocation(loc: LocationItem): void {
    this.editingLocationId = loc.id;
    this.locationForm = {
      name: loc.name,
      address: loc.address,
      city: loc.city,
      openingTime: loc.openingTime?.substring(0, 5),
      closingTime: loc.closingTime?.substring(0, 5),
      contactPhone: loc.contactPhone
    };
  }

  cancelLocationForm(): void {
    this.locationForm = null;
    this.editingLocationId = null;
  }

  saveLocation(): void {
    const body = {
      branchName: this.locationForm.name,
      address: this.locationForm.address,
      city: this.locationForm.city,
      openingTime: this.locationForm.openingTime,
      closingTime: this.locationForm.closingTime,
      contactPhone: this.locationForm.contactPhone
    };

    if (this.editingLocationId) {
      this.http.put(`${this.apiUrl}/locations/${this.editingLocationId}`, body, { headers: this.getHeaders() })
        .subscribe({ next: () => { this.cancelLocationForm(); this.loadLocations(); } });
    } else {
      this.http.post(`${this.apiUrl}/locations`, body, { headers: this.getHeaders() })
        .subscribe({ next: () => { this.cancelLocationForm(); this.loadLocations(); } });
    }
  }

  deleteLocation(id: string): void {
    if (!confirm('Удалить локацию?')) return;
    this.http.delete(`${this.apiUrl}/locations/${id}`, { headers: this.getHeaders() })
      .subscribe({ next: () => this.loadLocations() });
  }

  // ============ WORKPLACES ============

  loadWorkplaces(): void {
    this.wpLoading = true;
    const url = this.wpFilterLocation
      ? `${this.apiUrl}/workplaces/location/${this.wpFilterLocation}`
      : `${this.apiUrl}/workplaces`;

    this.http.get<WorkPlaceItem[]>(url).subscribe({
      next: (data) => {
        this.workplaces = data.map(wp => ({
          ...wp,
          locationName: this.locations.find(l => l.id === wp.locationId)?.name || wp.locationId
        }));
        this.wpLoading = false;
      },
      error: () => this.wpLoading = false
    });
  }

  startAddWorkplace(): void {
    this.editingWpId = null;
    this.wpForm = { name: '', locationId: '', capacity: 1, description: '', priceForHour: 0 };
  }

  startEditWorkplace(wp: WorkPlaceItem): void {
    this.editingWpId = wp.id;
    this.wpForm = { name: wp.name, locationId: wp.locationId, capacity: wp.capacity, description: wp.description, priceForHour: wp.priceForHour };
  }

  cancelWpForm(): void {
    this.wpForm = null;
    this.editingWpId = null;
  }

  saveWorkplace(): void {
    const body = {
      name: this.wpForm.name,
      locationId: this.wpForm.locationId,
      capacity: this.wpForm.capacity,
      description: this.wpForm.description,
      priceForHour: this.wpForm.priceForHour
    };

    if (this.editingWpId) {
      this.http.put(`${this.apiUrl}/workplaces/${this.editingWpId}`, body, { headers: this.getHeaders() })
        .subscribe({ next: () => { this.cancelWpForm(); this.loadWorkplaces(); } });
    } else {
      this.http.post(`${this.apiUrl}/workplaces`, body, { headers: this.getHeaders() })
        .subscribe({ next: () => { this.cancelWpForm(); this.loadWorkplaces(); } });
    }
  }

  deleteWorkplace(id: string): void {
    if (!confirm('Удалить рабочее место?')) return;
    this.http.delete(`${this.apiUrl}/workplaces/${id}`, { headers: this.getHeaders() })
      .subscribe({ next: () => this.loadWorkplaces() });
  }

  toggleAvailability(wp: WorkPlaceItem): void {
    this.http.patch(`${this.apiUrl}/workplaces/${wp.id}/toggle-availability`, {}, { headers: this.getHeaders() })
      .subscribe({ next: () => this.loadWorkplaces() });
  }

  // ============ SERVICES ============

  loadServices(): void {
    this.svcLoading = true;
    this.http.get<ServiceItem[]>(`${this.apiUrl}/services`).subscribe({
      next: (data) => { this.services = data; this.svcLoading = false; },
      error: () => this.svcLoading = false
    });
  }

  startAddService(): void {
    this.editingSvcId = null;
    this.svcForm = { name: '', description: '', price: 0 };
  }

  startEditService(svc: ServiceItem): void {
    this.editingSvcId = svc.id;
    this.svcForm = { name: svc.name, description: svc.description, price: svc.price };
  }

  cancelSvcForm(): void {
    this.svcForm = null;
    this.editingSvcId = null;
  }

  saveService(): void {
    const body = { name: this.svcForm.name, description: this.svcForm.description, price: this.svcForm.price };

    if (this.editingSvcId) {
      this.http.put(`${this.apiUrl}/services/${this.editingSvcId}`, body, { headers: this.getHeaders() })
        .subscribe({ next: () => { this.cancelSvcForm(); this.loadServices(); } });
    } else {
      this.http.post(`${this.apiUrl}/services`, body, { headers: this.getHeaders() })
        .subscribe({ next: () => { this.cancelSvcForm(); this.loadServices(); } });
    }
  }

  deleteService(id: string): void {
    if (!confirm('Удалить услугу?')) return;
    this.http.delete(`${this.apiUrl}/services/${id}`, { headers: this.getHeaders() })
      .subscribe({ next: () => this.loadServices() });
  }

  // ============ BOOKINGS ============

  loadActiveBookings(): void {
    this.bkLoading = true;
    this.http.get<BookingItem[]>(`${this.apiUrl}/bookings/active`, { headers: this.getHeaders() })
      .subscribe({ next: (data) => { this.bookings = data; this.bkLoading = false; }, error: () => this.bkLoading = false });
  }

  loadBookingsByDateRange(): void {
    if (!this.dateStart || !this.dateEnd) return;
    this.bkLoading = true;
    this.http.get<BookingItem[]>(`${this.apiUrl}/bookings/date-range?start=${this.dateStart}&end=${this.dateEnd}`, { headers: this.getHeaders() })
      .subscribe({ next: (data) => { this.bookings = data; this.bkLoading = false; }, error: () => this.bkLoading = false });
  }

  // ============ CONTRACTS ============

  loadPendingContracts(): void {
    this.ctLoading = true;
    this.http.get<ContractItem[]>(`${this.apiUrl}/contracts/pending`, { headers: this.getHeaders() })
      .subscribe({ next: (data) => { this.contracts = data; this.ctLoading = false; }, error: () => this.ctLoading = false });
  }

  // ============ HELPERS ============

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleString('ru-RU', {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  }

  getPaymentStatusLabel(status: string): string {
    const map: { [key: string]: string } = { PAID: 'Оплачен', PENDING: 'Ожидает', CANCELLED: 'Отменён', COMPLETED: 'Завершён', REFUNDED: 'Возвращён' };
    return map[status] || status;
  }
}
