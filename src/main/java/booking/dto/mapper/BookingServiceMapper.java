package booking.dto.mapper;

import booking.dto.response.BookingServiceResponse;
import booking.entity.AdditionalService;
import booking.entity.Booking;
import booking.entity.BookingService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookingServiceMapper {

    @Mapping(target = "id", expression = "java(new booking.entity.BookingServiceId(bookingId, serviceId))")
    @Mapping(target = "booking", source = "bookingId", qualifiedByName = "mapBooking")
    @Mapping(target = "service", source = "serviceId", qualifiedByName = "mapService")
    @Mapping(target = "priceAtBookingTime", source = "priceAtBookingTime")
    @Mapping(target = "quantity", source = "quantity")
    BookingService toEntity(String bookingId, String serviceId, int quantity, int priceAtBookingTime);

    @Mapping(source = "booking.id", target = "bookingId")
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "service.name", target = "serviceName")
    @Mapping(source = "priceAtBookingTime", target = "priceAtBookingTime")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(target = "totalPrice", expression = "java(bookingService.getTotalPrice())")
    BookingServiceResponse toResponse(BookingService bookingService);

    List<BookingServiceResponse> toResponseList(List<BookingService> bookingServices);

    @Named("mapBooking")
    default Booking mapBooking(String bookingId) {
        if (bookingId == null) return null;
        Booking booking = new Booking();
        booking.setId(bookingId);
        return booking;
    }

    @Named("mapService")
    default AdditionalService mapService(String serviceId) {
        if (serviceId == null) return null;
        AdditionalService service = new AdditionalService();
        service.setId(serviceId);
        return service;
    }
}