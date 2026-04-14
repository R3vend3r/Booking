package booking.dto.mapper;

import booking.dto.request.ClientRequest;
import booking.dto.response.ClientResponse;
import booking.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "birthday", target = "birthday")
    ClientResponse toResponse(Client client);

    List<ClientResponse> toResponseList(List<Client> clients);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    Client toEntity(ClientRequest request);
}