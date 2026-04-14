package booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private String id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String clientId;
    private String workPlaceId;
}
