package booking.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateManagerRequest {
    @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
    private String login;

    @Email(message = "Неверный формат email")
    private String email;

    @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
    private String password;

    private Boolean enabled;
}
