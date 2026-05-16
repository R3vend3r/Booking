//package booking.controller;
//
//import booking.dto.request.BookingRequest;
//import booking.dto.response.BookingResponse;
//import booking.exception.GlobalExceptionHandler;
//import booking.exception.ServiceException;
//import booking.service.BookingService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import java.time.LocalDateTime;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@ExtendWith(MockitoExtension.class)
//class BookingControllerTest {
//
//    private MockMvc mockMvc;
//
//    @Mock
//    private BookingService bookingService;
//
//    @InjectMocks
//    private BookingController bookingController;
//
//    private ObjectMapper objectMapper;
//
//    @BeforeEach
//    void setUp() {
//        objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//
//        mockMvc = MockMvcBuilders
//                .standaloneSetup(bookingController)
//                .setControllerAdvice(new GlobalExceptionHandler())
//                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
//                .build();
//    }
//
//    // ==================== CREATE BOOKING ====================
//
//    @Test
//    void createBooking_shouldReturnCreatedWhenSuccess() throws Exception {
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = start.plusHours(2);
//
//        BookingRequest request = new BookingRequest();
//        request.setStartTime(start);
//        request.setEndTime(end);
//        request.setClientId("CLIENT-1");
//        request.setWorkPlaceId("WP-1");
//
//        BookingResponse response = new BookingResponse("B-123", start, end, "CLIENT-1", "WP-1");
//
//        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(response);
//
//        mockMvc.perform(post("/api/bookings")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").value("B-123"))
//                .andExpect(jsonPath("$.clientId").value("CLIENT-1"))
//                .andExpect(jsonPath("$.workPlaceId").value("WP-1"));
//
//        verify(bookingService, times(1)).createBooking(any(BookingRequest.class));
//    }
//
//    @Test
//    void createBooking_shouldReturnBadRequestWhenStartTimeNull() throws Exception {
//        BookingRequest request = new BookingRequest();
//        request.setStartTime(null);
//        request.setEndTime(LocalDateTime.now().plusDays(1));
//        request.setClientId("CLIENT-1");
//        request.setWorkPlaceId("WP-1");
//
//        mockMvc.perform(post("/api/bookings")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        verify(bookingService, never()).createBooking(any(BookingRequest.class));
//    }
//
//    @Test
//    void createBooking_shouldReturnBadRequestWhenClientNotFound() throws Exception {
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = start.plusHours(2);
//
//        BookingRequest request = new BookingRequest();
//        request.setStartTime(start);
//        request.setEndTime(end);
//        request.setClientId("CLIENT-999");
//        request.setWorkPlaceId("WP-1");
//
//        when(bookingService.createBooking(any(BookingRequest.class)))
//                .thenThrow(new ServiceException("Клиент с ID CLIENT-999 не найден"));
//
//        mockMvc.perform(post("/api/bookings")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isNotFound());
//
//        verify(bookingService, times(1)).createBooking(any(BookingRequest.class));
//    }
//
//    @Test
//    void createBooking_shouldReturnBadRequestWhenWorkPlaceNotFound() throws Exception {
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = start.plusHours(2);
//
//        BookingRequest request = new BookingRequest();
//        request.setStartTime(start);
//        request.setEndTime(end);
//        request.setClientId("CLIENT-1");
//        request.setWorkPlaceId("WP-999");
//
//        when(bookingService.createBooking(any(BookingRequest.class)))
//                .thenThrow(new ServiceException("Рабочее место с ID WP-999 не найдено"));
//
//        mockMvc.perform(post("/api/bookings")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isNotFound());
//
//        verify(bookingService, times(1)).createBooking(any(BookingRequest.class));
//    }
//
//    @Test
//    void createBooking_shouldReturnBadRequestWhenWorkplaceOccupied() throws Exception {
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = start.plusHours(2);
//
//        BookingRequest request = new BookingRequest();
//        request.setStartTime(start);
//        request.setEndTime(end);
//        request.setClientId("CLIENT-1");
//        request.setWorkPlaceId("WP-1");
//
//        when(bookingService.createBooking(any(BookingRequest.class)))
//                .thenThrow(new ServiceException("Рабочее место уже забронировано на выбранное время"));
//
//        mockMvc.perform(post("/api/bookings")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        verify(bookingService, times(1)).createBooking(any(BookingRequest.class));
//    }
//
//    // ==================== GET BOOKING BY ID ====================
//
//    @Test
//    void getBookingById_shouldReturnBookingWhenFound() throws Exception {
//        String bookingId = "B-123";
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = start.plusHours(2);
//        BookingResponse response = new BookingResponse(bookingId, start, end, "CLIENT-1", "WP-1");
//
//        when(bookingService.getBookingById(bookingId)).thenReturn(response);
//
//        mockMvc.perform(get("/api/bookings/{id}", bookingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value("B-123"))
//                .andExpect(jsonPath("$.clientId").value("CLIENT-1"));
//
//        verify(bookingService, times(1)).getBookingById(bookingId);
//    }
//
//    @Test
//    void getBookingById_shouldReturnNotFoundWhenMissing() throws Exception {
//        String bookingId = "B-999";
//        when(bookingService.getBookingById(bookingId))
//                .thenThrow(new ServiceException("Бронирование с ID B-999 не найдено"));
//
//        mockMvc.perform(get("/api/bookings/{id}", bookingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//
//        verify(bookingService, times(1)).getBookingById(bookingId);
//    }
//
//    // ==================== GET BOOKING WITH SERVICES ====================
//
//    @Test
//    void getBookingByIdWithServices_shouldReturnBookingWhenFound() throws Exception {
//        String bookingId = "B-123";
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = start.plusHours(2);
//        BookingResponse response = new BookingResponse(bookingId, start, end, "CLIENT-1", "WP-1");
//
//        when(bookingService.getBookingByIdWithServices(bookingId)).thenReturn(response);
//
//        mockMvc.perform(get("/api/bookings/{id}/with-services", bookingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value("B-123"));
//
//        verify(bookingService, times(1)).getBookingByIdWithServices(bookingId);
//    }
//
//    @Test
//    void getBookingByIdWithServices_shouldReturnNotFoundWhenMissing() throws Exception {
//        String bookingId = "B-999";
//        when(bookingService.getBookingByIdWithServices(bookingId))
//                .thenThrow(new ServiceException("Бронирование с ID B-999 не найдено"));
//
//        mockMvc.perform(get("/api/bookings/{id}/with-services", bookingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//
//        verify(bookingService, times(1)).getBookingByIdWithServices(bookingId);
//    }
//
//    // ==================== GET ALL BOOKINGS ====================
//
//    @Test
//    void getAllBookings_shouldReturnListOfBookings() throws Exception {
//        LocalDateTime start1 = LocalDateTime.now().plusDays(1);
//        LocalDateTime end1 = start1.plusHours(2);
//        LocalDateTime start2 = LocalDateTime.now().plusDays(2);
//        LocalDateTime end2 = start2.plusHours(3);
//
//        List<BookingResponse> bookings = Arrays.asList(
//                new BookingResponse("B-123", start1, end1, "CLIENT-1", "WP-1"),
//                new BookingResponse("B-456", start2, end2, "CLIENT-2", "WP-2")
//        );
//
//        when(bookingService.getAllBookings()).thenReturn(bookings);
//
//        mockMvc.perform(get("/api/bookings")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value("B-123"))
//                .andExpect(jsonPath("$[1].id").value("B-456"));
//
//        verify(bookingService, times(1)).getAllBookings();
//    }
//
//    @Test
//    void getAllBookings_shouldReturnEmptyListWhenNoBookings() throws Exception {
//        when(bookingService.getAllBookings()).thenReturn(List.of());
//
//        mockMvc.perform(get("/api/bookings")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$").isArray())
//                .andExpect(jsonPath("$.length()").value(0));
//
//        verify(bookingService, times(1)).getAllBookings();
//    }
//
//    // ==================== GET BOOKINGS BY CLIENT ====================
//
//    @Test
//    void getBookingsByClient_shouldReturnBookingsWhenClientExists() throws Exception {
//        String clientId = "CLIENT-1";
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = start.plusHours(2);
//
//        List<BookingResponse> bookings = Arrays.asList(
//                new BookingResponse("B-123", start, end, clientId, "WP-1")
//        );
//
//        when(bookingService.getBookingsByClient(clientId)).thenReturn(bookings);
//
//        mockMvc.perform(get("/api/bookings/client/{clientId}", clientId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].clientId").value(clientId));
//
//        verify(bookingService, times(1)).getBookingsByClient(clientId);
//    }
//
//    @Test
//    void getBookingsByClient_shouldReturnNotFoundWhenClientMissing() throws Exception {
//        String clientId = "CLIENT-999";
//        when(bookingService.getBookingsByClient(clientId))
//                .thenThrow(new ServiceException("Клиент с ID CLIENT-999 не найден"));
//
//        mockMvc.perform(get("/api/bookings/client/{clientId}", clientId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//
//        verify(bookingService, times(1)).getBookingsByClient(clientId);
//    }
//
//    // ==================== GET BOOKINGS BY WORKPLACE ====================
//
//    @Test
//    void getBookingsByWorkPlace_shouldReturnBookingsWhenWorkPlaceExists() throws Exception {
//        String workPlaceId = "WP-1";
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = start.plusHours(2);
//
//        List<BookingResponse> bookings = Arrays.asList(
//                new BookingResponse("B-123", start, end, "CLIENT-1", workPlaceId)
//        );
//
//        when(bookingService.getBookingsByWorkPlace(workPlaceId)).thenReturn(bookings);
//
//        mockMvc.perform(get("/api/bookings/workplace/{workPlaceId}", workPlaceId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].workPlaceId").value(workPlaceId));
//
//        verify(bookingService, times(1)).getBookingsByWorkPlace(workPlaceId);
//    }
//
//    @Test
//    void getBookingsByWorkPlace_shouldReturnNotFoundWhenWorkPlaceMissing() throws Exception {
//        String workPlaceId = "WP-999";
//        when(bookingService.getBookingsByWorkPlace(workPlaceId))
//                .thenThrow(new ServiceException("Рабочее место с ID WP-999 не найдено"));
//
//        mockMvc.perform(get("/api/bookings/workplace/{workPlaceId}", workPlaceId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//
//        verify(bookingService, times(1)).getBookingsByWorkPlace(workPlaceId);
//    }
//
//    // ==================== GET ACTIVE BOOKINGS ====================
//
//    @Test
//    void getActiveBookings_shouldReturnListOfActiveBookings() throws Exception {
//        LocalDateTime start = LocalDateTime.now().minusHours(1);
//        LocalDateTime end = LocalDateTime.now().plusHours(1);
//
//        List<BookingResponse> bookings = Arrays.asList(
//                new BookingResponse("B-123", start, end, "CLIENT-1", "WP-1")
//        );
//
//        when(bookingService.getActiveBookings()).thenReturn(bookings);
//
//        mockMvc.perform(get("/api/bookings/active")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value("B-123"));
//
//        verify(bookingService, times(1)).getActiveBookings();
//    }
//
//    // ==================== GET BOOKINGS BY DATE RANGE ====================
//
//    @Test
//    void getBookingsByDateRange_shouldReturnBookings() throws Exception {
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = LocalDateTime.now().plusDays(7);
//
//        List<BookingResponse> bookings = Arrays.asList(
//                new BookingResponse("B-123", start, start.plusHours(2), "CLIENT-1", "WP-1")
//        );
//
//        when(bookingService.getBookingsByDateRange(start, end)).thenReturn(bookings);
//
//        mockMvc.perform(get("/api/bookings/date-range")
//                        .param("start", start.toString())
//                        .param("end", end.toString())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value("B-123"));
//
//        verify(bookingService, times(1)).getBookingsByDateRange(start, end);
//    }
//
//    @Test
//    void getBookingsByDateRange_shouldReturnBadRequestWhenStartAfterEnd() throws Exception {
//        LocalDateTime start = LocalDateTime.now().plusDays(7);
//        LocalDateTime end = LocalDateTime.now().plusDays(1);
//
//        when(bookingService.getBookingsByDateRange(start, end))
//                .thenThrow(new ServiceException("Начальная дата не может быть позже конечной"));
//
//        mockMvc.perform(get("/api/bookings/date-range")
//                        .param("start", start.toString())
//                        .param("end", end.toString())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//
//        verify(bookingService, times(1)).getBookingsByDateRange(start, end);
//    }
//
//    // ==================== UPDATE BOOKING ====================
//
//    @Test
//    void updateBooking_shouldUpdateSuccessfully() throws Exception {
//        String bookingId = "B-123";
//        LocalDateTime start = LocalDateTime.now().plusDays(2);
//        LocalDateTime end = start.plusHours(3);
//
//        BookingRequest request = new BookingRequest();
//        request.setStartTime(start);
//        request.setEndTime(end);
//        request.setClientId("CLIENT-1");
//        request.setWorkPlaceId("WP-2");
//
//        BookingResponse response = new BookingResponse(bookingId, start, end, "CLIENT-1", "WP-2");
//
//        when(bookingService.updateBooking(eq(bookingId), any(BookingRequest.class))).thenReturn(response);
//
//        mockMvc.perform(put("/api/bookings/{id}", bookingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.workPlaceId").value("WP-2"));
//
//        verify(bookingService, times(1)).updateBooking(eq(bookingId), any(BookingRequest.class));
//    }
//
//    @Test
//    void updateBooking_shouldReturnNotFoundWhenBookingMissing() throws Exception {
//        String bookingId = "B-999";
//        LocalDateTime start = LocalDateTime.now().plusDays(2);
//        LocalDateTime end = start.plusHours(3);
//
//        BookingRequest request = new BookingRequest();
//        request.setStartTime(start);
//        request.setEndTime(end);
//        request.setClientId("CLIENT-1");
//        request.setWorkPlaceId("WP-1");
//
//        when(bookingService.updateBooking(eq(bookingId), any(BookingRequest.class)))
//                .thenThrow(new ServiceException("Бронирование с ID B-999 не найдено"));
//
//        mockMvc.perform(put("/api/bookings/{id}", bookingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isNotFound());
//
//        verify(bookingService, times(1)).updateBooking(eq(bookingId), any(BookingRequest.class));
//    }
//
//    @Test
//    void updateBooking_shouldReturnBadRequestWhenWorkplaceOccupied() throws Exception {
//        String bookingId = "B-123";
//        LocalDateTime start = LocalDateTime.now().plusDays(2);
//        LocalDateTime end = start.plusHours(3);
//
//        BookingRequest request = new BookingRequest();
//        request.setStartTime(start);
//        request.setEndTime(end);
//        request.setClientId("CLIENT-1");
//        request.setWorkPlaceId("WP-1");
//
//        when(bookingService.updateBooking(eq(bookingId), any(BookingRequest.class)))
//                .thenThrow(new ServiceException("Рабочее место уже забронировано на выбранное время"));
//
//        mockMvc.perform(put("/api/bookings/{id}", bookingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//
//        verify(bookingService, times(1)).updateBooking(eq(bookingId), any(BookingRequest.class));
//    }
//
//    // ==================== DELETE BOOKING ====================
//
//    @Test
//    void deleteBooking_shouldDeleteSuccessfully() throws Exception {
//        String bookingId = "B-123";
//        doNothing().when(bookingService).deleteBooking(bookingId);
//
//        mockMvc.perform(delete("/api/bookings/{id}", bookingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNoContent());
//
//        verify(bookingService, times(1)).deleteBooking(bookingId);
//    }
//
//    @Test
//    void deleteBooking_shouldReturnNotFoundWhenBookingMissing() throws Exception {
//        String bookingId = "B-999";
//        doThrow(new ServiceException("Бронирование с ID B-999 не найдено"))
//                .when(bookingService).deleteBooking(bookingId);
//
//        mockMvc.perform(delete("/api/bookings/{id}", bookingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//
//        verify(bookingService, times(1)).deleteBooking(bookingId);
//    }
//
//    @Test
//    void deleteBooking_shouldReturnBadRequestWhenBookingAlreadyStarted() throws Exception {
//        String bookingId = "B-123";
//        doThrow(new ServiceException("Нельзя отменить уже начавшееся бронирование"))
//                .when(bookingService).deleteBooking(bookingId);
//
//        mockMvc.perform(delete("/api/bookings/{id}", bookingId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest());
//
//        verify(bookingService, times(1)).deleteBooking(bookingId);
//    }
//
//    // ==================== CHECK WORKPLACE AVAILABILITY ====================
//
//    @Test
//    void checkWorkplaceAvailability_shouldReturnTrueWhenAvailable() throws Exception {
//        String workPlaceId = "WP-1";
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = start.plusHours(2);
//
//        when(bookingService.isWorkplaceOccupied(workPlaceId, start, end)).thenReturn(false);
//
//        mockMvc.perform(get("/api/bookings/workplace/{workPlaceId}/check", workPlaceId)
//                        .param("start", start.toString())
//                        .param("end", end.toString())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("true"));
//
//        verify(bookingService, times(1)).isWorkplaceOccupied(workPlaceId, start, end);
//    }
//
//    @Test
//    void checkWorkplaceAvailability_shouldReturnFalseWhenOccupied() throws Exception {
//        String workPlaceId = "WP-1";
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        LocalDateTime end = start.plusHours(2);
//
//        when(bookingService.isWorkplaceOccupied(workPlaceId, start, end)).thenReturn(true);
//
//        mockMvc.perform(get("/api/bookings/workplace/{workPlaceId}/check", workPlaceId)
//                        .param("start", start.toString())
//                        .param("end", end.toString())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string("false"));
//
//        verify(bookingService, times(1)).isWorkplaceOccupied(workPlaceId, start, end);
//    }
//}