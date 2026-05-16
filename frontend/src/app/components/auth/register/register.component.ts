import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { RegistrationRequest } from '../../../models/auth.model';

@Component({
  standalone: false,
  selector: 'app-register',
  template: `
    <div class="container" style="max-width: 400px; padding-top: 40px;">
      <div class="card">
        <h2 style="margin-bottom: 24px; text-align: center;">Регистрация</h2>
        
        <form (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Логин</label>
            <input type="text" [(ngModel)]="request.login" name="login" required minlength="3" maxlength="50" />
          </div>
          
          <div class="form-group">
            <label>Пароль</label>
            <input type="password" [(ngModel)]="request.password" name="password" required minlength="6" />
          </div>
          
          <div class="form-group">
            <label>Email</label>
            <input type="email" [(ngModel)]="request.email" name="email" required />
          </div>
          
          <div class="form-group">
            <label>ФИО</label>
            <input type="text" [(ngModel)]="request.fullName" name="fullName" required />
          </div>
          
          <div class="form-group">
            <label>Телефон</label>
            <input type="tel" [(ngModel)]="request.phone" name="phone" pattern="\\+?[0-9]{10,15}" />
          </div>
          
          <div class="form-group">
            <label>Дата рождения</label>
            <input type="date" [(ngModel)]="request.birthday" name="birthday" />
          </div>
          
          <div *ngIf="error" class="error" style="margin-bottom: 16px;">
            {{ error }}
          </div>
          
          <button type="submit" class="btn btn-primary" style="width: 100%;" [disabled]="loading">
            {{ loading ? 'Регистрация...' : 'Зарегистрироваться' }}
          </button>
        </form>
        
        <p style="text-align: center; margin-top: 16px;">
          Уже есть аккаунт? <a routerLink="/login">Войти</a>
        </p>
      </div>
    </div>
  `
})
export class RegisterComponent {
  request: RegistrationRequest = {
    login: '',
    password: '',
    email: '',
    fullName: '',
    phone: '',
    birthday: ''
  };
  error = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.loading = true;
    this.error = '';

    this.authService.register(this.request).subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Ошибка регистрации';
        this.loading = false;
      }
    });
  }
}
