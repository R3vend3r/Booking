import { Component } from '@angular/core';
import { AuthService } from './services/auth.service';
import { Router } from '@angular/router';

@Component({
  standalone: false,
  selector: 'app-root',
  template: `
    <nav class="navbar">
      <div class="navbar-content">
        <a routerLink="/" class="navbar-brand">Booking</a>
        
        <div class="navbar-links" *ngIf="!authService.isAuthenticated()">
          <a (click)="scrollTo('about')">О нас</a>
          <a (click)="scrollTo('services')">Услуги</a>
          <a (click)="scrollTo('locations')">Локации</a>
          <a (click)="scrollTo('contacts')">Контакты</a>
        </div>
        
        <div class="navbar-auth">
          <ng-container *ngIf="!authService.isAuthenticated()">
            <a routerLink="/login">Вход</a>
            <a routerLink="/register" class="btn btn-primary" style="padding: 6px 16px; color: white;">Регистрация</a>
          </ng-container>
          <ng-container *ngIf="authService.isAuthenticated()">
            <a routerLink="/profile" *ngIf="authService.hasRole('ROLE_USER') || authService.hasRole('ROLE_MANAGER')">Профиль</a>
            <a routerLink="/admin" *ngIf="authService.hasRole('ROLE_ADMIN')">Админ</a>
            <a routerLink="/manager" *ngIf="authService.hasRole('ROLE_MANAGER')">Менеджер</a>
            <a (click)="logout()" style="cursor: pointer">Выход</a>
          </ng-container>
        </div>
      </div>
    </nav>
    
    <main>
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .navbar-content {
      display: flex; align-items: center; gap: 32px;
    }
    .navbar-links {
      display: flex; gap: 20px;
    }
    .navbar-links a {
      cursor: pointer;
    }
    .navbar-auth {
      display: flex; gap: 12px; align-items: center; margin-left: auto;
    }
  `]
})
export class AppComponent {
  constructor(
    public authService: AuthService,
    private router: Router
  ) {}

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  scrollTo(id: string): void {
    if (this.router.url !== '/') {
      this.router.navigate(['/']).then(() => {
        setTimeout(() => {
          document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
        }, 100);
      });
    } else {
      document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
    }
  }
}
