package booking.dto.mapper;

import booking.dto.request.LocationRequest;
import booking.dto.response.LocationResponse;
import booking.entity.Location;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public Location toEntity(LocationRequest request) {
        return new Location(
                request.getBranchName(),
                request.getAddress(),
                request.getCity(),
                request.getOpeningTime(),
                request.getClosingTime(),
                request.getContactPhone()
        );
    }

    public LocationResponse toResponse(Location location) {
        LocationResponse response = new LocationResponse();
        response.setId(location.getId());
        response.setName(location.getBranchName());
        response.setAddress(location.getAddress());
        response.setCity(location.getCity());
        response.setOpeningTime(location.getOpeningTime());
        response.setClosingTime(location.getClosingTime());
        response.setContactPhone(location.getContactPhone());
        if (location.getWorkplaces() != null) {
            response.setWorkplacesCount(location.getWorkplaces().size());
        }

        return response;
    }
}