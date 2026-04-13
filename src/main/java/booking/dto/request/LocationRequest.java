package booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalTime;

@Data
public class LocationRequest {
    @NotBlank(message = "Название обязательно")
    private String branchName;

    @NotBlank(message = "Адрес обязателен")
    private String address;

    @NotBlank(message = "Город обязателен")
    private String city;

    private LocalTime openingTime;

    private LocalTime closingTime;

    private String contactPhone;
}
