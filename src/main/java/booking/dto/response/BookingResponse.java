package booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResponse {
    private String id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String clientId;
    private String clientName;
    private String workPlaceId;
    private String workPlaceName;
    private String locationName;
    private String locationAddress;
    private String locationCity;
    private Long totalAmount;
    private String contractId;
    private String paymentStatus;
    private List<BookingServiceResponse> services;
}
