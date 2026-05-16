import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { AuthResponse, LoginRequest, RegistrationRequest } from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        this.setUser(response);
      })
    );
  }

  register(request: RegistrationRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/registration`, request).pipe(
      tap(response => {
        this.setUser(response);
      })
    );
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    const user = this.getUserFromStorage();
    return user?.token || null;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  hasRole(role: string): boolean {
    const user = this.getUserFromStorage();
    return user?.role === role;
  }

  getCurrentRole(): string | null {
    return this.getUserFromStorage()?.role || null;
  }

  get currentUser(): AuthResponse | null {
    return this.getUserFromStorage();
  }

  private setUser(response: AuthResponse): void {
    localStorage.setItem('currentUser', JSON.stringify(response));
    this.currentUserSubject.next(response);
  }

  private getUserFromStorage(): AuthResponse | null {
    const user = localStorage.getItem('currentUser');
    return user ? JSON.parse(user) : null;
  }
}
