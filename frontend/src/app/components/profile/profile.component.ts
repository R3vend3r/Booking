import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { BookingService } from '../../services/booking.service';
import { Booking } from '../../models/booking.model';
import { HttpClient } from '@angular/common/http';

@Component({
  standalone: false,
  selector: 'app-profile',
  template: `
    <div class="container" style="max-width: 700px; padding-top: 32px;">
      <h1 style="margin-bottom: 24px;">Профиль</h1>
      
      <div class="card">
        <div style="text-align: center; margin-bottom: 24px;">
          <div style="width: 80px; height: 80px; background: var(--primary); border-radius: 50%; 
                      display: flex; align-items: center; justify-content: center; margin: 0 auto;">
            <span style="color: white; font-size: 32px; font-weight: 600;">
              {{ profile?.login?.charAt(0)?.toUpperCase() }}
            </span>
          </div>
        </div>
        
        <div style="display: grid; gap: 16px;">
          <div style="display: flex; justify-content: space-between; padding: 12px; background: #f8fafc; border-radius: 8px;">
            <span style="color: #64748b;">Логин</span>
            <span>{{ profile?.login }}</span>
          </div>
          
          <div style="display: flex; justify-content: space-between; padding: 12px; background: #f8fafc; border-radius: 8px;">
            <span style="color: #64748b;">Роль</span>
            <span [style.color]="getRoleColor()">{{ getRoleName() }}</span>
          </div>
        </div>
      </div>

      <div class="card" style="margin-top: 24px;">
        <h2 style="margin-bottom: 20px;">Редактирование профиля</h2>
        
        <form (ngSubmit)="saveProfile()">
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="editForm.email" name="email" required />
          </div>

          <div class="form-group" *ngIf="profile?.role === 'ROLE_USER'">
            <label>ФИО</label>
            <input type="text" [(ngModel)]="editForm.fullName" name="fullName" />
          </div>

          <div class="form-group" *ngIf="profile?.role === 'ROLE_USER'">
            <label>Телефон</label>
            <input type="tel" [(ngModel)]="editForm.phone" name="phone" />
          </div>

          <div class="form-group" *ngIf="profile?.role === 'ROLE_USER'">
            <label>Дата рождения</label>
            <input type="date" [(ngModel)]="editForm.birthday" name="birthday" />
          </div>
          
          <div *ngIf="profileError" class="error" style="margin-bottom: 16px;">
            {{ profileError }}
          </div>
          
          <div *ngIf="profileSuccess" style="margin-bottom: 16px; color: #22c55e; font-size: 14px;">
            {{ profileSuccess }}
          </div>
          
          <button type="submit" class="btn btn-primary" style="width: 100%;" [disabled]="profileLoading">
            {{ profileLoading ? 'Сохранение...' : 'Сохранить изменения' }}
          </button>
        </form>
      </div>

      <div class="card" style="margin-top: 24px;">
        <h2 style="margin-bottom: 20px;">Смена пароля</h2>
        
        <form (ngSubmit)="changePassword()">
          <div class="form-group">
            <label>Старый пароль</label>
            <input type="password" [(ngModel)]="pwForm.oldPassword" name="oldPassword" required />
          </div>
          
          <div class="form-group">
            <label>Новый пароль</label>
            <input type="password" [(ngModel)]="pwForm.newPassword" name="newPassword" required minlength="6" />
          </div>
          
          <div class="form-group">
            <label>Повторите новый пароль</label>
            <input type="password" [(ngModel)]="pwForm.confirmPassword" name="confirmPassword" required />
          </div>
          
          <div *ngIf="pwError" class="error" style="margin-bottom: 16px;">
            {{ pwError }}
          </div>
          
          <div *ngIf="pwSuccess" style="margin-bottom: 16px; color: #22c55e; font-size: 14px;">
            {{ pwSuccess }}
          </div>
          
          <button type="submit" class="btn btn-primary" style="width: 100%;" [disabled]="pwLoading">
            {{ pwLoading ? 'Сохранение...' : 'Сменить пароль' }}
          </button>
        </form>
      </div>

      <div class="card" style="margin-top: 24px;" *ngIf="profile?.role !== 'ROLE_MANAGER' && profile?.role !== 'ROLE_ADMIN'">
        <h2 style="margin-bottom: 20px;">Мои бронирования</h2>
        
        <div *ngIf="loading" class="loading">
          <p>Загрузка...</p>
        </div>
        
        <div *ngIf="!loading && bookings.length === 0" style="text-align: center; padding: 40px;">
          <p style="color: #64748b;">На данный момент у вас нет бронирований</p>
        </div>
        
        <div *ngIf="!loading && bookings.length > 0" style="display: flex; flex-direction: column; gap: 12px;">
          <div *ngFor="let booking of bookings" class="card" style="background: #f8fafc;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
              <div>
                <p style="font-weight: 600; margin-bottom: 4px;">{{ booking.workPlaceName }}</p>
                <p style="color: #64748b; font-size: 13px; margin-bottom: 8px;">
                  {{ booking.locationName }}, {{ booking.locationAddress }}
                </p>
                <p style="color: #64748b; font-size: 14px;">
                  {{ formatDate(booking.startTime) }} — {{ formatDate(booking.endTime) }}
                </p>
              </div>
              <div style="text-align: right; white-space: nowrap;">
                <span style="font-weight: 600; font-size: 16px; color: var(--primary);">{{ booking.totalAmount || 0 }} ₽</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ProfileComponent implements OnInit {
  profile: any = null;
  bookings: Booking[] = [];
  loading = true;
  editForm = { email: '', fullName: '', phone: '', birthday: '' };
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
      this.bookingService.getMyBookings().subscribe({
        next: (data) => { this.bookings = data; this.loading = false; },
        error: () => { this.loading = false; }
      });
    } else {
      this.loading = false;
    }
  }

  saveProfile(): void {
    this.profileError = '';
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

  changePassword(): void {
    this.pwError = '';
    this.pwSuccess = '';

    if (this.pwForm.newPassword !== this.pwForm.confirmPassword) {
      this.pwError = 'Новые пароли не совпадают';
      return;
    }

    if (this.pwForm.newPassword.length < 6) {
      this.pwError = 'Новый пароль должен быть не менее 6 символов';
      return;
    }

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
}
