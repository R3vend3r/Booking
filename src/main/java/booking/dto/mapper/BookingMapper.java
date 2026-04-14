package booking.dto.mapper;

import booking.dto.request.BookingRequest;
import booking.dto.response.BookingResponse;
import booking.entity.Booking;
import booking.entity.Client;
import booking.entity.WorkPlace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "client", source = "clientId", qualifiedByName = "mapClient")
    @Mapping(target = "workPlace", source = "workPlaceId", qualifiedByName = "mapWorkPlace")
    @Mapping(target = "bookingServices", ignore = true)
    @Mapping(target = "contract", ignore = true)
    Booking toEntity(BookingRequest request);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "workPlace.id", target = "workPlaceId")
    BookingResponse toResponse(Booking booking);

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
}