package booking.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegistrationRequest{
       @NotBlank(message = "Логин обязателен")
       @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
       private String login;

       @NotBlank(message = "Пароль обязателен")
       @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
       private String password;

       private String fullName;

       @Email(message = "Неверный формат email")
       @NotBlank(message = "Email обязателен")
       private String email;

       private LocalDate birthday;

       private String phone;
        // паспорт как будто делать не оптимально, и тогда скидки по др не делаем
        //телефон при регистрации не обязателен, либо делать емайл не обзательным
}
