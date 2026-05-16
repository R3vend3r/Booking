package booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentWorkplaceResponse {
    private String id;
    private String name;
    private int priceForHour;
    private String locationId;
    private String locationName;
    private boolean availableNow;
    private LocalDateTime nextAvailableTime;
}
