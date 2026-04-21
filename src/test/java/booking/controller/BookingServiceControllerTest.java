package booking.controller;

import booking.dto.request.BookingServiceRequest;
import booking.dto.response.BookingServiceResponse;
import booking.exception.GlobalExceptionHandler;
import booking.exception.ServiceException;
import booking.service.BookingServiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookingServiceService bookingServiceService;

    @InjectMocks
    private BookingServiceController bookingServiceController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingServiceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
                .build();
    }

    // ==================== ADD SERVICE TO BOOKING ====================

    @Test
    void addServiceToBooking_shouldReturnCreatedWhenSuccess() throws Exception {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-123");
        request.setServiceId("S-456");
        request.setQuantity(2);

        BookingServiceResponse response = new BookingServiceResponse(
                "B-123", "S-456", "WiFi", 2, 1500, 3000
        );

        when(bookingServiceService.addServiceToBooking(any(BookingServiceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/bookings/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value("B-123"))
                .andExpect(jsonPath("$.serviceId").value("S-456"))
                .andExpect(jsonPath("$.serviceName").value("WiFi"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(3000));

        verify(bookingServiceService, times(1)).addServiceToBooking(any(BookingServiceRequest.class));
    }

    @Test
    void addServiceToBooking_shouldReturnBadRequestWhenQuantityIsZero() throws Exception {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-123");
        request.setServiceId("S-456");
        request.setQuantity(0);

        mockMvc.perform(post("/api/bookings/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookingServiceService, never()).addServiceToBooking(any(BookingServiceRequest.class));
    }

    @Test
    void addServiceToBooking_shouldReturnBadRequestWhenQuantityNegative() throws Exception {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-123");
        request.setServiceId("S-456");
        request.setQuantity(-1);

        mockMvc.perform(post("/api/bookings/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookingServiceService, never()).addServiceToBooking(any(BookingServiceRequest.class));
    }

    @Test
    void addServiceToBooking_shouldReturnBadRequestWhenBookingIdNull() throws Exception {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId(null);
        request.setServiceId("S-456");
        request.setQuantity(1);

        mockMvc.perform(post("/api/bookings/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookingServiceService, never()).addServiceToBooking(any(BookingServiceRequest.class));
    }

    @Test
    void addServiceToBooking_shouldReturnBadRequestWhenServiceIdNull() throws Exception {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-123");
        request.setServiceId(null);
        request.setQuantity(1);

        mockMvc.perform(post("/api/bookings/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookingServiceService, never()).addServiceToBooking(any(BookingServiceRequest.class));
    }

    @Test
    void addServiceToBooking_shouldReturnNotFoundWhenBookingMissing() throws Exception {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-999");
        request.setServiceId("S-456");
        request.setQuantity(1);

        when(bookingServiceService.addServiceToBooking(any(BookingServiceRequest.class)))
                .thenThrow(new ServiceException("Бронирование с ID B-999 не найдено"));

        mockMvc.perform(post("/api/bookings/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(bookingServiceService, times(1)).addServiceToBooking(any(BookingServiceRequest.class));
    }

    @Test
    void addServiceToBooking_shouldReturnNotFoundWhenServiceMissing() throws Exception {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-123");
        request.setServiceId("S-999");
        request.setQuantity(1);

        when(bookingServiceService.addServiceToBooking(any(BookingServiceRequest.class)))
                .thenThrow(new ServiceException("Услуга с ID S-999 не найдена"));

        mockMvc.perform(post("/api/bookings/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(bookingServiceService, times(1)).addServiceToBooking(any(BookingServiceRequest.class));
    }

    @Test
    void addServiceToBooking_shouldReturnBadRequestWhenServiceAlreadyAdded() throws Exception {
        BookingServiceRequest request = new BookingServiceRequest();
        request.setBookingId("B-123");
        request.setServiceId("S-456");
        request.setQuantity(1);

        when(bookingServiceService.addServiceToBooking(any(BookingServiceRequest.class)))
                .thenThrow(new ServiceException("Услуга уже добавлена в бронирование"));

        mockMvc.perform(post("/api/bookings/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(bookingServiceService, times(1)).addServiceToBooking(any(BookingServiceRequest.class));
    }

    // ==================== UPDATE SERVICE QUANTITY ====================

    @Test
    void updateServiceQuantity_shouldUpdateSuccessfully() throws Exception {
        String bookingId = "B-123";
        String serviceId = "S-456";
        int quantity = 3;

        BookingServiceResponse response = new BookingServiceResponse(
                bookingId, serviceId, "WiFi", 3, 1500, 4500
        );

        when(bookingServiceService.updateServiceQuantity(eq(bookingId), eq(serviceId), eq(quantity)))
                .thenReturn(response);

        mockMvc.perform(put("/api/bookings/services/{bookingId}/{serviceId}", bookingId, serviceId)
                        .param("quantity", String.valueOf(quantity))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(bookingId))
                .andExpect(jsonPath("$.serviceId").value(serviceId))
                .andExpect(jsonPath("$.quantity").value(3))
                .andExpect(jsonPath("$.totalPrice").value(4500));

        verify(bookingServiceService, times(1)).updateServiceQuantity(bookingId, serviceId, quantity);
    }


    @Test
    void updateServiceQuantity_shouldReturnBadRequestWhenQuantityZero() throws Exception {
        String bookingId = "B-123";
        String serviceId = "S-456";
        int quantity = 0;

        when(bookingServiceService.updateServiceQuantity(eq(bookingId), eq(serviceId), eq(quantity)))
                .thenThrow(new ServiceException("Количество должно быть больше 0"));

        mockMvc.perform(put("/api/bookings/services/{bookingId}/{serviceId}", bookingId, serviceId)
                        .param("quantity", String.valueOf(quantity))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingServiceService, times(1)).updateServiceQuantity(bookingId, serviceId, quantity);
    }

    @Test
    void updateServiceQuantity_shouldReturnBadRequestWhenQuantityNegative() throws Exception {
        String bookingId = "B-123";
        String serviceId = "S-456";
        int quantity = -5;

        when(bookingServiceService.updateServiceQuantity(eq(bookingId), eq(serviceId), eq(quantity)))
                .thenThrow(new ServiceException("Количество должно быть больше 0"));

        mockMvc.perform(put("/api/bookings/services/{bookingId}/{serviceId}", bookingId, serviceId)
                        .param("quantity", String.valueOf(quantity))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingServiceService, times(1)).updateServiceQuantity(bookingId, serviceId, quantity);
    }

    @Test
    void updateServiceQuantity_shouldReturnNotFoundWhenServiceNotInBooking() throws Exception {
        String bookingId = "B-123";
        String serviceId = "S-999";
        int quantity = 2;

        when(bookingServiceService.updateServiceQuantity(bookingId, serviceId, quantity))
                .thenThrow(new ServiceException("Услуга не найдена в бронировании"));

        mockMvc.perform(put("/api/bookings/services/{bookingId}/{serviceId}", bookingId, serviceId)
                        .param("quantity", String.valueOf(quantity))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingServiceService, times(1)).updateServiceQuantity(bookingId, serviceId, quantity);
    }

    // ==================== REMOVE SERVICE FROM BOOKING ====================

    @Test
    void removeServiceFromBooking_shouldRemoveSuccessfully() throws Exception {
        String bookingId = "B-123";
        String serviceId = "S-456";

        doNothing().when(bookingServiceService).removeServiceFromBooking(bookingId, serviceId);

        mockMvc.perform(delete("/api/bookings/services/{bookingId}/{serviceId}", bookingId, serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bookingServiceService, times(1)).removeServiceFromBooking(bookingId, serviceId);
    }

    @Test
    void removeServiceFromBooking_shouldReturnNotFoundWhenServiceNotInBooking() throws Exception {
        String bookingId = "B-123";
        String serviceId = "S-999";

        doThrow(new ServiceException("Услуга не найдена в бронировании"))
                .when(bookingServiceService).removeServiceFromBooking(bookingId, serviceId);

        mockMvc.perform(delete("/api/bookings/services/{bookingId}/{serviceId}", bookingId, serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingServiceService, times(1)).removeServiceFromBooking(bookingId, serviceId);
    }

    // ==================== GET SERVICES BY BOOKING ID ====================

    @Test
    void getServicesByBookingId_shouldReturnListOfServices() throws Exception {
        String bookingId = "B-123";

        List<BookingServiceResponse> responses = Arrays.asList(
                new BookingServiceResponse(bookingId, "S-456", "WiFi", 2, 1500, 3000),
                new BookingServiceResponse(bookingId, "S-789", "Parking", 1, 2000, 2000)
        );

        when(bookingServiceService.getServicesByBookingId(bookingId)).thenReturn(responses);

        mockMvc.perform(get("/api/bookings/services/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookingId").value(bookingId))
                .andExpect(jsonPath("$[0].serviceName").value("WiFi"))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[1].serviceName").value("Parking"))
                .andExpect(jsonPath("$[1].quantity").value(1));

        verify(bookingServiceService, times(1)).getServicesByBookingId(bookingId);
    }

    @Test
    void getServicesByBookingId_shouldReturnEmptyListWhenNoServices() throws Exception {
        String bookingId = "B-123";

        when(bookingServiceService.getServicesByBookingId(bookingId)).thenReturn(List.of());

        mockMvc.perform(get("/api/bookings/services/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookingServiceService, times(1)).getServicesByBookingId(bookingId);
    }

    @Test
    void getServicesByBookingId_shouldReturnNotFoundWhenBookingMissing() throws Exception {
        String bookingId = "B-999";

        when(bookingServiceService.getServicesByBookingId(bookingId))
                .thenThrow(new ServiceException("Бронирование с ID B-999 не найдено"));

        mockMvc.perform(get("/api/bookings/services/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingServiceService, times(1)).getServicesByBookingId(bookingId);
    }

    // ==================== GET TOTAL QUANTITY FOR SERVICE ====================

    @Test
    void getTotalQuantityForService_shouldReturnTotalQuantity() throws Exception {
        String serviceId = "S-456";
        int totalQuantity = 15;

        when(bookingServiceService.getTotalQuantityForService(serviceId)).thenReturn(totalQuantity);

        mockMvc.perform(get("/api/bookings/services/service/{serviceId}/total-quantity", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("15"));

        verify(bookingServiceService, times(1)).getTotalQuantityForService(serviceId);
    }

    @Test
    void getTotalQuantityForService_shouldReturnZeroWhenNoBookings() throws Exception {
        String serviceId = "S-456";

        when(bookingServiceService.getTotalQuantityForService(serviceId)).thenReturn(0);

        mockMvc.perform(get("/api/bookings/services/service/{serviceId}/total-quantity", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(bookingServiceService, times(1)).getTotalQuantityForService(serviceId);
    }

    @Test
    void getTotalQuantityForService_shouldReturnNotFoundWhenServiceMissing() throws Exception {
        String serviceId = "S-999";

        when(bookingServiceService.getTotalQuantityForService(serviceId))
                .thenThrow(new ServiceException("Услуга с ID S-999 не найдена"));

        mockMvc.perform(get("/api/bookings/services/service/{serviceId}/total-quantity", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(bookingServiceService, times(1)).getTotalQuantityForService(serviceId);
    }
}