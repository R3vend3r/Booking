package booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingServiceResponse {
    private String bookingId;
    private String serviceId;
    private String serviceName;
    private int quantity;
    private int priceAtBookingTime;
    private int totalPrice;
}