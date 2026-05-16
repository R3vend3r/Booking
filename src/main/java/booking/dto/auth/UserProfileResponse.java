package booking.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UserProfileResponse {
    private String login;
    private String email;
    private String role;
    private String fullName;
    private String phone;
    private LocalDate birthday;
}
