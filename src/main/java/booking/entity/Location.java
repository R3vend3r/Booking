package booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "locations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id", "branch_name"}))
public class Location {

    @Id
    @Column(nullable = false)
    private String id;
    @Column(name = "branch_name")
    private String branchName;
    @Column
    private String address;
    @Column
    private String city;
    @Column(name = "open_time")
    private LocalTime openingTime;
    @Column(name = "close_time")
    private LocalTime closingTime;
    @Column(name = "contact_phone")
    private String contactPhone;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<WorkPlace> workplaces = new ArrayList<>();

    public Location(String name, String address, String city, LocalTime openingTime, LocalTime closingTime, String contactPhone) {
        if (openingTime != null && closingTime != null &&
                openingTime.isAfter(closingTime)) {
            throw new IllegalArgumentException("Opening time must be before closing time");
        }
        this.branchName = name;
        this.address = address;
        this.city = city;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.contactPhone = contactPhone;
    }

    @PrePersist
    private void generateId() {
        if (this.id == null) {
            this.id = "L-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }
    public boolean isOpenAt(LocalTime time) {
        if (openingTime == null || closingTime == null || time == null) {
            return false;
        }
        return !time.isBefore(openingTime) && !time.isAfter(closingTime);
    }
}
