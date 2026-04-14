package booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings")
public class Booking {

    @Id
    private String id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    @ToString.Exclude
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workplace_id")
    @ToString.Exclude
    private WorkPlace workPlace;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<BookingService> bookingServices = new ArrayList<>();

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Contract contract;

    @PrePersist
    private void generateId() {
        if (this.id == null) {
            this.id = "B-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public void addService(AdditionalService service, int quantity, int priceAtBookingTime) {
        BookingServiceId id = new BookingServiceId(this.id, service.getId());
        BookingService bs = new BookingService(id, this, service, quantity, priceAtBookingTime);
        bookingServices.add(bs);
        service.getBookingServices().add(bs);
    }

    public void removeService(AdditionalService service) {
        bookingServices.removeIf(bs -> bs.getService().equals(service));
        service.getBookingServices().removeIf(bs -> bs.getBooking().equals(this));
    }

    public Long getTotalAmount() {
        long servicesTotal = bookingServices.stream()
                .mapToLong(BookingService::getTotalPrice)
                .sum();

        long workplaceTotal = 0L;
        if (workPlace != null && startTime != null && endTime != null) {
            long hours = java.time.Duration.between(startTime, endTime).toHours();
            if (hours < 1) hours = 1;
            workplaceTotal = workPlace.getPriceForHour() * hours;
        }

        return servicesTotal + workplaceTotal;
    }

    public Contract createContract() {
        if (this.contract == null) {
            Contract newContract = new Contract();
            newContract.setBooking(this);
            newContract.setTotalAmount(this.getTotalAmount());
            this.contract = newContract;
        }
        return this.contract;
    }
}