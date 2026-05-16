package booking.dto.mapper;

import booking.dto.request.BookingRequest;
import booking.dto.response.BookingResponse;
import booking.dto.response.BookingServiceResponse;
import booking.entity.Booking;
import booking.entity.Client;
import booking.entity.WorkPlace;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "client", source = "clientId", qualifiedByName = "mapClient")
    @Mapping(target = "workPlace", source = "workPlaceId", qualifiedByName = "mapWorkPlace")
    @Mapping(target = "bookingServices", ignore = true)
    @Mapping(target = "contract", ignore = true)
    Booking toEntity(BookingRequest request);

    @Named("basic")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(target = "clientName", expression = "java(booking.getClient().getFullName() != null ? booking.getClient().getFullName() : booking.getClient().getId())")
    @Mapping(source = "workPlace.id", target = "workPlaceId")
    @Mapping(target = "workPlaceName", expression = "java(booking.getWorkPlace().getName())")
    @Mapping(target = "locationName", expression = "java(booking.getWorkPlace().getLocation().getBranchName())")
    @Mapping(target = "locationAddress", expression = "java(booking.getWorkPlace().getLocation().getAddress())")
    @Mapping(target = "locationCity", expression = "java(booking.getWorkPlace().getLocation().getCity())")
    @Mapping(target = "totalAmount", expression = "java(getBookingTotalAmount(booking))")
    @Mapping(target = "contractId", expression = "java(booking.getContract() != null ? booking.getContract().getId() : null)")
    @Mapping(target = "paymentStatus", source = "contract.paymentStatus")
    BookingResponse toResponse(Booking booking);

    @Named("withServices")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(target = "clientName", expression = "java(booking.getClient().getFullName() != null ? booking.getClient().getFullName() : booking.getClient().getId())")
    @Mapping(source = "workPlace.id", target = "workPlaceId")
    @Mapping(target = "workPlaceName", expression = "java(booking.getWorkPlace().getName())")
    @Mapping(target = "locationName", expression = "java(booking.getWorkPlace().getLocation().getBranchName())")
    @Mapping(target = "locationAddress", expression = "java(booking.getWorkPlace().getLocation().getAddress())")
    @Mapping(target = "locationCity", expression = "java(booking.getWorkPlace().getLocation().getCity())")
    @Mapping(target = "totalAmount", expression = "java(getBookingTotalAmount(booking))")
    @Mapping(target = "contractId", expression = "java(booking.getContract() != null ? booking.getContract().getId() : null)")
    @Mapping(target = "paymentStatus", source = "contract.paymentStatus")
    @Mapping(target = "services", expression = "java(mapServices(booking))")
    BookingResponse toResponseWithServices(Booking booking);

    @IterableMapping(qualifiedByName = "basic")
    List<BookingResponse> toResponseList(List<Booking> bookings);

    @Named("mapClient")
    default Client mapClient(String clientId) {
        if (clientId == null) return null;
        Client client = new Client();
        client.setId(clientId);
        return client;
    }

    @Named("mapWorkPlace")
    default WorkPlace mapWorkPlace(String workPlaceId) {
        if (workPlaceId == null) return null;
        WorkPlace workPlace = new WorkPlace();
        workPlace.setId(workPlaceId);
        return workPlace;
    }

    default Long getBookingTotalAmount(Booking booking) {
        if (booking.getContract() != null && booking.getContract().getTotalAmount() != null) {
            return booking.getContract().getTotalAmount();
        }
        return booking.getTotalAmount() != null ? booking.getTotalAmount() : 0L;
    }

    default List<BookingServiceResponse> mapServices(Booking booking) {
        if (booking.getBookingServices() == null) return Collections.emptyList();
        return booking.getBookingServices().stream()
                .map(bs -> new BookingServiceResponse(
                        booking.getId(),
                        bs.getService().getId(),
                        bs.getService().getName(),
                        bs.getQuantity(),
                        bs.getPriceAtBookingTime(),
                        bs.getTotalPrice()
                ))
                .collect(Collectors.toList());
    }
}