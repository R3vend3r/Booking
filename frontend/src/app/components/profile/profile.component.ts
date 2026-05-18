import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { BookingService } from '../../services/booking.service';
import { Booking } from '../../models/booking.model';
import { HttpClient } from '@angular/common/http';

@Component({
  standalone: false,
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  Math = Math;
  profile: any = null;
  bookings: Booking[] = [];
  loading = true;
  bkPage = 0;
  bkPageSize = 5;
  workplacePricesMap: Map<string, number> = new Map();
  bookingServicesTotal: Map<string, number> = new Map();
  editForm = { email: '', fullName: '', phone: '', birthday: '' };
  todayDate = new Date().toISOString().split('T')[0];
  profileError = '';
  profileSuccess = '';
  profileLoading = false;
  pwForm = { oldPassword: '', newPassword: '', confirmPassword: '' };
  pwError = '';
  pwSuccess = '';
  pwLoading = false;
  private apiUrl = 'http://localhost:8080/api';

  constructor(
    private authService: AuthService,
    private bookingService: BookingService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.http.get<any>(`${this.apiUrl}/auth/profile`, {
      headers: { Authorization: `Bearer ${this.authService.getToken()}` }
    }).subscribe({
      next: (data) => {
        this.profile = data;
        this.editForm = {
          email: data.email || '',
          fullName: data.fullName || '',
          phone: data.phone || '',
          birthday: data.birthday || ''
        };
        this.loadBookings();
      },
      error: () => {
        const stored = localStorage.getItem('currentUser');
        this.profile = stored ? JSON.parse(stored) : null;
        this.loadBookings();
      }
    });
  }

  loadBookings(): void {
    const role = this.profile?.role;
    if (role === 'ROLE_USER') {
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
    } else {
      this.loading = false;
    }
  }

  validateProfileForm(): boolean {
    this.profileError = '';

    if (!this.editForm.email?.trim()) {
      this.profileError = 'Email обязателен';
      return false;
    }

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailPattern.test(this.editForm.email.trim())) {
      this.profileError = 'Неверный формат email';
      return false;
    }

    if (this.editForm.fullName?.trim() && this.editForm.fullName.trim().length < 2) {
      this.profileError = 'ФИО должно содержать минимум 2 символа';
      return false;
    }

    if (this.editForm.phone?.trim()) {
      const phoneClean = this.editForm.phone.replace(/[\s\-()]/g, '');
      if (!/^\+?[0-9]{10,15}$/.test(phoneClean)) {
        this.profileError = 'Неверный формат телефона';
        return false;
      }
    }

    if (this.editForm.birthday) {
      const bd = new Date(this.editForm.birthday);
      if (bd >= new Date()) {
        this.profileError = 'Дата рождения не может быть в будущем';
        return false;
      }
    }

    return true;
  }

  onProfileFormChange(): void {
    this.profileError = '';
  }

  saveProfile(): void {
    if (!this.validateProfileForm()) return;

    this.profileSuccess = '';
    this.profileLoading = true;

    this.http.put(`${this.apiUrl}/auth/profile`, {
      email: this.editForm.email,
      fullName: this.editForm.fullName,
      phone: this.editForm.phone,
      birthday: this.editForm.birthday
    }, {
      headers: { Authorization: `Bearer ${this.authService.getToken()}` }
    }).subscribe({
      next: () => {
        this.profileSuccess = 'Профиль обновлён';
        this.profileLoading = false;
        this.loadProfile();
      },
      error: (err) => {
        this.profileError = err.error?.message || 'Ошибка при сохранении';
        this.profileLoading = false;
      }
    });
  }

  validatePasswordForm(): boolean {
    this.pwError = '';

    if (!this.pwForm.oldPassword) {
      this.pwError = 'Введите старый пароль';
      return false;
    }

    if (!this.pwForm.newPassword || this.pwForm.newPassword.length < 6) {
      this.pwError = 'Новый пароль должен быть не менее 6 символов';
      return false;
    }

    if (this.pwForm.newPassword !== this.pwForm.confirmPassword) {
      this.pwError = 'Новые пароли не совпадают';
      return false;
    }

    return true;
  }

  onPasswordFormChange(): void {
    this.pwError = '';
  }

  changePassword(): void {
    if (!this.validatePasswordForm()) return;

    this.pwSuccess = '';
    this.pwLoading = true;

    this.http.post(`${this.apiUrl}/auth/change-password`, {
      oldPassword: this.pwForm.oldPassword,
      newPassword: this.pwForm.newPassword
    }, {
      headers: { Authorization: `Bearer ${this.authService.getToken()}` }
    }).subscribe({
      next: () => {
        this.pwSuccess = 'Пароль успешно изменён';
        this.pwForm = { oldPassword: '', newPassword: '', confirmPassword: '' };
        this.pwLoading = false;
      },
      error: (err) => {
        this.pwError = err.error?.message || 'Ошибка при смене пароля';
        this.pwLoading = false;
      }
    });
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

  totalBookingPrice(booking: Booking): number {
    const start = new Date(booking.startTime);
    const end = new Date(booking.endTime);
    const hours = Math.max(1, Math.round((end.getTime() - start.getTime()) / 3600000));
    const wpPrice = (this.workplacePricesMap.get(booking.workPlaceId) || 0) * hours;
    const servicesPrice = this.bookingServicesTotal.get(booking.id) || 0;
    return wpPrice + servicesPrice;
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleString('ru-RU', {
      day: '2-digit', month: '2-digit', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  }

  getRoleName(): string {
    const roleMap: { [key: string]: string } = {
      'ROLE_ADMIN': 'Администратор',
      'ROLE_MANAGER': 'Менеджер',
      'ROLE_USER': 'Клиент'
    };
    return roleMap[this.profile?.role] || this.profile?.role;
  }

  getRoleColor(): string {
    const colorMap: { [key: string]: string } = {
      'ROLE_ADMIN': '#ef4444',
      'ROLE_MANAGER': '#f59e0b',
      'ROLE_USER': '#22c55e'
    };
    return colorMap[this.profile?.role] || '#64748b';
  }

  get paginatedProfileBookings(): Booking[] {
    return this.bookings.slice(this.bkPage * this.bkPageSize, (this.bkPage + 1)* this.bkPageSize);
  }

  prevBkPage(): void {
    if (this.bkPage > 0) this.bkPage--;
  }
   nextBkPage(): void {
    if ((this.bkPage + 1) * this.bkPageSize< this. bookings.length)
      this.bkPage++;
    }
}
