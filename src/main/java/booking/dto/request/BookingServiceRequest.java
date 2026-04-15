package booking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingServiceRequest {

    @NotNull(message = "ID бронирования обязателен")
    private String bookingId;

    @NotNull(message = "ID услуги обязателен")
    private String serviceId;

    @Min(value = 1, message = "Количество должно быть не менее 1")
    private int quantity = 1;
}