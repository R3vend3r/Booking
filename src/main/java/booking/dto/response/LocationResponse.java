package booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponse {
    private String id;
    private String name;
    private String address;
    private String city;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private String contactPhone;
    private Integer workplacesCount;
}
