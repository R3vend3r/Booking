package booking.controller;

import booking.dto.request.ServiceRequest;
import booking.dto.response.ServiceResponse;
import booking.exception.GlobalExceptionHandler;
import booking.exception.ServiceException;
import booking.service.AdditionalServiceService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdditionalServiceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdditionalServiceService additionalServiceService;

    @InjectMocks
    private AdditionalServiceController additionalServiceController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(additionalServiceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
                .build();
    }

    @Test
    void createService_shouldReturnCreatedWhenSuccess() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("WiFi");
        request.setDescription("High-speed internet access");
        request.setPrice(1500);

        ServiceResponse response = new ServiceResponse("S-123", "WiFi", "High-speed internet access", 1500);

        when(additionalServiceService.addService(any(ServiceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("S-123"))
                .andExpect(jsonPath("$.name").value("WiFi"))
                .andExpect(jsonPath("$.description").value("High-speed internet access"))
                .andExpect(jsonPath("$.price").value(1500));

        verify(additionalServiceService, times(1)).addService(any(ServiceRequest.class));
    }

    @Test
    void createService_shouldReturnBadRequestWhenValidationFails() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("");
        request.setDescription("Test");
        request.setPrice(1000);

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(additionalServiceService, never()).addService(any(ServiceRequest.class));
    }

    @Test
    void createService_shouldReturnBadRequestWhenPriceNegative() throws Exception {
        ServiceRequest request = new ServiceRequest();
        request.setName("WiFi");
        request.setDescription("Test");
        request.setPrice(-100);

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(additionalServiceService, never()).addService(any(ServiceRequest.class));
    }

    @Test
    void getServiceById_shouldReturnServiceWhenFound() throws Exception {
        String serviceId = "S-123";
        ServiceResponse response = new ServiceResponse(serviceId, "WiFi", "High-speed internet", 1500);

        when(additionalServiceService.findServiceById(serviceId)).thenReturn(response);

        mockMvc.perform(get("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("S-123"))
                .andExpect(jsonPath("$.name").value("WiFi"))
                .andExpect(jsonPath("$.price").value(1500));

        verify(additionalServiceService, times(1)).findServiceById(serviceId);
    }

    @Test
    void getServiceById_shouldReturnNotFoundWhenMissing() throws Exception {
        String serviceId = "S-999";
        when(additionalServiceService.findServiceById(serviceId))
                .thenThrow(new ServiceException("Такой услуги не существует"));

        mockMvc.perform(get("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(additionalServiceService, times(1)).findServiceById(serviceId);
    }

    @Test
    void getServiceByName_shouldReturnServiceWhenFound() throws Exception {
        String serviceName = "WiFi";
        ServiceResponse response = new ServiceResponse("S-123", "WiFi", "High-speed internet", 1500);

        when(additionalServiceService.findServiceByName(serviceName)).thenReturn(response);

        mockMvc.perform(get("/api/services/search")
                        .param("name", serviceName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("S-123"))
                .andExpect(jsonPath("$.name").value("WiFi"))
                .andExpect(jsonPath("$.price").value(1500));

        verify(additionalServiceService, times(1)).findServiceByName(serviceName);
    }

    @Test
    void getServiceByName_shouldReturnNotFoundWhenMissing() throws Exception {
        String serviceName = "NonExistent";
        when(additionalServiceService.findServiceByName(serviceName))
                .thenThrow(new ServiceException("Такой услуги не существует"));

        mockMvc.perform(get("/api/services/search")
                        .param("name", serviceName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(additionalServiceService, times(1)).findServiceByName(serviceName);
    }

    @Test
    void getAllServices_shouldReturnListOfServices() throws Exception {
        List<ServiceResponse> services = Arrays.asList(
                new ServiceResponse("S-123", "WiFi", "High-speed internet", 1500),
                new ServiceResponse("S-456", "Parking", "Secure parking", 2000)
        );

        when(additionalServiceService.getAllServices()).thenReturn(services);

        mockMvc.perform(get("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("S-123"))
                .andExpect(jsonPath("$[0].name").value("WiFi"))
                .andExpect(jsonPath("$[1].id").value("S-456"))
                .andExpect(jsonPath("$[1].name").value("Parking"));

        verify(additionalServiceService, times(1)).getAllServices();
    }

    @Test
    void getAllServices_shouldReturnEmptyListWhenNoServices() throws Exception {
        when(additionalServiceService.getAllServices()).thenReturn(List.of());

        mockMvc.perform(get("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(additionalServiceService, times(1)).getAllServices();
    }

    @Test
    void updateService_shouldUpdateSuccessfully() throws Exception {
        String serviceId = "S-123";
        ServiceRequest request = new ServiceRequest();
        request.setName("Premium WiFi");
        request.setDescription("Very fast internet");
        request.setPrice(2500);

        ServiceResponse response = new ServiceResponse(serviceId, "Premium WiFi", "Very fast internet", 2500);

        when(additionalServiceService.updateService(eq(serviceId), any(ServiceRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("S-123"))
                .andExpect(jsonPath("$.name").value("Premium WiFi"))
                .andExpect(jsonPath("$.price").value(2500));

        verify(additionalServiceService, times(1)).updateService(eq(serviceId), any(ServiceRequest.class));
    }

    @Test
    void updateService_shouldReturnNotFoundWhenServiceMissing() throws Exception {
        String serviceId = "S-999";
        ServiceRequest request = new ServiceRequest();
        request.setName("WiFi");
        request.setDescription("Test");
        request.setPrice(1000);

        when(additionalServiceService.updateService(eq(serviceId), any(ServiceRequest.class)))
                .thenThrow(new ServiceException("Такой услуги не существует"));

        mockMvc.perform(put("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(additionalServiceService, times(1)).updateService(eq(serviceId), any(ServiceRequest.class));
    }

    @Test
    void updateService_shouldReturnBadRequestWhenDuplicateName() throws Exception {
        String serviceId = "S-123";
        ServiceRequest request = new ServiceRequest();
        request.setName("Existing Service");
        request.setDescription("Test");
        request.setPrice(1000);

        when(additionalServiceService.updateService(eq(serviceId), any(ServiceRequest.class)))
                .thenThrow(new ServiceException("Услуга с названием 'Existing Service' уже существует"));

        mockMvc.perform(put("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(additionalServiceService, times(1)).updateService(eq(serviceId), any(ServiceRequest.class));
    }

    @Test
    void deleteService_shouldDeleteSuccessfully() throws Exception {
        String serviceId = "S-123";
        doNothing().when(additionalServiceService).deleteService(serviceId);

        mockMvc.perform(delete("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(additionalServiceService, times(1)).deleteService(serviceId);
    }

    @Test
    void deleteService_shouldReturnNotFoundWhenServiceMissing() throws Exception {
        String serviceId = "S-999";
        doThrow(new ServiceException("Услуга с ID S-999 не найдена"))
                .when(additionalServiceService).deleteService(serviceId);

        mockMvc.perform(delete("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(additionalServiceService, times(1)).deleteService(serviceId);
    }

    @Test
    void deleteService_shouldReturnBadRequestWhenServiceHasBookings() throws Exception {
        String serviceId = "S-123";
        doThrow(new ServiceException("Нельзя удалить услугу, которая используется в бронированиях"))
                .when(additionalServiceService).deleteService(serviceId);

        mockMvc.perform(delete("/api/services/{id}", serviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(additionalServiceService, times(1)).deleteService(serviceId);
    }
}