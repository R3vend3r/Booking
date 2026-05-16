package booking.dto.auth;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {
    private String email;
    private String fullName;
    private String phone;
    private LocalDate birthday;
}
