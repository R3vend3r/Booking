import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BookingService } from '../../services/booking.service';

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
  clientName?: string;
  workPlaceId: string;
  workPlaceName?: string;
  locationName?: string;
  locationCity?: string;
  paymentStatus?: string;
  contractId?: string;
  totalAmount?: number;
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
  templateUrl: './manager.component.html',
  styleUrls: ['./manager.component.css']
})
export class ManagerComponent implements OnInit {
  Math = Math;
  pageSize = 5;
  private apiUrl = 'http://localhost:8080/api';
  activeTab = 'locations';

  locations: LocationItem[] = [];
  locPage = 0;
  locFilterCity = '';
  locationsLoading = true;
  locationForm: any = null;
  locationFormErrors: any = {};
  editingLocationId: string | null = null;

  workplaces: WorkPlaceItem[] = [];
  wpPage = 0;
  wpLoading = true;
  wpForm: any = null;
  wpFormErrors: any = {};
  editingWpId: string | null = null;
  wpFilterLocation = '';

  services: ServiceItem[] = [];
  svcPage = 0;
  svcLoading = true;
  svcForm: any = null;
  svcFormErrors: any = {};
  editingSvcId: string | null = null;

 bookings: BookingItem[] = [];
bkPage = 0;
bkSearchId = '';
bkShowFilters = false;
bkFilterCity: string[] = [];
bkFilterLocation: string[] = [];
bkFilterWorkplace: string[] = [];
bkFilterStatus: string[] = [];
bkFilterCityTemp: string[] = [];
bkFilterLocationTemp: string[] = [];
bkFilterWorkplaceTemp: string[] = [];
bkFilterStatusTemp: string[] = [];
pendingBookings: BookingItem[] = [];
bkLoading = true;
dateStart = '';
dateEnd = '';
showPendingOnly = false;

contracts: ContractItem[] = [];
ctPage = 0;
ctLoading = true;

selectedBookingDetail: any = null;
selectedContractDetail: any = null;
bookingServicesDetail: any[] = [];
bookingDetailLoading = false;


  constructor(
    private http: HttpClient,
    private bookingService: BookingService
  ) {}

 ngOnInit(): void {
  this.loadLocations();
  this.loadServices();
  this.loadWorkplaces();
  this.loadAllBookings();
  this.loadAllContracts();
}

  get locationCities(): string[] {
    return [...new Set(this.locations.map(l => l.city).filter(c => c))];
  }

  get filteredLocations(): LocationItem[] {
    return this.locFilterCity ? this.locations.filter(l => l.city === this.locFilterCity) : this.locations;
  }

  onLocFilterChange(): void {
    this.locPage = 0;
  }

  private countWithFilters(extraFilter: (b: BookingItem) => boolean,
    excludeOwn?: string): number {
    return this.bookings.filter(b => {
      if (this.bkSearchId &&
        !b.id.toLowerCase().includes(this.bkSearchId.toLowerCase())) return false;

      if (this.bkFilterCityTemp.length && (!b.locationCity ||
        !this.bkFilterCityTemp.includes(b.locationCity))) return false;

      if (this.bkFilterLocationTemp.length && (!b.locationName ||
        !this.bkFilterLocationTemp.includes(b.locationName))) return false;

      if (this.bkFilterWorkplaceTemp.length && (!b.workPlaceName ||
        !this.bkFilterWorkplaceTemp.includes(b.workPlaceName))) return false;

      if (this.bkFilterStatusTemp.length && (!b.paymentStatus ||
        !this.bkFilterStatusTemp.includes(b.paymentStatus))) return false;

      if (excludeOwn !== 'city' && this.bkFilterCityTemp.length && b.locationCity &&
        !this.bkFilterCityTemp.includes(b.locationCity)) return false;

      if (excludeOwn !== 'location' && this.bkFilterLocationTemp.length && b.locationName &&
        !this.bkFilterLocationTemp.includes(b.locationName)) return false;

      if (excludeOwn !== 'workplace' && this.bkFilterWorkplaceTemp.length && b.workPlaceName &&
        !this.bkFilterWorkplaceTemp.includes(b.workPlaceName)) return false;

      if (excludeOwn !== 'status' && this.bkFilterStatusTemp.length && b.paymentStatus &&
        !this.bkFilterStatusTemp.includes(b.paymentStatus)) return false;

      return extraFilter(b);
    }).length;
  }


  get bookingCityOptions(): {value: string, count: number}[] {
    const cities = [...new Set(this.bookings.map(b => b.locationCity).filter((c): c is string => !!c))];
    return cities.map(city => ({value: city, count: this.countWithFilters(b => b.locationCity === city)}));
  }

