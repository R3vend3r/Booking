export interface Location {
  id: string;
  name: string;
  address: string;
  city: string;
  openingTime: string;
  closingTime: string;
  contactPhone: string;
  workplacesCount?: number;
}

export interface WorkPlace {
  id: string;
  name: string;
  capacity: number;
  description?: string;
  priceForHour: number;
  available: boolean;
  locationId: string;
  availableNow?: boolean;
  currentBookingStart?: string;
  currentBookingEnd?: string;
  nextBookingStart?: string;
  nextBookingEnd?: string;
}

export interface AdditionalService {
  id: string;
  name: string;
  description: string;
  price: number;
}

export interface Client {
  id: string;
  fullName: string;
  phone: string;
  birthday: string;
}

export interface Booking {
  id: string;
  startTime: string;
  endTime: string;
  clientId: string;
  workPlaceId: string;
  workPlaceName: string;
  locationName: string;
  locationAddress: string;
  workplace: WorkPlace;
  services?: BookingService[];
  totalAmount?: number;
  contract?: Contract;
  contractId?: string;
  paymentStatus?: string;
}

export interface BookingService {
  bookingId: string;
  serviceId: string;
  serviceName: string;
  quantity: number;
  priceAtBookingTime: number;
  totalPrice: number;
}

export interface Contract {
  id: string;
  bookingId: string;
  totalAmount: number;
  paymentStatus: 'PAID' | 'PENDING' | 'CANCELLED' | 'COMPLETED' | 'REFUNDED';
  paymentMethod?: 'CARD' | 'CASH' | 'TRANSFER';
  createdAt: string;
}
