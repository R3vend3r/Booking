package booking.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ManagerResponse {
    private Long id;
    private String login;
    private String email;
    private boolean enabled;
    private LocalDateTime createdAt;
}
