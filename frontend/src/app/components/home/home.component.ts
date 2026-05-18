import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { BookingService } from '../../services/booking.service';
import { AuthService } from '../../services/auth.service';
import { Location, AdditionalService } from '../../models/booking.model';

@Component({
  standalone: false,
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  Math = Math;
  locPage = 0;
  locPageSize = 6;
  svcPage = 0;
  svcPageSize = 10;

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
  showActiveOnly = false;
  recentWorkplaces: any[] = [];
  recentLoading = true;

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
    this.loadRecentWorkplaces();
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
    this.locPage = 0;
  }

  toggleActiveOnly(): void {
    this.showActiveOnly = !this.showActiveOnly;
    this.locPage = 0;
    if (this.showActiveOnly) {
      this.loading = true;
      this.bookingService.getLocationsWithAvailableWorkplaces().subscribe({
        next: (data) => {
          this.locations = data;
          this.onSearch();
          this.loading = false;
        },
        error: () => this.loading = false
      });
    } else {
      this.loadLocations();
    }
  }

  prevLocPage(): void {
    if (this.locPage > 0) this.locPage--;
  }

  nextLocPage(): void {
  if ((this.locPage + 1) * this.locPageSize < this.filteredLocations.length) this.locPage++;
  }

  prevSvcPage(): void {
    if (this.svcPage > 0) this.svcPage--;
  }

  nextSvcPage(): void {
    if ((this.svcPage + 1) * this.svcPageSize < this.services.length) this.svcPage++;
  }


  loadRecentWorkplaces(): void {
    this.bookingService.getRecentlyBookedWorkplaces(5).subscribe({
      next: (data) => { this.recentWorkplaces = data; this.recentLoading = false; },
      error: () => this.recentLoading = false
    });
  }

  scrollTo(id: string): void {
    document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });
  }
}