  get bookingLocationOptions(): {value: string, count: number}[] {
    const locs = [...new Set(this.bookings.map(b => b.locationName).filter((l): l is string => !!l))];
    return locs.map(loc => ({value: loc, count: this.countWithFilters(b => b.locationName === loc)}));
  }

  get bookingWorkplaceOptions(): {value: string, count: number}[] {
    const wps = [...new Set(this.bookings.map(b => b.workPlaceName).filter((w): w is string => !!w))];
    return wps.map(wp => ({value: wp, count: this.countWithFilters(b => b.workPlaceName === wp)}));
  }


  get bookingStatusOptions(): {value: string, count: number}[] {
    const sts = [...new Set(this.bookings.map(b =>
    b.paymentStatus).filter((s): s is string => !!s))];
    return sts.map(st => ({value: st, count: this.countWithFilters(b => b.paymentStatus === st)}));
  }

  toggleFilter(arr: string[], val: string): string[] {
    return arr.includes(val) ? arr.filter(v => v !== val) : [...arr, val];
  }

resetBkFilters(): void {
  this.bkFilterCityTemp = [];
  this.bkFilterLocationTemp = [];
  this.bkFilterWorkplaceTemp = [];
  this.bkFilterStatusTemp = [];
  this.bkSearchId = '';
}


  applyBkFilters(): void {
    this.bkFilterCity = [...this.bkFilterCityTemp];
    this.bkFilterLocation = [...this.bkFilterLocationTemp];
    this.bkFilterWorkplace = [...this.bkFilterWorkplaceTemp];
    this.bkFilterStatus = [...this.bkFilterStatusTemp];
    this.bkPage = 0;
    this.bkShowFilters = false;
  }

  refreshBookings(): void {
    this.bkFilterCity = [];
    this.bkFilterLocation = [];
    this.bkFilterWorkplace = [];
    this.bkFilterStatus = [];
    this.bkFilterCityTemp = [];
    this.bkFilterLocationTemp = [];
    this.bkFilterWorkplaceTemp = [];
    this.bkFilterStatusTemp = [];
    this.bkSearchId = '';
    this.bkPage = 0;
    this.showPendingOnly ? this.loadActiveBookings() : this.loadAllBookings();
  }

  toggleBkFilters(): void {
    if (!this.bkShowFilters) {
      this.bkFilterCityTemp = [...this.bkFilterCity];
      this.bkFilterLocationTemp = [...this.bkFilterLocation];
      this.bkFilterWorkplaceTemp = [...this.bkFilterWorkplace];
      this.bkFilterStatusTemp = [...this.bkFilterStatus];
    }
    this.bkShowFilters = !this.bkShowFilters;
  }

  onBkFilterChange(category: string, val: string): void {
    if (category === 'city') this.bkFilterCityTemp = this.toggleFilter(this.bkFilterCityTemp, val);
    if (category === 'location') this.bkFilterLocationTemp = this.toggleFilter(this.bkFilterLocationTemp, val);
    if (category === 'workplace') this.bkFilterWorkplaceTemp = this.toggleFilter(this.bkFilterWorkplaceTemp, val);
    if (category === 'status') this.bkFilterStatusTemp = this.toggleFilter(this.bkFilterStatusTemp, val);
  }

  get filteredBookings(): BookingItem[] {
    let result = this.bookings;

    if (this.bkSearchId) {
      result = result.filter(b =>
        b.id.toLowerCase().includes(this.bkSearchId.toLowerCase())
      );
    }

    if (this.bkFilterCity.length) {
      result = result.filter(b => !!b.locationCity &&
        this.bkFilterCity.includes(b.locationCity)
      );
    }

    if (this.bkFilterLocation.length) {
      result = result.filter(b => !!b.locationName &&
        this.bkFilterLocation.includes(b.locationName)
      );
    }

    if (this.bkFilterWorkplace.length) {
      result = result.filter(b => !!b.workPlaceName &&
        this.bkFilterWorkplace.includes(b.workPlaceName)
      );
    }

    if (this.bkFilterStatus.length) {
      result = result.filter(b => !!b.paymentStatus &&
        this.bkFilterStatus.includes(b.paymentStatus)
      );
    }

    return result;
  }


  private getHeaders() {
    const user = localStorage.getItem('currentUser');
    const token = user ? JSON.parse(user).token : '';
    return { Authorization: `Bearer ${token}` };
  }

