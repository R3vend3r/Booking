package booking.dto.mapper;

import booking.dto.request.LocationRequest;
import booking.dto.response.LocationResponse;
import booking.entity.Location;
import booking.entity.WorkPlace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocationMapper {

    @Mapping(target = "id", ignore = true)
    Location toEntity(LocationRequest request);

    @Mapping(source = "branchName", target = "name")
    @Mapping(source = "workplaces", target = "workplacesCount", qualifiedByName = "countWorkplaces")
    LocationResponse toResponse(Location location);

    List<LocationResponse> toResponseList(List<Location> locations);

    @Named("countWorkplaces")
    default int countWorkplaces(List<WorkPlace> workplaces) {
        return workplaces != null ? workplaces.size() : 0;
    }
}