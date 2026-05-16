import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Location, WorkPlace, AdditionalService, Booking, BookingService as BookingServiceModel, Contract } from '../models/booking.model';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  // Locations
  getLocations(): Observable<Location[]> {
    return this.http.get<Location[]>(`${this.apiUrl}/locations`);
  }

  getLocationById(id: string): Observable<Location> {
    return this.http.get<Location>(`${this.apiUrl}/locations/${id}`);
  }

  getLocationsByCity(city: string): Observable<Location[]> {
    return this.http.get<Location[]>(`${this.apiUrl}/locations/city/${city}`);
  }

  // Workplaces
  getWorkplaces(): Observable<WorkPlace[]> {
    return this.http.get<WorkPlace[]>(`${this.apiUrl}/workplaces`);
  }

  getWorkplaceById(id: string): Observable<WorkPlace> {
    return this.http.get<WorkPlace>(`${this.apiUrl}/workplaces/${id}`);
  }

  getWorkplacesByLocation(locationId: string): Observable<WorkPlace[]> {
    return this.http.get<WorkPlace[]>(`${this.apiUrl}/workplaces/location/${locationId}`);
  }

  getAvailableWorkplaces(locationId: string): Observable<WorkPlace[]> {
    return this.http.get<WorkPlace[]>(`${this.apiUrl}/workplaces/location/${locationId}/available`);
  }

  // Services
  getServices(): Observable<AdditionalService[]> {
    return this.http.get<AdditionalService[]>(`${this.apiUrl}/services`);
  }

  getServiceById(id: string): Observable<AdditionalService> {
    return this.http.get<AdditionalService>(`${this.apiUrl}/services/${id}`);
  }

  // Bookings
  getMyBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.apiUrl}/bookings/my`);
  }

  getBookingById(id: string): Observable<Booking> {
    return this.http.get<Booking>(`${this.apiUrl}/bookings/${id}`);
  }

  getBookingWithServices(id: string): Observable<Booking> {
    return this.http.get<Booking>(`${this.apiUrl}/bookings/${id}/with-services`);
  }

  createBooking(booking: { clientId: string; workPlaceId: string; startTime: string; endTime: string }): Observable<Booking> {
    return this.http.post<Booking>(`${this.apiUrl}/bookings`, booking);
  }

  updateBooking(id: string, booking: { workPlaceId: string; startTime: string; endTime: string }): Observable<Booking> {
    return this.http.put<Booking>(`${this.apiUrl}/bookings/${id}`, booking);
  }

  deleteBooking(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bookings/${id}`);
  }

  checkWorkplaceAvailability(workplaceId: string, start: string, end: string): Observable<boolean> {
    const params = new HttpParams()
      .set('start', start)
      .set('end', end);
    return this.http.get<boolean>(`${this.apiUrl}/bookings/workplace/${workplaceId}/check`, { params });
  }

  // Booking Services
  addServiceToBooking(bookingId: string, serviceId: string, quantity: number): Observable<BookingService> {
    return this.http.post<BookingService>(`${this.apiUrl}/bookings/services`, {
      bookingId,
      serviceId,
      quantity
    });
  }

  updateServiceQuantity(bookingId: string, serviceId: string, quantity: number): Observable<BookingService> {
    return this.http.put<BookingService>(
      `${this.apiUrl}/bookings/services/${bookingId}/${serviceId}?quantity=${quantity}`,
      {}
    );
  }

  removeServiceFromBooking(bookingId: string, serviceId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bookings/services/${bookingId}/${serviceId}`);
  }

  getServicesByBooking(bookingId: string): Observable<BookingService[]> {
    return this.http.get<BookingService[]>(`${this.apiUrl}/bookings/services/booking/${bookingId}`);
  }

  // Contracts
  getContractByBooking(bookingId: string): Observable<Contract> {
    return this.http.get<Contract>(`${this.apiUrl}/contracts/booking/${bookingId}`);
  }

  createContract(bookingId: string): Observable<Contract> {
    return this.http.post<Contract>(`${this.apiUrl}/contracts`, { bookingId });
  }

  payContract(contractId: string, method: 'CARD' | 'CASH' | 'TRANSFER'): Observable<Contract> {
    return this.http.post<Contract>(
      `${this.apiUrl}/contracts/${contractId}/pay?method=${method}`,
      {}
    );
  }
}
