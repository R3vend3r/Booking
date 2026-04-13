package booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "clients")
public class Client {

    @Id
    @Column(nullable = false)
    private String id;

    @Column
    private String fullName;

    @Column
    private String phone;

    @Column
    private LocalDate birthday;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    public Client(String fullName, String phone, LocalDate birthday) {
        this.fullName = fullName;
        this.phone = phone;
        this.birthday = birthday;
    }

    @PrePersist
    private void generateId() {
        if (this.id == null) {
            this.id = "CL-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
    @Transient
    public int getBookingsCount() {
        return bookings != null ? bookings.size() : 0;
    }

    @Transient
    public Long getMoneySpent() {
        return bookings != null ? bookings.stream()
                .flatMap(b -> b.getBookingServices().stream())
                .mapToLong(BookingService::getTotalPrice)
                .sum() : 0L;
    }
}
