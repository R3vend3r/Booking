package booking.dto.mapper;

import booking.dto.request.ServiceRequest;
import booking.dto.response.ServiceResponse;
import booking.entity.AdditionalService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "price", target = "price")
    ServiceResponse toResponse(AdditionalService request);

    List<ServiceResponse> toResponseList(List<AdditionalService> services);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookingServices", ignore = true)
    AdditionalService toEntity(ServiceRequest request);
}
