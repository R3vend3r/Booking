package booking.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegistrationRequest {
       @NotBlank(message = "Логин обязателен")
       @Size(min = 3, max = 50, message = "Логин должен быть от 3 до 50 символов")
       private String login;

       @NotBlank(message = "Пароль обязателен")
       @Size(min = 8, message = "Пароль должен быть не менее 8 символов")
       private String password;

       @NotBlank(message = "ФИО обязательно")
       private String fullName;

       @Email(message = "Неверный формат email")
       @NotBlank(message = "Email обязателен")
       private String email;

       @Past(message = "Дата рождения должна быть в прошлом")
       private LocalDate birthday;

       @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$", message = "Неверный формат телефона")
       private String phone;
}