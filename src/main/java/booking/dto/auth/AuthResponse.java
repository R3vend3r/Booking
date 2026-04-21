package booking.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String userId;
    private String login;
    private String role;
    private String token;

    public AuthResponse(String userId, String login, String role) {
        this.userId = userId;
        this.login = login;
        this.role = role;
    }
}
