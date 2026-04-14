package booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkPlaceResponse {
    private String id;
    private String name;
    private int capacity;
    private String description;
    private String locationId;
    private int priceForHour;
    private boolean isAvailable;
}
