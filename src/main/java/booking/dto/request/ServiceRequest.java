package booking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceRequest {
    @NotBlank(message = "Название услуги обязательно")
    private String name;
    @NotBlank(message = "Необходимо описание для услуги")
    private String description;
    @NotNull(message = "Укажите цену услуги")
    @Min(value = 0, message = "Цена не может быть отрицательной")
    private int price;
}
