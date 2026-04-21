package booking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkPlaceRequest {
    @NotBlank(message = "Название обязательно")
    private String name;

    @NotBlank(message = "Локация обязательна")
    private String locationId;

    @NotNull(message = "Укажите вместительность")
    @Min(value = 1, message = "Вместительность должна быть не менее 1")
    private Integer capacity;

    @NotBlank(message = "Предоставьте описание")
    private String description;

    @NotNull(message = "Введите цену")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private Integer priceForHour;
}