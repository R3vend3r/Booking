package booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkPlaceRequest {
    @NotBlank(message = "Название обязательно")
    private String name;
    @NotBlank(message = "Локация обязательна")
    private String locationId;
    @NotBlank(message = "Укажите вместительность")
    private int capacity;
    @NotBlank(message = "Предоставьте описание")
    private String description;
    @NotBlank(message = "Введите цену")
    private int priceForHour;
}