  loadLocations(): void {
    this.locationsLoading = true;
    this.locPage = 0;
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

validateLocationForm(): boolean {
  this.locationFormErrors = {};

  if (!this.locationForm.name?.trim()) {
    this.locationFormErrors.name = 'Название обязательно';
  } else if (this.locationForm.name.trim().length < 2) {
    this.locationFormErrors.name = 'Название должно содержать минимум 2 символа';
  }

  if (!this.locationForm.address?.trim()) {
    this.locationFormErrors.address = 'Адрес обязателен';
  } else if (this.locationForm.address.trim().length < 5) {
    this.locationFormErrors.address = 'Адрес должен содержать минимум 5 символов';
  }

  if (!this.locationForm.city?.trim()) {
    this.locationFormErrors.city = 'Город обязателен';
  }

  const phone = this.locationForm.contactPhone;

  if (!phone?.trim()) {
    this.locationFormErrors.contactPhone = 'Телефон обязателен';
  } else if (!/^\+?[0-9\s\-()]{10,18}$/.test(phone.trim())) {
    this.locationFormErrors.contactPhone = 'Неверный формат телефона';
  }

  const open = this.locationForm.openingTime;
  const close = this.locationForm.closingTime;

  if (open && close && open >= close) {
    this.locationFormErrors.closingTime = 'Время открытия должно быть раньше времени закрытия';
  }

  return Object.keys(this.locationFormErrors).length === 0;
}

  onLocFormChange(): void {
    this.locationFormErrors = {};
  }

  validateWorkplaceForm(): boolean {
    this.wpFormErrors = {};

    if (!this.wpForm.name?.trim()) {
      this.wpFormErrors.name = 'Название обязательно';
    } else if (this.wpForm.name.trim().length < 2) {
      this.wpFormErrors.name = 'Название должно содержать минимум 2 символа';
    }

    if (!this.wpForm.locationId) {
      this.wpFormErrors.locationId = 'Выберите локацию';
    }

    if (!this.wpForm.capacity || this.wpForm.capacity < 1) {
      this.wpFormErrors.capacity = 'Вместимость должна быть не менее 1';
    }

    if (!this.wpForm.description?.trim()) {
      this.wpFormErrors.description = 'Описание обязательно';
    }

    if (this.wpForm.priceForHour === null ||
      this.wpForm.priceForHour === undefined ||
      this.wpForm.priceForHour < 0) {
      this.wpFormErrors.priceForHour = 'Цена не может быть отрицательной';
    } else if (this.wpForm.priceForHour === 0) {
      this.wpFormErrors.priceForHour = 'Цена должна быть больше 0';
    }

    return Object.keys(this.wpFormErrors).length === 0;
  }

  onWpFormChange(): void {
    this.wpFormErrors = {};
  }

  validateServiceForm(): boolean {
  this.svcFormErrors = {};

  if (!this.svcForm.name?.trim()) {
    this.svcFormErrors.name = 'Название обязательно';
  } else if (this.svcForm.name.trim().length < 2) {
    this.svcFormErrors.name = 'Название должно содержать минимум 2 символа';
  }

  if (!this.svcForm.description?.trim()) {
    this.svcFormErrors.description = 'Описание обязательно';
  }

  if (this.svcForm.price === null ||
      this.svcForm.price === undefined ||
      this.svcForm.price < 0) {
    this.svcFormErrors.price = 'Цена не может быть отрицательной';
  } else if (this.svcForm.price === 0) {
    this.svcFormErrors.price = 'Цена должна быть больше 0';
  }

  return Object.keys(this.svcFormErrors).length === 0;
}

  onSvcFormChange(): void {
    this.svcFormErrors = {};
  }


  saveLocation(): void {
    if (!this.validateLocationForm()) return;
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
      error: (err) => {
        console.error('Ошибка загрузки рабочих мест:', err);
        this.wpLoading = false;
      }
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
    this.http.patch<WorkPlaceItem>(`${this.apiUrl}/workplaces/${wp.id}/toggle-availability`, {}, { headers: this.getHeaders() })
      .subscribe({
        next: () => this.loadWorkplaces(),
        error: (err) => {
          console.error('Ошибка переключения:', err);
          alert('Не удалось изменить статус рабочего места');
        }
      });
  }

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

  loadAllBookings(): void {
  this.bkLoading = true;
  this.bkPage = 0;
  this.showPendingOnly = false;
  this.http.get<BookingItem[]>(`${this.apiUrl}/bookings`, {
    headers: this.getHeaders()
  }).subscribe({
    next: (data) => {
      this.bookings = data;
      this.bkLoading = false;
    },
    error: (err) => {
      console.error('Ошибка загрузки бронирований:', err);
      this.bkLoading = false;
      this.bookings = [];
    }
  });
}

  loadActiveBookings(): void {
    this.bkLoading = true;
    this.bkPage = 0;
    this.showPendingOnly = false;
    this.http.get<BookingItem[]>(`${this.apiUrl}/bookings/active`, { headers: this.getHeaders() })
      .subscribe({ next: (data) => { this.bookings = data; this.bkLoading = false; }, error: () => this.bkLoading = false });
  }

  loadBookingsByDateRange(): void {
    if (!this.dateStart || !this.dateEnd) return;
    this.bkLoading = true;
    this.showPendingOnly = false;
    this.http.get<BookingItem[]>(`${this.apiUrl}/bookings/date-range?start=${this.dateStart}&end=${this.dateEnd}`, { headers: this.getHeaders() })
      .subscribe({ next: (data) => { this.bookings = data; this.bkLoading = false; }, error: () => this.bkLoading = false });
  }

  loadAllContracts(): void {
  this.ctLoading = true;
  this.http.get<ContractItem[]>(`${this.apiUrl}/contracts`, {
    headers: this.getHeaders()
  }).subscribe({
    next: (data) => {
      this.contracts = data;
      this.ctLoading = false;
    },
    error: (err) => {
      console.error('Ошибка загрузки контрактов:', err);
      this.ctLoading = false;
      this.contracts = [];
    }
  });
}

  loadPendingContracts(): void {
    this.ctLoading = true;
    this.http.get<ContractItem[]>(`${this.apiUrl}/contracts/pending`, {
      headers: this.getHeaders()
    }).subscribe({
      next: (data) => {
        this.contracts = data;
        this.ctLoading = false;
      },
      error: (err) => {
        console.error('Ошибка загрузки ожидающих контрактов:', err);
        this.ctLoading = false;
        this.contracts = [];
      }
    });
  }
  openBookingDetails(bk: any): void {
    this.bookingDetailLoading = true;
    this.bookingService.getBookingWithServices(bk.id).subscribe({
      next: (data) => {
        this.selectedBookingDetail = data;
        this.bookingServicesDetail = data.services || [];
        this.bookingDetailLoading = false;
      },
      error: () => {
        this.selectedBookingDetail = bk;
        this.bookingServicesDetail = [];
        this.bookingDetailLoading = false;
      }
    });
  }

  closeBookingDetails(): void {
    this.selectedBookingDetail = null;
    this.bookingServicesDetail = [];
  }

  openContractDetails(ct: any): void {
    this.selectedContractDetail = ct;
  }

  closeContractDetails(): void {
    this.selectedContractDetail = null;
  }

  prevPage(tab: string): void {
  if (tab === 'locations' && this.locPage > 0) this.locPage--;
  if (tab === 'workplaces' && this.wpPage > 0) this.wpPage--;
  if (tab === 'services' && this.svcPage > 0) this.svcPage--;
  if (tab === 'bookings' && this.bkPage > 0) this.bkPage--;
  if (tab === 'contracts' && this.ctPage > 0) this.ctPage--;
}

nextPage(tab: string): void {
  if (tab === 'locations' && (this.locPage + 1) * this.pageSize < this.filteredLocations.length) this.locPage++;
  if (tab === 'workplaces' && (this.wpPage + 1) * this.pageSize < this.workplaces.length) this.wpPage++;
  if (tab === 'services' && (this.svcPage + 1) * this.pageSize < this.services.length) this.svcPage++;
  if (tab === 'bookings' && (this.bkPage + 1) * this.pageSize < this.filteredBookings.length) this.bkPage++;
  if (tab === 'contracts' && (this.ctPage + 1) * this.pageSize < this.contracts.length) this.ctPage++;
}


  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleString('ru-RU', {
      day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  }

  getPaymentStatusLabel(status: string): string {
    const map: { [key: string]: string } = {
      PAID: 'Оплачен',
      PENDING: 'Ожидает оплаты',
      CANCELLED: 'Отменён',
      COMPLETED: 'Завершён',
      REFUNDED: 'Возвращён',
      APPROVED: 'Одобрен'
    };
    return map[status] || status;
  }

  getBookingStatusLabel(paymentStatus?: string, endTime?: string): string {
    if (paymentStatus === 'CANCELLED') return 'Отменено';
    if (paymentStatus === 'PAID') {
        if (endTime && new Date(endTime) < new Date()) return 'Завершено';
        return 'Оплачено';
    }
    if (paymentStatus === 'PENDING') {
        if (endTime && new Date(endTime) < new Date()) return 'Просрочено';
        return 'Ожидает оплаты';
    }
    return paymentStatus || 'Новое';
}

  getBookingCardClass(paymentStatus?: string): string {
    if (paymentStatus === 'PAID') return 'booking-paid';
    if (paymentStatus === 'CANCELLED') return 'booking-cancelled';
    if (paymentStatus === 'APPROVED') return 'booking-approved';
    return 'booking-pending';
  }
}