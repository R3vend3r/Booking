package booking.entity;

import booking.enums.PaymentMethod;
import booking.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "contracts")
public class Contract {

    @Id
    private String id;

    @Column(name = "contract_number", nullable = false, unique = true)
    private String contractNumber;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Column(name = "payment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @ToString.Exclude
    private Booking booking;

    @PrePersist
    private void generateId() {
        if (this.id == null) {
            this.id = "CT-" + UUID.randomUUID().toString().substring(0, 8);
        }
        if (this.contractNumber == null) {
            this.contractNumber = generateContractNumber();
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }

    private String generateContractNumber() {
        return "CONTRACT-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 4);
    }

    public void markAsPaid(PaymentMethod method) {
        this.paymentStatus = PaymentStatus.PAID;
        this.paymentMethod = method;
        this.paymentDate = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.paymentStatus = PaymentStatus.CANCELLED;
    }
}
