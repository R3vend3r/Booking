import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { LoginRequest } from '../../../models/auth.model';

@Component({
  standalone: false,
  selector: 'app-login',
  template: `
    <div class="container" style="max-width: 400px; padding-top: 60px;">
      <div class="card">
        <h2 style="margin-bottom: 24px; text-align: center;">Вход</h2>
        
        <form (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Логин</label>
            <input type="text" [(ngModel)]="request.login" name="login" required />
          </div>
          
          <div class="form-group">
            <label>Пароль</label>
            <input type="password" [(ngModel)]="request.password" name="password" required />
          </div>
          
          <div *ngIf="error" class="error" style="margin-bottom: 16px;">
            {{ error }}
          </div>
          
          <button type="submit" class="btn btn-primary" style="width: 100%;" [disabled]="loading">
            {{ loading ? 'Вход...' : 'Войти' }}
          </button>
        </form>
        
        <p style="text-align: center; margin-top: 16px;">
          Нет аккаунта? <a routerLink="/register">Зарегистрироваться</a>
        </p>
      </div>
    </div>
  `
})
export class LoginComponent {
  request: LoginRequest = { login: '', password: '' };
  error = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.loading = true;
    this.error = '';

    this.authService.login(this.request).subscribe({
      next: (response) => {
        if (response.role === 'ROLE_ADMIN') {
          this.router.navigate(['/admin']);
        } else if (response.role === 'ROLE_MANAGER') {
          this.router.navigate(['/manager']);
        } else {
          this.router.navigate(['/']);
        }
      },
      error: (err) => {
        this.error = err.error?.message || 'Ошибка входа';
        this.loading = false;
      }
    });
  }
}
