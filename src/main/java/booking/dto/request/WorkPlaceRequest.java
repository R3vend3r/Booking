package booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkPlaceRequest {
    @NotBlank(message = "Название обязательно")
    private String name;
    @NotBlank(message = "Локация обязательна")
    private String locationId;
    private int capacity;
    private String description;
    private int priceForHour;
}
