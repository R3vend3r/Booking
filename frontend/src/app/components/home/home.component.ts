import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { BookingService } from '../../services/booking.service';
import { AuthService } from '../../services/auth.service';
import { Location, AdditionalService } from '../../models/booking.model';

@Component({
  standalone: false,
  selector: 'app-home',
  template: `
    <div *ngIf="showSuccessToast" style="
      position: fixed; top: 80px; left: 50%; transform: translateX(-50%);
      background: #22c55e; color: white; padding: 16px 32px; border-radius: 8px;
      font-weight: 500; z-index: 1000; box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    ">
      Бронирование успешно создано!
    </div>
    <!-- Hero -->
    <section class="hero-section">
      <div class="container" style="text-align: center;">
        <h1 style="font-size: 48px; margin-bottom: 16px; color: white;">Аренда рабочих мест</h1>
        <p style="font-size: 20px; color: rgba(255,255,255,0.85); margin-bottom: 40px; max-width: 600px; margin-left: auto; margin-right: auto;">
          Найдите идеальное место для работы в удобной локации
        </p>
        <a (click)="scrollTo('locations')" class="btn" 
           style="background: white; color: var(--primary); padding: 14px 36px; font-size: 16px; font-weight: 600; border-radius: 8px; cursor: pointer;">
          Выбрать локацию
        </a>
      </div>
    </section>

    <!-- About -->
    <section id="about" class="section">
      <div class="container">
        <h2 class="section-title">О нас</h2>
        <p style="text-align: center; color: #64748b; max-width: 700px; margin: 0 auto 48px; font-size: 16px; line-height: 1.7;">
          Мы предоставляем современные рабочие места в различных локациях города. 
          Коворкинг, переговорные комнаты — всё для комфортной и продуктивной работы.
        </p>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 24px;">
          <div class="feature-card" *ngFor="let feature of features">
            <div class="feature-icon">{{ feature.icon }}</div>
            <h3>{{ feature.title }}</h3>
            <p>{{ feature.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Services -->
    <section id="services" class="section" style="background: #f8fafc;">
      <div class="container">
        <h2 class="section-title">Услуги</h2>
        <p style="text-align: center; color: #64748b; margin-bottom: 40px;">
          Дополнительные возможности для комфортной работы
        </p>
        <div *ngIf="servicesLoading" class="loading"><p>Загрузка...</p></div>
        <div *ngIf="!servicesLoading" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 24px;">
          <div class="card" *ngFor="let s of services" style="text-align: center; padding: 32px 24px;">
            <h3 style="margin-bottom: 12px;">{{ s.name }}</h3>
            <p style="color: #64748b; margin-bottom: 16px; font-size: 14px;">{{ s.description }}</p>
            <span style="font-size: 22px; font-weight: 700; color: var(--primary);">{{ s.price }} ₽</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Locations -->
    <section id="locations" class="section">
      <div class="container">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 32px; flex-wrap: wrap; gap: 16px;">
          <h2 class="section-title" style="margin-bottom: 0;">Локации</h2>
          <input type="text" 
                 [(ngModel)]="searchCity" 
                 (input)="onSearch()"
                 placeholder="Поиск по городу..."
                 style="padding: 10px 16px; width: 250px; border: 1px solid var(--border); border-radius: 8px;" />
        </div>
        
        <div *ngIf="loading" class="loading"><p>Загрузка...</p></div>
        
        <div *ngIf="!loading" style="display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px;">
          <div *ngFor="let location of filteredLocations" class="location-card" 
               [routerLink]="['/workplaces', location.id]">
            <div class="location-header">
              <h3>{{ location.name }}</h3>
              <span class="location-city">{{ location.city }}</span>
            </div>
            <p class="location-address">{{ location.address }}</p>
            <div class="location-footer">
              <span>{{ location.openingTime }} — {{ location.closingTime }}</span>
              <span class="location-arrow">Выбрать место →</span>
            </div>
          </div>
        </div>
        
        <div *ngIf="!loading && filteredLocations.length === 0" style="text-align: center; padding: 60px;">
          <p style="color: #64748b;">Локации не найдены</p>
        </div>
      </div>
    </section>

    <!-- Contacts -->
    <section id="contacts" class="section" style="background: #f8fafc;">
      <div class="container" style="max-width: 600px;">
        <h2 class="section-title">Контакты</h2>
        <div style="display: grid; gap: 20px;">
          <div class="contact-item">
            <span class="contact-label">Телефон</span>
            <span>+7 (495) 123-45-67</span>
          </div>
          <div class="contact-item">
            <span class="contact-label">Email</span>
            <span>info@booking.ru</span>
          </div>
          <div class="contact-item">
            <span class="contact-label">Режим работы</span>
            <span>Пн—Пт: 9:00 — 21:00, Сб—Вс: 10:00 — 20:00</span>
          </div>
        </div>
      </div>
    </section>
  `,
  styles: [`
    .hero-section {
      background: linear-gradient(135deg, #2563eb, #1d4ed8);
      padding: 80px 0 80px;
    }
    .section { padding: 72px 0; }
    .section-title {
      text-align: center; font-size: 32px; margin-bottom: 16px;
    }
    .feature-card {
      background: white; border-radius: 12px; padding: 32px 24px;
      text-align: center; box-shadow: 0 1px 3px rgba(0,0,0,0.08);
      transition: transform 0.2s;
    }
    .feature-card:hover { transform: translateY(-4px); }
    .feature-icon { font-size: 36px; margin-bottom: 16px; }
    .feature-card h3 { margin-bottom: 8px; font-size: 16px; }
    .feature-card p { color: #64748b; font-size: 14px; line-height: 1.6; }
    .location-card {
      background: white; border-radius: 12px; padding: 24px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.08); cursor: pointer;
      transition: box-shadow 0.2s, transform 0.2s;
    }
    .location-card:hover {
      box-shadow: 0 4px 12px rgba(0,0,0,0.12); transform: translateY(-2px);
    }
    .location-header {
      display: flex; justify-content: space-between; align-items: start; margin-bottom: 8px;
    }
    .location-header h3 { font-size: 18px; }
    .location-city {
      background: #eff6ff; color: #2563eb; padding: 2px 10px;
      border-radius: 12px; font-size: 13px; font-weight: 500;
    }
    .location-address { color: #64748b; font-size: 14px; margin-bottom: 16px; }
    .location-footer {
      display: flex; justify-content: space-between; align-items: center;
      font-size: 14px; color: #94a3b8; padding-top: 16px; border-top: 1px solid #f1f5f9;
    }
    .location-arrow { color: var(--primary); font-weight: 500; }
    .contact-item {
      display: flex; justify-content: space-between; padding: 16px 20px;
      background: white; border-radius: 8px;
    }
    .contact-label { color: #64748b; }
  `]
})
export class HomeComponent implements OnInit {
  features = [
    { icon: '🏢', title: 'Удобные локации', desc: 'Коворкинги в разных районах города рядом с метро' },
    { icon: '⚡', title: 'Быстрый интернет', desc: 'Высокоскоростной Wi-Fi на всех рабочих местах' },
    { icon: '☕', title: 'Всё включено', desc: 'Чай, кофе, вода и переговорные комнаты' },
    { icon: '🕐', title: 'Гибкий график', desc: 'Бронируйте на час, день или месяц' }
  ];

