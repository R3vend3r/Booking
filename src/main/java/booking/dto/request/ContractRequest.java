package booking.dto.request;

import booking.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContractRequest {

    @NotNull(message = "ID бронирования обязательно")
    private String bookingId;

    private PaymentMethod paymentMethod;
}