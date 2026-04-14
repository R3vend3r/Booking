package booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    @NotBlank
    private LocalDateTime startTime;
    @NotBlank
    private LocalDateTime endTime;
    @NotBlank(message = "Клиент должен быть зарегистрирован")
    private String clientId;
    @NotBlank(message = "Выберите рабочее место")
    private String workPlaceId;
}
