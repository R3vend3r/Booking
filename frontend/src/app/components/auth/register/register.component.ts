import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { RegistrationRequest } from '../../../models/auth.model';

@Component({
  standalone: false,
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
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
  confirmPassword = '';
  error = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.error = '';

    if (!this.validateInputs()) {
      this.loading = false;
      return;
    }

    this.loading = true;

    this.authService.register(this.request).subscribe({
      next: () => {
        this.router.navigate(['/login'], {
          queryParams: { registered: 'success' }
        });
      },
      error: (err) => {
        this.error = err.error?.message || 'Ошибка регистрации';
        this.loading = false;
      }
    });
  }

  isPasswordsMatch(): boolean {
    return this.request.password === this.confirmPassword;
  }

  isInvalidAge(birthday: string): boolean {
    if (!birthday) return false;

    const birthDate = new Date(birthday);
    const today = new Date();

    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();

    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }

    return age < 18 || age > 120;
  }

  private validateInputs(): boolean {
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

    if (!/^[a-zA-Z0-9_]+$/.test(this.request.login)) {
      this.error = 'Логин может содержать только буквы, цифры и знак подчеркивания';
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

    if (!this.isPasswordsMatch()) {
      this.error = 'Пароли не совпадают';
      return false;
    }

    if (!this.request.email) {
      this.error = 'Пожалуйста, введите email';
      return false;
    }

    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    if (!emailRegex.test(this.request.email)) {
      this.error = 'Пожалуйста, введите корректный email адрес';
      return false;
    }

    if (!this.request.fullName || this.request.fullName.trim() === '') {
      this.error = 'Пожалуйста, введите ФИО';
      return false;
    }

    if (this.request.fullName.length < 2) {
      this.error = 'ФИО должно содержать минимум 2 символа';
      return false;
    }

    if (this.request.phone) {
      const phoneRegex = /^\+?[0-9]{10,15}$/;
      if (!phoneRegex.test(this.request.phone)) {
        this.error = 'Пожалуйста, введите корректный номер телефона';
        return false;
      }
    }

    if (this.request.birthday && this.isInvalidAge(this.request.birthday)) {
      this.error = 'Возраст должен быть от 18 до 120 лет';
      return false;
    }

    return true;
  }
}