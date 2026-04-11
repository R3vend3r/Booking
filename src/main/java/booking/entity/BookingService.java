package booking.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@Table(name = "booking_services")
public class BookingService {

    @EmbeddedId
    private BookingServiceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id")
    @ToString.Exclude
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("serviceId")
    @JoinColumn(name = "service_id")
    @ToString.Exclude
    private Service service;

    @Column(name = "price_at_booking_time", nullable = false)
    private int priceAtBookingTime;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    public BookingService(BookingServiceId id, Booking booking, Service service,
                          int quantity, int priceAtBookingTime) {
        this.id = id;
        this.booking = booking;
        this.service = service;
        this.quantity = quantity;
        this.priceAtBookingTime = priceAtBookingTime;
    }
    public int getTotalPrice() {
        return priceAtBookingTime * quantity;
    }
}