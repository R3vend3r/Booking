package booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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

    @NotBlank(message = "Телефон обязателен")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Неверный формат телефона")
    private String contactPhone;
}