  locations: Location[] = [];
  filteredLocations: Location[] = [];
  services: AdditionalService[] = [];
  searchCity = '';
  loading = true;
  servicesLoading = true;

  showSuccessToast = false;

  constructor(
    private bookingService: BookingService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUser;
    if (user?.role === 'ROLE_ADMIN') {
      this.router.navigate(['/admin']);
      return;
    }
    if (user?.role === 'ROLE_MANAGER') {
      this.router.navigate(['/manager']);
      return;
    }
    this.route.queryParams.subscribe(params => {
      if (params['booked'] === 'true') {
        this.showSuccessToast = true;
        setTimeout(() => { this.showSuccessToast = false; }, 4000);
        this.router.navigate([], { queryParams: { booked: null }, queryParamsHandling: 'merge' });
      }
    });
    this.loadLocations();
    this.loadServices();
  }

  loadLocations(): void {
    this.bookingService.getLocations().subscribe({
      next: (data) => {
        this.locations = data;
        this.filteredLocations = data;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  loadServices(): void {
    this.bookingService.getServices().subscribe({
      next: (data) => { this.services = data; this.servicesLoading = false; },
      error: () => this.servicesLoading = false
    });
  }

  onSearch(): void {
    if (!this.searchCity) {
      this.filteredLocations = this.locations;
    } else {
      this.filteredLocations = this.locations.filter(l =>
        l.city.toLowerCase().includes(this.searchCity.toLowerCase())
      );
    }
  }

  scrollTo(id: string): void {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
  }
}
