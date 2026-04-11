package booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "workplaces")
public class WorkPlace {

    @Id
    @Column(nullable = false)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column
    private String description;

    @Column(name = "price_for_hour")
    private int priceForHour;

    @Column
    private boolean available = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    @ToString.Exclude // исключить из toString()
    private Location location;

    @OneToMany(mappedBy = "workPlace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    public WorkPlace(String name, int capacity, String description, int priceForHour, boolean available, Location location) {
        this.name = name;
        this.capacity = capacity;
        this.description = description;
        this.priceForHour = priceForHour;
        this.available = available;
        this.location = location;
    }

    @PrePersist
    private void generateId() {
        if (this.id == null) {
            this.id = "WP-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setWorkplace(this);
    }
}
