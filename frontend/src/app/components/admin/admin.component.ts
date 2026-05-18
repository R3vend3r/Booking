import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BookingService } from '../../services/booking.service';

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
  bookingId?: string;
}

@Component({
  standalone: false,
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  Math = Math;
  pageSize = 5;

  activeTab: 'admin' | 'managers' | 'bookings' | 'contracts' = 'admin';
  svgW = 600;
  svgH= 300;
  svgPad = { top: 20, right: 30, bottom: 55, left: 70 };
  get chartw(): number { return this.svgW - this.svgPad.right - this.svgPad.left; }
  get chartH(): number { return this.svgH- this.svgPad.top - this.svgPad.bottom; }

  users: any[] = [];
  totalUsers = 0;
  userPageLimit = 10;
  userPageOffset = 0;
  usersLoading = false;
  searchQuery = '';

  showConfirmModal = false;
  selectedUser: any = null;
  confirmAction: 'block' | 'unblock' = 'block';
  confirmLoading = false;

  clientsWithBookings: any[] = [];
  clientPageLimit = 10;
  clientPageOffset = 0;
  hasMoreClients = true;
  clientsLoading = false;


  revenueData: any[] = [];
  revenuePeriod: 'day' | 'week' | 'month' = 'month';
  revenueLoading = false;
  revenuePage = 0;
  revenuePageSize = 5;
  topServices: any[] = [];
  topServicesLoading = false;
  statsSummary: any = null;
  statsLoading = false;


  managers: any[] = [];
  managersLoading = false;
  mgrPage = 0;
  mgrPageSize = 10;
  showManagerModal = false;
  managerForm: any = { login: '', email: '', password: '' };
  editingManagerId: number | null = null;
  managerSaving = false;
  managerFormErrors: any = {};


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
  bkLoading = true;
  showPendingOnly = false;


  contracts: ContractItem[] = [];
  ctPage = 0;
  ctLoading = true;

  selectedBookingDetail: any = null;
  selectedContractDetail: any = null;
  bookingServicesDetail: any[] = [];
  bookingDetailLoading = false;

  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient, private router: Router, private bookingService: BookingService) {}


ngOnInit(): void {
  this.loadUsers();
  this.loadClientsWithBookings()
  this.loadRevenue();
  this.loadTopServices();
  this.loadStatsSummary();
  this.loadAllBookings();
  this.loadAllContracts();
}

  loadUsers(): void {
    this.usersLoading = true;
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';
    const params = `limit=${this.userPageLimit}&offset=${this.userPageOffset}&q=${encodeURIComponent(this.searchQuery)}`;

    this.http.get<any>(`${this.apiUrl}/admin/users/active/paginated?${params}`, {
      headers: { Authorization: `Bearer ${tokenStr}` }
    }).subscribe({
      next: (data) => {
        this.users = data.users;
        this.totalUsers = data.total;
        this.usersLoading = false;
      },
      error: () => {
        this.usersLoading = false;
      }
    });
  }

  onSearch(): void {
    this.userPageOffset = 0;
    this.loadUsers();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.userPageOffset = 0;
    this.loadUsers();
  }

  get hasMoreUsers(): boolean {
    return this.userPageOffset + this.userPageLimit < this.totalUsers;
  }

  get hasPrevUsers(): boolean {
    return this.userPageOffset > 0;
  }

  prevUserPage(): void {
    if (this.hasPrevUsers) {
      this.userPageOffset -= this.userPageLimit;
      this.loadUsers();
    }
  }

  nextUserPage(): void {
    if (this.hasMoreUsers) {
      this.userPageOffset += this.userPageLimit;
      this.loadUsers();
    }
  }

  confirmToggle(user: any): void {
    this.selectedUser = user;
    this.confirmAction = user.enabled ? 'block' : 'unblock';
    this.showConfirmModal = true;
  }

  closeModal(): void {
    this.showConfirmModal = false;
    this.selectedUser = null;
    this.confirmLoading = false;
  }

  executeToggle(): void {
    if (!this.selectedUser) return;
    this.confirmLoading = true;
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';
    const action = this.confirmAction === 'block' ? 'disable' : 'enable';
    const userId = this.selectedUser.id;

    this.http.post(`${this.apiUrl}/admin/users/${userId}/${action}`, {}, {
      headers: { Authorization: `Bearer ${tokenStr}` }
    }).subscribe({
      next: () => {
        this.closeModal();
        this.loadUsers();
      },
      error: () => {
        this.confirmLoading = false;
      }
    });
  }

  loadClientsWithBookings(): void {
    this.clientsLoading = true;
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';

    this.http.get<any[]>(`${this.apiUrl}/admin/clients-with-bookings?limit=${this.clientPageLimit}&offset=${this.clientPageOffset}`, {
      headers: { Authorization: `Bearer ${tokenStr}` }
    }).subscribe({
      next: (data) => {
        this.clientsWithBookings = data;
        this.hasMoreClients = data.length >= this.clientPageLimit;
        this.clientsLoading = false;
      },
      error: () => {
        this.clientsLoading = false;
      }
    });
  }

  get hasPrevClients(): boolean {
    return this.clientPageOffset > 0;
  }

  prevClientPage(): void {
    if (this.hasPrevClients) {
      this.clientPageOffset -= this.clientPageLimit;
      this.loadClientsWithBookings();
    }
  }

  nextClientPage(): void {
    if (this.hasMoreClients) {
      this.clientPageOffset += this.clientPageLimit;
      this.loadClientsWithBookings();
    }
  }

loadRevenue(): void {
    this.revenueLoading = true;
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';
    this.http.get<any[]>(`${this.apiUrl}/admin/reports/revenue/${this.revenuePeriod}`,
        { headers: { Authorization: `Bearer ${tokenStr}` } }
    ).subscribe({
        next: (data) => {
            this.revenueData = [...data].sort((a, b) => a.period.localeCompare(b.period));
            this.revenueLoading = false;
        },
        error: () => {
            this.revenueLoading = false;
        }
    });
}

  setRevenuePeriod (period: 'day' | 'week' | 'month'): void {
    this.revenuePeriod = period;
    this.revenuePage = 0;
    this.loadRevenue();
  }

  prevRevenuePage(): void {
    if (this.revenuePage > 0) this.revenuePage--;
  }

  nextRevenuePage(): void {
    if ((this.revenuePage + 1)* this.revenuePageSize < this.revenueData. length) this.revenuePage++;
  }


  loadTopServices(): void {
    this.topServicesLoading = true;
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';
    this.http.get<any[]>(`${this.apiUrl}/admin/reports/top-services`,
      { headers: { Authorization: `Bearer ${tokenStr}` } }).subscribe({
        next: (data) => {
          this.topServices = data;
          this.topServicesLoading = false;
        },
        error: () => { this.topServicesLoading = false; }
    });
  }


  loadStatsSummary(): void {
    this.statsLoading = true;
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';
    this.http.get<any>(`${this.apiUrl}/admin/reports/summary`, {
        headers: { Authorization: `Bearer ${tokenStr}` }
    }).subscribe({
        next: (data) => {
            this.statsSummary = data;
            this.statsLoading = false;
        },
        error: () => {
            this.statsLoading = false;
        }
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 0 }).format(value);
  }

  formatPeriod(period: string): string {
    if (this.revenuePeriod === 'month') {
        const parts = period.split('-');
        if (parts.length >= 2) {
            const months = ['янв', 'фев', 'мар', 'апр', 'май', 'июн', 'июл', 'авг', 'сен', 'окт', 'ноя', 'дек'];
            return months[parseInt(parts[1]) - 1] + ' ' + parts[0];
        }
    }
    if (this.revenuePeriod === 'week') {
        return 'нед. ' + period;
    }
    return period;
  }

  get maxChartPoints(): number {
    switch (this.revenuePeriod) {
        case 'day': return 30;
        case 'week': return 20;
        case 'month': return 12;
        default: return 12;
    }
  }

  get chartPoints(): any[] {
      return this.revenueData.slice(0, -this.maxChartPoints);
  }

  get paginatedRevenue(): any[] {
      const start = this.revenuePage * this.revenuePageSize;
      const end = (this.revenuePage + 1) * this.revenuePageSize;
      return this.revenueData.slice(start, end);
  }

 get chartData(): { line: string; area: string; yTicks: { y: number; label: string }[]; xLabels: { x: number; label: string }[] } {
    const d = this.revenueData;
    const empty = { line: '', area: '', yTicks: [], xLabels: [] };

    if (!d || d.length < 1) return empty;

    if (d.length === 1) {
        const val = d[0].sum || 0;
        const range = val || 1;
        const { chartw: chartW, chartH: chartH, svgPad: p } = this;
        const cy = p.top + chartH - ((val || 0) / range) * chartH;

        const tickCount = 5;
        const yTicks = Array.from({ length: tickCount }, (_, i) => {
            const v = (range * i) / (tickCount - 1);
            return {
                y: p.top + chartH - (v / range) * chartH,
                label: this.formatCurrency(Math.round(v))
            };
        });

        return {
            line: `M ${p.left} ${cy} L ${p.left + chartW} ${cy}`,
            area: '',
            yTicks,
            xLabels: [{ x: p.left + chartW / 2, label: this.formatPeriod(d[0].period) }]
        };
    }

    const vals = d.map(r => r.sum);
    const maxVal = Math.max(...vals);
    const range = maxVal || 1;
    const { chartw: chartW, chartH: chartH, svgPad: p } = this;

    const pts = d.map((r, i) => ({
        x: p.left + (i / (d.length - 1)) * chartW,
        y: p.top + chartH - (r.sum / range) * chartH
    }));

    let line = `M ${pts[0].x} ${pts[0].y}`;
    for (let i = 1; i < pts.length; i++) {
        const cx = (pts[i - 1].x + pts[i].x) / 2;
        line += ` Q ${cx} ${pts[i - 1].y} ${pts[i].x} ${pts[i].y}`;
    }

    let area = `M ${pts[0].x} ${p.top + chartH} L ${pts[0].x} ${pts[0].y}`;
    for (let i = 1; i < pts.length; i++) {
        const cx = (pts[i - 1].x + pts[i].x) / 2;
        area += ` Q ${cx} ${pts[i - 1].y} ${pts[i].x} ${pts[i].y}`;
    }
    area += ` L ${pts[pts.length - 1].x} ${p.top + chartH} Z`;

    const tickCount = 5;
    const yTicks = Array.from({ length: tickCount }, (_, i) => {
        const val = (range * i) / (tickCount - 1);
        return {
            y: p.top + chartH - (val / range) * chartH,
            label: this.formatCurrency(Math.round(val))
        };
    });

    let xLabels: { x: number; label: string }[];

    if (this.revenuePeriod === 'day' && d.length > 1) {
        const short = (p: string) => p ? p.substring(5) : '';
        xLabels = [
            { x: p.left, label: `${short(d[0].period)}` },
            { x: p.left + chartW / 2, label: `${short(d[Math.floor(d.length / 2)].period)}` },
            { x: p.left + chartW, label: `${short(d[d.length - 1].period)}` }
        ];
    } else {
        const step = Math.max(1, Math.floor(d.length / 5));
        xLabels = d
            .map((r, i) => ({ x: pts[i].x, label: (i % step === 0 || i === d.length - 1) ? this.formatPeriod(r.period) : '' }))
            .filter(lbl => lbl.label);
    }

    return { line, area, yTicks, xLabels };
}


  switchToManagers(): void {
    this.activeTab = 'managers';
    this.mgrPage = 0;
    this.loadManagers();
  }

  get paginatedManagers(): any[] {
    return this.managers.slice(this.mgrPage * this.mgrPageSize, (this.mgrPage + 1) * this.mgrPageSize);
  }

  prevMgrPage(): void {
    if (this.mgrPage > 0) this.mgrPage--;
  }

  nextMgrPage(): void {
    if ((this.mgrPage + 1) * this.mgrPageSize < this.managers.length) this.mgrPage++;
  }

  loadManagers(): void {
    this.managersLoading = true;
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';
    this.http.get<any[]>(`${this.apiUrl}/admin/managers`, {
        headers: { Authorization: `Bearer ${tokenStr}` }
    }).subscribe({
        next: (data) => {
            this.managers = data;
            this.managersLoading = false;
        },
        error: () => {
            this.managersLoading = false;
        }
    });
  }

  openCreateManager(): void {
      this.editingManagerId = null;
      this.managerForm = {
          login: '',
          email: '',
          password: ''
      };
      this.showManagerModal = true;
  }

  openEditManager(mgr: any): void {
      this.editingManagerId = mgr.id;
      this.managerForm = {
          login: mgr.login,
          email: mgr.email || '',
          password: '',
          enabled: mgr.enabled
      };
      this.showManagerModal = true;
  }

  closeManagerModal(): void {
    this.showManagerModal = false;
    this.editingManagerId = null;
    this.managerForm = {
        login: '',
        email: '',
        password: ''
    };
    this.managerSaving = false;
    this.managerFormErrors = {};
}

  validateManagerForm(): boolean {
      this.managerFormErrors = {};

      if (!this.managerForm.login?.trim()) {
          this.managerFormErrors.login = 'Логин обязателен';
      } else if (this.managerForm.login.trim().length < 3) {
          this.managerFormErrors.login = 'Логин должен содержать минимум 3 символа';
      }

      const email = this.managerForm.email;
      if (email && email.length > 0) {
          const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
          if (!emailPattern.test(email)) {
              this.managerFormErrors.email = "Неверный формат email";
          }
      }

      const password = this.managerForm.password;
      if (!this.editingManagerId && (!password || password.length < 8)) {
          this.managerFormErrors.password = "Пароль должен быть не менее 8 символов";
      } else if (password && password.length > 0 && password.length < 8) {
          this.managerFormErrors.password = "Пароль должен быть не менее 8 символов";
      }

      return Object.keys(this.managerFormErrors).length === 0;
  }

  onManagerFormChange(): void {
      this.managerFormErrors = {};
  }

  isManagerFormInvalid(): boolean {
      if (!this.managerForm.login?.trim() || this.managerForm.login.trim().length < 3) return true;
      if (this.managerForm.email?.length > 0) {
          const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
          if (!emailPattern.test(this.managerForm.email)) return true;
      }
      if (!this.editingManagerId && (!this.managerForm.password || this.managerForm.password.length < 8)) return true;
      if (this.managerForm.password?.length > 0 && this.managerForm.password.length < 8) return true;
      return false;
  }

  saveManager(): void {
    if (!this.validateManagerForm()) {
        return;
    }
    this.managerSaving = true;
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';
    const headers = { Authorization: `Bearer ${tokenStr}` };

    if (this.editingManagerId) {
        const body: any = {};
        if (this.managerForm.login) body.login = this.managerForm.login;
        body.email = this.managerForm.email || null;
        if (this.managerForm.password) body.password = this.managerForm.password;
        body.enabled = this.managerForm.enabled;

        this.http.put<any>(`${this.apiUrl}/admin/managers/${this.editingManagerId}`, body, {
            headers
        }).subscribe({
            next: () => {
                this.closeManagerModal();
                this.loadManagers();
            },
            error: () => {
                this.managerSaving = false;
            }
        });
    } else {
        this.http.post<any>(`${this.apiUrl}/admin/managers`, this.managerForm, {
            headers
        }).subscribe({
            next: () => {
                this.closeManagerModal();
                this.loadManagers();
            },
            error: () => {
                this.managerSaving = false;
            }
        });
    }
  }

  deleteManager(id: string): void {
    if (!confirm('Удалить менеджера?')) return;
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';
    this.http.delete(`${this.apiUrl}/admin/managers/${id}`, {
        headers: { Authorization: `Bearer ${tokenStr}` }
    }).subscribe({
        next: () => {
            this.loadManagers();
        },
        error: () => {}
    });
  }

  private getHeaders() {
    const user = localStorage.getItem('currentUser');
    const token = user ? JSON.parse(user).token : '';
    return { Authorization: `Bearer ${token}` };
  }

  loadAllBookings(): void {
    this.bkLoading = true;
    this.bkPage = 0;
    this.showPendingOnly = false;
    this.http.get<any[]>(`${this.apiUrl}/bookings`, {
        headers: this.getHeaders()
    }).subscribe({
        next: (data) => {
            this.bookings = data;
            this.bkLoading = false;
        },
        error: () => {
            this.bkLoading = false;
            this.bookings = [];
        }
    });
  }

  loadActiveBookings(): void {
    this.bkLoading = true;
    this.http.get<any[]>(`${this.apiUrl}/bookings/active`, {
        headers: this.getHeaders()
    }).subscribe({
        next: (data) => {
            this.bookings = data;
            this.bkLoading = false;
        },
        error: () => {
            this.bkLoading = false;
        }
    });
  }

  loadAllContracts(): void {
    this.ctLoading = true;
    this.ctPage = 0;
    this.http.get<any[]>(`${this.apiUrl}/contracts`, {
        headers: this.getHeaders()
    }).subscribe({
        next: (data) => {
            this.contracts = data;
            this.ctLoading = false;
        },
        error: () => {
            this.ctLoading = false;
            this.contracts = [];
        }
    });
  }

  loadPendingContracts(): void {
    this.ctLoading = true;
    this.http.get<any[]>(`${this.apiUrl}/contracts/pending`, {
        headers: this.getHeaders()
    }).subscribe({
        next: (data) => {
            this.contracts = data;
            this.ctLoading = false;
        },
        error: () => {
            this.ctLoading = false;
            this.contracts = [];
        }
    });
  }

  private countWithFilters(extraFilter: (b: any) => boolean, excludeOwn?: string): number {
    return this.bookings.filter(b => {
        if (this.bkSearchId && !b.id.toLowerCase().includes(this.bkSearchId.toLowerCase())) return false;
        if (excludeOwn !== 'city' && this.bkFilterCityTemp.length && (!b.locationCity || !this.bkFilterCityTemp.includes(b.locationCity))) return false;
        if (excludeOwn !== 'location' && this.bkFilterLocationTemp.length && (!b.locationName || !this.bkFilterLocationTemp.includes(b.locationName))) return false;
        if (excludeOwn !== 'workplace' && this.bkFilterWorkplaceTemp.length && (!b.workPlaceName || !this.bkFilterWorkplaceTemp.includes(b.workPlaceName))) return false;
        if (excludeOwn !== 'status' && this.bkFilterStatusTemp.length && (!b.paymentStatus || !this.bkFilterStatusTemp.includes(b.paymentStatus))) return false;
        return extraFilter(b);
    }).length;
  }

  get bookingCityOptions(): { value: string; count: number }[] {
    const cities = [...new Set(this.bookings.map(b => b.locationCity).filter((c): c is string => !!c))];
    return cities.map(city => ({
        value: city,
        count: this.countWithFilters(b => b.locationCity === city, 'city')
    }));
  }

  get bookingLocationOptions(): { value: string; count: number }[] {
    const locs = [...new Set(this.bookings.map(b => b.locationName).filter((l): l is string => !!l))];
    return locs.map(loc => ({
        value: loc,
        count: this.countWithFilters(b => b.locationName === loc, 'location')
    }));
  }

  get bookingWorkplaceOptions(): { value: string; count: number }[] {
    const wps = [...new Set(this.bookings.map(b => b.workPlaceName).filter((w): w is string => !!w))];
    return wps.map(wp => ({
        value: wp,
        count: this.countWithFilters(b => b.workPlaceName === wp, 'workplace')
    }));
  }

  get bookingStatusOptions(): { value: string; count: number }[] {
    const sts = [...new Set(this.bookings.map(b => b.paymentStatus).filter((s): s is string => !!s))];
    return sts.map(st => ({
        value: st,
        count: this.countWithFilters(b => b.paymentStatus === st, 'status')
    }));
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
    this.showPendingOnly = false;
    this.loadAllBookings();
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
        result = result.filter(b => b.id.toLowerCase().includes(this.bkSearchId.toLowerCase()));
    }
    if (this.bkFilterCity.length) {
        result = result.filter(b => b.locationCity && this.bkFilterCity.includes(b.locationCity));
    }
    if (this.bkFilterLocation.length) {
        result = result.filter(b => b.locationName && this.bkFilterLocation.includes(b.locationName));
    }
    if (this.bkFilterWorkplace.length) {
        result = result.filter(b => b.workPlaceName && this.bkFilterWorkplace.includes(b.workPlaceName));
    }
    if (this.bkFilterStatus.length) {
        result = result.filter(b => b.paymentStatus && this.bkFilterStatus.includes(b.paymentStatus));
    }
    return result;
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

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleString('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
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

  prevBkPage(): void {
      if (this.bkPage > 0) this.bkPage--;
  }

  nextBkPage(): void {
      if ((this.bkPage + 1) * this.pageSize < this.filteredBookings.length) this.bkPage++;
  }

  prevCtPage(): void {
      if (this.ctPage > 0) this.ctPage--;
  }

  nextCtPage(): void {
      if ((this.ctPage + 1) * this.pageSize < this.contracts.length) {
          this.ctPage++;
      }
  }

  logout(): void {
      localStorage.removeItem('currentUser');
      this.router.navigate(['/']);
  }
}
