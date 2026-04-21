package booking.service;

import booking.dto.mapper.BookingMapper;
import booking.dto.request.BookingRequest;
import booking.dto.response.BookingResponse;
import booking.entity.Booking;
import booking.entity.Client;
import booking.entity.WorkPlace;
import booking.exception.ServiceException;
import booking.repo.BookingRepository;
import booking.repo.ClientRepository;
import booking.repo.WorkPlaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private WorkPlaceRepository workPlaceRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_shouldSaveAndReturnResponseWhenSuccess() {
        BookingRequest request = new BookingRequest();
        request.setClientId("CL-123");
        request.setWorkPlaceId("WP-123");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        Client client = new Client();
        client.setId("CL-123");

        WorkPlace workPlace = new WorkPlace();
        workPlace.setId("WP-123");

        Booking booking = new Booking();
        Booking savedBooking = new Booking();
        savedBooking.setId("B-123");

        BookingResponse response = new BookingResponse();
        response.setId("B-123");

        when(clientRepository.findById("CL-123")).thenReturn(Optional.of(client));
        when(workPlaceRepository.findById("WP-123")).thenReturn(Optional.of(workPlace));
        when(bookingRepository.findByWorkPlaceId("WP-123")).thenReturn(List.of());
        when(bookingMapper.toEntity(request)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(savedBooking);
        when(bookingMapper.toResponse(savedBooking)).thenReturn(response);

        BookingResponse result = bookingService.createBooking(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("B-123");
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void createBooking_shouldThrowExceptionWhenClientNotFound() {
        BookingRequest request = new BookingRequest();
        request.setClientId("CL-999");

        when(clientRepository.findById("CL-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Клиент с ID CL-999 не найден");
    }

    @Test
    void createBooking_shouldThrowExceptionWhenWorkPlaceNotFound() {
        BookingRequest request = new BookingRequest();
        request.setClientId("CL-123");
        request.setWorkPlaceId("WP-999");

        when(clientRepository.findById("CL-123")).thenReturn(Optional.of(new Client()));
        when(workPlaceRepository.findById("WP-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Рабочее место с ID WP-999 не найдено");
    }

    @Test
    void createBooking_shouldThrowExceptionWhenStartTimeAfterEndTime() {
        BookingRequest request = new BookingRequest();
        request.setClientId("CL-123");
        request.setWorkPlaceId("WP-123");
        request.setStartTime(LocalDateTime.now().plusDays(2));
        request.setEndTime(LocalDateTime.now().plusDays(1));

        when(clientRepository.findById("CL-123")).thenReturn(Optional.of(new Client()));
        when(workPlaceRepository.findById("WP-123")).thenReturn(Optional.of(new WorkPlace()));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Время начала не может быть позже времени окончания");
    }

    @Test
    void createBooking_shouldThrowExceptionWhenStartTimeInPast() {
        BookingRequest request = new BookingRequest();
        request.setClientId("CL-123");
        request.setWorkPlaceId("WP-123");
        request.setStartTime(LocalDateTime.now().minusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1));

        when(clientRepository.findById("CL-123")).thenReturn(Optional.of(new Client()));
        when(workPlaceRepository.findById("WP-123")).thenReturn(Optional.of(new WorkPlace()));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Время бронирования не может быть в прошлом");
    }

    @Test
    void createBooking_shouldThrowExceptionWhenWorkPlaceOccupied() {
        BookingRequest request = new BookingRequest();
        request.setClientId("CL-123");
        request.setWorkPlaceId("WP-123");
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        Client client = new Client();
        client.setId("CL-123");

        WorkPlace workPlace = new WorkPlace();
        workPlace.setId("WP-123");

        Booking existingBooking = new Booking();
        existingBooking.setStartTime(LocalDateTime.now().plusDays(1).plusHours(1));
        existingBooking.setEndTime(LocalDateTime.now().plusDays(1).plusHours(3));

        when(clientRepository.findById("CL-123")).thenReturn(Optional.of(client));
        when(workPlaceRepository.findById("WP-123")).thenReturn(Optional.of(workPlace));
        when(bookingRepository.findByWorkPlaceId("WP-123")).thenReturn(List.of(existingBooking));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Рабочее место уже забронировано на выбранное время");
    }

    @Test
    void getBookingById_shouldReturnResponseWhenFound() {
        String id = "B-123";
        Booking booking = new Booking();
        booking.setId(id);
        BookingResponse response = new BookingResponse();
        response.setId(id);

        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));
        when(bookingMapper.toResponse(booking)).thenReturn(response);

        BookingResponse result = bookingService.getBookingById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void getBookingById_shouldThrowExceptionWhenNotFound() {
        String id = "B-999";
        when(bookingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Бронирование с ID B-999 не найдено");
    }

    @Test
    void getAllBookings_shouldReturnListOfResponses() {
        Booking booking1 = new Booking();
        Booking booking2 = new Booking();
        List<Booking> bookings = Arrays.asList(booking1, booking2);

        BookingResponse response1 = new BookingResponse();
        BookingResponse response2 = new BookingResponse();

        when(bookingRepository.findAll()).thenReturn(bookings);
        when(bookingMapper.toResponse(booking1)).thenReturn(response1);
        when(bookingMapper.toResponse(booking2)).thenReturn(response2);

        List<BookingResponse> result = bookingService.getAllBookings();

        assertThat(result).hasSize(2);
    }

    @Test
    void getBookingsByClient_shouldReturnListOfResponses() {
        String clientId = "CL-123";
        Client client = new Client();
        client.setId(clientId);

        Booking booking1 = new Booking();
        Booking booking2 = new Booking();
        List<Booking> bookings = Arrays.asList(booking1, booking2);

        BookingResponse response1 = new BookingResponse();
        BookingResponse response2 = new BookingResponse();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(bookingRepository.findByClientId(clientId)).thenReturn(bookings);
        when(bookingMapper.toResponse(booking1)).thenReturn(response1);
        when(bookingMapper.toResponse(booking2)).thenReturn(response2);

        List<BookingResponse> result = bookingService.getBookingsByClient(clientId);

        assertThat(result).hasSize(2);
    }

    @Test
    void getBookingsByClient_shouldThrowExceptionWhenClientNotFound() {
        String clientId = "CL-999";
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingsByClient(clientId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Клиент с ID CL-999 не найден");
    }

    @Test
    void updateBooking_shouldUpdateSuccessfully() {
        String id = "B-123";
        BookingRequest request = new BookingRequest();
        request.setWorkPlaceId("WP-123");
        request.setStartTime(LocalDateTime.now().plusDays(2));
        request.setEndTime(LocalDateTime.now().plusDays(2).plusHours(2));

        WorkPlace existingWorkPlace = new WorkPlace();
        existingWorkPlace.setId("WP-456");

        Booking existingBooking = new Booking();
        existingBooking.setId(id);
        existingBooking.setWorkPlace(existingWorkPlace);
        existingBooking.setStartTime(LocalDateTime.now().plusDays(1));
        existingBooking.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        WorkPlace newWorkPlace = new WorkPlace();
        newWorkPlace.setId("WP-123");

        Booking updatedBooking = new Booking();
        updatedBooking.setId(id);
        updatedBooking.setWorkPlace(newWorkPlace);

        BookingResponse response = new BookingResponse();
        response.setId(id);

        when(bookingRepository.findById(id)).thenReturn(Optional.of(existingBooking));
        when(workPlaceRepository.findById("WP-123")).thenReturn(Optional.of(newWorkPlace));
        when(bookingRepository.findByWorkPlaceId("WP-123")).thenReturn(List.of());
        when(bookingRepository.save(existingBooking)).thenReturn(updatedBooking);
        when(bookingMapper.toResponse(updatedBooking)).thenReturn(response);

        BookingResponse result = bookingService.updateBooking(id, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void updateBooking_shouldThrowExceptionWhenBookingNotFound() {
        String id = "B-999";
        BookingRequest request = new BookingRequest();

        when(bookingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateBooking(id, request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Бронирование с ID B-999 не найдено");
    }

    @Test
    void deleteBooking_shouldDeleteSuccessfully() {
        String id = "B-123";
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStartTime(LocalDateTime.now().plusDays(1));

        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));

        bookingService.deleteBooking(id);

        verify(bookingRepository, times(1)).delete(booking);
    }

    @Test
    void deleteBooking_shouldThrowExceptionWhenBookingNotFound() {
        String id = "B-999";
        when(bookingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.deleteBooking(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Бронирование с ID B-999 не найдено");
    }

    @Test
    void deleteBooking_shouldThrowExceptionWhenBookingAlreadyStarted() {
        String id = "B-123";
        Booking booking = new Booking();
        booking.setId(id);
        booking.setStartTime(LocalDateTime.now().minusHours(1));

        when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.deleteBooking(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Нельзя отменить уже начавшееся бронирование");
    }

    @Test
    void isWorkplaceOccupied_shouldReturnTrueWhenOccupied() {
        String workPlaceId = "WP-123";
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1).plusHours(2);

        Booking existingBooking = new Booking();
        existingBooking.setStartTime(LocalDateTime.now().plusDays(1).plusHours(1));
        existingBooking.setEndTime(LocalDateTime.now().plusDays(1).plusHours(3));

        when(bookingRepository.findByWorkPlaceId(workPlaceId)).thenReturn(List.of(existingBooking));

        boolean result = bookingService.isWorkplaceOccupied(workPlaceId, start, end);

        assertThat(result).isTrue();
    }

    @Test
    void isWorkplaceOccupied_shouldReturnFalseWhenNotOccupied() {
        String workPlaceId = "WP-123";
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1).plusHours(2);

        when(bookingRepository.findByWorkPlaceId(workPlaceId)).thenReturn(List.of());

        boolean result = bookingService.isWorkplaceOccupied(workPlaceId, start, end);

        assertThat(result).isFalse();
    }

    @Test
    void getActiveBookings_shouldReturnListOfActiveBookings() {
        Booking booking1 = new Booking();
        Booking booking2 = new Booking();
        List<Booking> bookings = Arrays.asList(booking1, booking2);

        BookingResponse response1 = new BookingResponse();
        BookingResponse response2 = new BookingResponse();

        when(bookingRepository.findActiveBookings(any(LocalDateTime.class))).thenReturn(bookings);
        when(bookingMapper.toResponse(booking1)).thenReturn(response1);
        when(bookingMapper.toResponse(booking2)).thenReturn(response2);

        List<BookingResponse> result = bookingService.getActiveBookings();

        assertThat(result).hasSize(2);
    }

    @Test
    void getBookingsByDateRange_shouldReturnListOfBookings() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(7);

        Booking booking1 = new Booking();
        Booking booking2 = new Booking();
        List<Booking> bookings = Arrays.asList(booking1, booking2);

        BookingResponse response1 = new BookingResponse();
        BookingResponse response2 = new BookingResponse();

        when(bookingRepository.findByStartTimeBetween(start, end)).thenReturn(bookings);
        when(bookingMapper.toResponse(booking1)).thenReturn(response1);
        when(bookingMapper.toResponse(booking2)).thenReturn(response2);

        List<BookingResponse> result = bookingService.getBookingsByDateRange(start, end);

        assertThat(result).hasSize(2);
    }

    @Test
    void getBookingsByDateRange_shouldThrowExceptionWhenStartAfterEnd() {
        LocalDateTime start = LocalDateTime.now().plusDays(7);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        assertThatThrownBy(() -> bookingService.getBookingsByDateRange(start, end))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Начальная дата не может быть позже конечной");
    }
}