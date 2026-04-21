package booking.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String login;
    private String email;
    private String fullName;
    private String phone;
    private boolean enabled;
}