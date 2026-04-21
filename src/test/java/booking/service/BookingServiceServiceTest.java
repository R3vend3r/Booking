package booking.service;

import booking.dto.mapper.BookingServiceMapper;
import booking.dto.request.BookingServiceRequest;
import booking.dto.response.BookingServiceResponse;
import booking.entity.AdditionalService;
import booking.entity.Booking;
import booking.entity.BookingService;
import booking.entity.BookingServiceId;
import booking.exception.ServiceException;
import booking.repo.AdditionalServiceRepository;
import booking.repo.BookingRepository;
import booking.repo.BookingServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceServiceTest {

    @Mock
    private BookingServiceRepository bookingServiceRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AdditionalServiceRepository additionalServiceRepository;

    @Mock
    private BookingServiceMapper bookingServiceMapper;

    @InjectMocks
    private BookingServiceService bookingServiceService;

    @Test
    void addServiceToBooking_shouldSaveAndReturnResponseWhenSuccess() {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-123");
        request.setServiceId("S-456");
        request.setQuantity(2);

        Booking booking = new Booking();
        booking.setId("B-123");

        AdditionalService service = new AdditionalService();
        service.setId("S-456");
        service.setPrice(1500);

        BookingService bookingService = new BookingService();
        BookingService savedBookingService = new BookingService();
        savedBookingService.setId(new BookingServiceId("B-123", "S-456"));

        BookingServiceResponse response = new BookingServiceResponse();
        response.setBookingId("B-123");
        response.setServiceId("S-456");
        response.setQuantity(2);

        when(bookingRepository.findById("B-123")).thenReturn(Optional.of(booking));
        when(additionalServiceRepository.findById("S-456")).thenReturn(Optional.of(service));
        when(bookingServiceRepository.findById(any(BookingServiceId.class))).thenReturn(Optional.empty());
        when(bookingServiceMapper.toEntity(eq("B-123"), eq("S-456"), eq(2), eq(1500))).thenReturn(bookingService);
        when(bookingServiceRepository.save(bookingService)).thenReturn(savedBookingService);
        when(bookingServiceMapper.toResponse(savedBookingService)).thenReturn(response);

        BookingServiceResponse result = bookingServiceService.addServiceToBooking(request);

        assertThat(result).isNotNull();
        assertThat(result.getBookingId()).isEqualTo("B-123");
        assertThat(result.getServiceId()).isEqualTo("S-456");
    }

    @Test
    void addServiceToBooking_shouldThrowExceptionWhenBookingNotFound() {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-999");

        when(bookingRepository.findById("B-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingServiceService.addServiceToBooking(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Бронирование с ID B-999 не найдено");
    }

    @Test
    void addServiceToBooking_shouldThrowExceptionWhenServiceNotFound() {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-123");
        request.setServiceId("S-999");

        when(bookingRepository.findById("B-123")).thenReturn(Optional.of(new Booking()));
        when(additionalServiceRepository.findById("S-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingServiceService.addServiceToBooking(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Услуга с ID S-999 не найдена");
    }

    @Test
    void addServiceToBooking_shouldThrowExceptionWhenServiceAlreadyAdded() {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-123");
        request.setServiceId("S-456");

        when(bookingRepository.findById("B-123")).thenReturn(Optional.of(new Booking()));
        when(additionalServiceRepository.findById("S-456")).thenReturn(Optional.of(new AdditionalService()));
        when(bookingServiceRepository.findById(any(BookingServiceId.class))).thenReturn(Optional.of(new BookingService()));

        assertThatThrownBy(() -> bookingServiceService.addServiceToBooking(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Услуга уже добавлена в бронирование");
    }

    @Test
    void addServiceToBooking_shouldThrowExceptionWhenQuantityIsZero() {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-123");
        request.setServiceId("S-456");
        request.setQuantity(0);

        when(bookingRepository.findById("B-123")).thenReturn(Optional.of(new Booking()));
        when(additionalServiceRepository.findById("S-456")).thenReturn(Optional.of(new AdditionalService()));

        assertThatThrownBy(() -> bookingServiceService.addServiceToBooking(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Количество должно быть больше 0");
    }

    @Test
    void updateServiceQuantity_shouldUpdateSuccessfully() {
        String bookingId = "B-123";
        String serviceId = "S-456";
        int quantity = 3;

        BookingServiceId id = new BookingServiceId(bookingId, serviceId);
        BookingService bookingService = new BookingService();
        bookingService.setId(id);
        bookingService.setQuantity(1);

        BookingService savedBookingService = new BookingService();
        savedBookingService.setId(id);
        savedBookingService.setQuantity(3);

        BookingServiceResponse response = new BookingServiceResponse();
        response.setQuantity(3);

        when(bookingServiceRepository.findById(id)).thenReturn(Optional.of(bookingService));
        when(bookingServiceRepository.save(bookingService)).thenReturn(savedBookingService);
        when(bookingServiceMapper.toResponse(savedBookingService)).thenReturn(response);

        BookingServiceResponse result = bookingServiceService.updateServiceQuantity(bookingId, serviceId, quantity);

        assertThat(result.getQuantity()).isEqualTo(3);
    }

    @Test
    void updateServiceQuantity_shouldThrowExceptionWhenServiceNotFound() {
        String bookingId = "B-123";
        String serviceId = "S-999";
        int quantity = 3;

        BookingServiceId id = new BookingServiceId(bookingId, serviceId);
        when(bookingServiceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingServiceService.updateServiceQuantity(bookingId, serviceId, quantity))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Услуга не найдена в бронировании");
    }

    @Test
    void updateServiceQuantity_shouldThrowExceptionWhenQuantityIsZero() {
        String bookingId = "B-123";
        String serviceId = "S-456";
        int quantity = 0;

        BookingServiceId id = new BookingServiceId(bookingId, serviceId);
        BookingService bookingService = new BookingService();
        bookingService.setId(id);

        when(bookingServiceRepository.findById(id)).thenReturn(Optional.of(bookingService));

        assertThatThrownBy(() -> bookingServiceService.updateServiceQuantity(bookingId, serviceId, quantity))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Количество должно быть больше 0");
    }

    @Test
    void removeServiceFromBooking_shouldRemoveSuccessfully() {
        String bookingId = "B-123";
        String serviceId = "S-456";

        BookingServiceId id = new BookingServiceId(bookingId, serviceId);
        BookingService bookingService = new BookingService();
        bookingService.setId(id);

        when(bookingServiceRepository.findById(id)).thenReturn(Optional.of(bookingService));

        bookingServiceService.removeServiceFromBooking(bookingId, serviceId);

        verify(bookingServiceRepository, times(1)).delete(bookingService);
    }

    @Test
    void removeServiceFromBooking_shouldThrowExceptionWhenServiceNotFound() {
        String bookingId = "B-123";
        String serviceId = "S-999";

        BookingServiceId id = new BookingServiceId(bookingId, serviceId);
        when(bookingServiceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingServiceService.removeServiceFromBooking(bookingId, serviceId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Услуга не найдена в бронировании");
    }

    @Test
    void getServicesByBookingId_shouldReturnListOfResponses() {
        String bookingId = "B-123";

        BookingService bs1 = new BookingService();
        BookingService bs2 = new BookingService();
        List<BookingService> bookingServices = Arrays.asList(bs1, bs2);

        BookingServiceResponse response1 = new BookingServiceResponse();
        BookingServiceResponse response2 = new BookingServiceResponse();
        List<BookingServiceResponse> expectedResponses = Arrays.asList(response1, response2);

        when(bookingServiceRepository.findByBookingIdWithService(bookingId)).thenReturn(bookingServices);
        when(bookingServiceMapper.toResponseList(bookingServices)).thenReturn(expectedResponses);

        List<BookingServiceResponse> result = bookingServiceService.getServicesByBookingId(bookingId);

        assertThat(result).hasSize(2);
    }

    @Test
    void getTotalQuantityForService_shouldReturnTotalQuantity() {
        String serviceId = "S-456";
        Integer totalQuantity = 15;

        when(bookingServiceRepository.getTotalQuantityForService(serviceId)).thenReturn(totalQuantity);

        int result = bookingServiceService.getTotalQuantityForService(serviceId);

        assertThat(result).isEqualTo(15);
    }

    @Test
    void getTotalQuantityForService_shouldReturnZeroWhenNull() {
        String serviceId = "S-456";

        when(bookingServiceRepository.getTotalQuantityForService(serviceId)).thenReturn(null);

        int result = bookingServiceService.getTotalQuantityForService(serviceId);

        assertThat(result).isEqualTo(0);
    }
}