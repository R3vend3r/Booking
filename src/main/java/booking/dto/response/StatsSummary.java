package booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatsSummary {
    private Double averageCheck;
    private Double totalRevenue;
    private Long paidContractsCount;
}
