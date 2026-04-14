package booking.dto.response;

import booking.enums.PaymentMethod;
import booking.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponse {
    private String id;
    private String contractNumber;
    private Long totalAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private String bookingId;
}