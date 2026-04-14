package booking.dto.mapper;

import booking.dto.request.WorkPlaceRequest;
import booking.dto.response.WorkPlaceResponse;
import booking.entity.Location;
import booking.entity.WorkPlace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WorkPlaceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", source = "locationId", qualifiedByName = "toLocation")
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "available", constant = "true")
    WorkPlace toEntity(WorkPlaceRequest request);

    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "available", target = "available")
    WorkPlaceResponse toResponse(WorkPlace workPlace);

    @Named("toLocation")
    static Location toLocation(String locationId) {
        if (locationId == null) return null;
        Location location = new Location();
        location.setId(locationId);
        return location;
    }
}