package booking.dto.mapper;

import booking.dto.request.ContractRequest;
import booking.dto.response.ContractResponse;
import booking.entity.Booking;
import booking.entity.Contract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contractNumber", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "booking", source = "bookingId", qualifiedByName = "mapBooking")
    Contract toEntity(ContractRequest request);

    @Mapping(source = "booking.id", target = "bookingId")
    ContractResponse toResponse(Contract contract);

    @Named("mapBooking")
    default Booking mapBooking(String bookingId) {
        if (bookingId == null) return null;
        Booking booking = new Booking();
        booking.setId(bookingId);
        return booking;
    }
}