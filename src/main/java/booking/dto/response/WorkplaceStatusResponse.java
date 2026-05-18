package booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkplaceStatusResponse {
    private String id;
    private String name;
    private int capacity;
    private String description;
    private String locationId;
    private int priceForHour;
    private boolean available;
    private boolean availableNow;
    private LocalDateTime currentBookingStart;
    private LocalDateTime currentBookingEnd;
    private LocalDateTime nextBookingStart;
    private LocalDateTime nextBookingEnd;
}
