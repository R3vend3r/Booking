import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { LoginRequest } from '../../../models/auth.model';

@Component({
  standalone: false,
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
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
    if (!this.validateInputs()) {
      return;
    }

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

  private validateInputs(): boolean {
    this.error = '';

    if (!this.request.login || this.request.login.trim() === '') {
      this.error = 'Пожалуйста, введите логин';
      return false;
    }

    if (this.request.login.length < 3) {
      this.error = 'Логин должен содержать минимум 3 символа';
      return false;
    }

    if (this.request.login.length > 50) {
      this.error = 'Логин не должен превышать 50 символов';
      return false;
    }

    if (!this.request.password) {
      this.error = 'Пожалуйста, введите пароль';
      return false;
    }

    if (this.request.password.length < 6) {
      this.error = 'Пароль должен содержать минимум 6 символов';
      return false;
    }

    if (this.request.password.length > 100) {
      this.error = 'Пароль не должен превышать 100 символов';
      return false;
    }

    return true;
  }
}