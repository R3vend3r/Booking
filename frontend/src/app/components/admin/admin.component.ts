import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  standalone: false,
  selector: 'app-admin',
  template: `
    <div class="container">
      <h1 style="margin-bottom: 24px;">Админ-панель</h1>
      
      <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 24px;">
        <div class="card">
          <h3 style="margin-bottom: 16px;">Управление пользователями</h3>
          <p style="color: #64748b; margin-bottom: 16px;">
            Просмотр активных клиентов, блокировка/разблокировка пользователей
          </p>
          <button class="btn btn-primary" (click)="loadUsers()">
            Загрузить пользователей
          </button>
          
          <div *ngIf="users.length" style="margin-top: 16px;">
            <div *ngFor="let u of users" style="display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid var(--border);">
              <div>
                <p style="font-weight: 500;">{{ u.login }}</p>
                <p style="font-size: 12px; color: #64748b;">{{ u.email }}</p>
              </div>
              <button class="btn" 
                      [style.background]="u.enabled ? '#fee2e2' : '#dcfce7'"
                      [style.color]="u.enabled ? '#dc2626' : '#16a34a'"
                      (click)="toggleUser(u)">
                {{ u.enabled ? 'Заблокировать' : 'Разблокировать' }}
              </button>
            </div>
          </div>
        </div>
        
        <div class="card">
          <h3 style="margin-bottom: 16px;">Статистика</h3>
          <div style="display: grid; gap: 12px;">
            <div style="padding: 16px; background: #f8fafc; border-radius: 8px;">
              <p style="color: #64748b; font-size: 14px;">Активных пользователей</p>
              <p style="font-size: 24px; font-weight: 600;">{{ users.length }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class AdminComponent implements OnInit {
  users: any[] = [];
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';
    
    this.http.get<any[]>(`${this.apiUrl}/admin/users/active`, {
      headers: { Authorization: `Bearer ${tokenStr}` }
    }).subscribe({
      next: (data) => this.users = data,
      error: (err) => console.error('Error loading users:', err)
    });
  }

  toggleUser(user: any): void {
    const token = localStorage.getItem('currentUser');
    const tokenStr = token ? JSON.parse(token).token : '';
    const action = user.enabled ? 'disable' : 'enable';
    
    this.http.post(`${this.apiUrl}/admin/users/${user.userId}/${action}`, {}, {
      headers: { Authorization: `Bearer ${tokenStr}` }
    }).subscribe({
      next: () => this.loadUsers(),
      error: (err) => console.error('Error toggling user:', err)
    });
  }
}
