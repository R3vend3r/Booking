package booking.controller;

import booking.dto.request.LocationRequest;
import booking.dto.response.LocationResponse;
import booking.exception.GlobalExceptionHandler;
import booking.exception.ServiceException;
import booking.service.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(locationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
                .build();
    }

    // ==================== CREATE LOCATION ====================

    @Test
    void createLocation_shouldReturnCreatedWhenSuccess() throws Exception {
        LocationRequest request = new LocationRequest();
        request.setBranchName("Central Branch");
        request.setAddress("ул. Ленина, 1");
        request.setCity("Москва");
        request.setOpeningTime(LocalTime.of(9, 0));
        request.setClosingTime(LocalTime.of(21, 0));
        request.setContactPhone("+74951234567");

        LocationResponse response = new LocationResponse(
                "LOC-123", "Central Branch", "ул. Ленина, 1", "Москва",
                LocalTime.of(9, 0), LocalTime.of(21, 0), "+74951234567", 0
        );

        when(locationService.addLocation(any(LocationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("LOC-123"))
                .andExpect(jsonPath("$.name").value("Central Branch"))
                .andExpect(jsonPath("$.city").value("Москва"))
                .andExpect(jsonPath("$.contactPhone").value("+74951234567"));

        verify(locationService, times(1)).addLocation(any(LocationRequest.class));
    }

    @Test
    void createLocation_shouldReturnBadRequestWhenBranchNameIsBlank() throws Exception {
        LocationRequest request = new LocationRequest();
        request.setBranchName("");
        request.setAddress("ул. Ленина, 1");
        request.setCity("Москва");
        request.setContactPhone("+74951234567");

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(locationService, never()).addLocation(any(LocationRequest.class));
    }

    @Test
    void createLocation_shouldReturnBadRequestWhenAddressIsBlank() throws Exception {
        LocationRequest request = new LocationRequest();
        request.setBranchName("Central Branch");
        request.setAddress("");
        request.setCity("Москва");
        request.setContactPhone("+74951234567");

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(locationService, never()).addLocation(any(LocationRequest.class));
    }

    @Test
    void createLocation_shouldReturnBadRequestWhenCityIsBlank() throws Exception {
        LocationRequest request = new LocationRequest();
        request.setBranchName("Central Branch");
        request.setAddress("ул. Ленина, 1");
        request.setCity("");
        request.setContactPhone("+74951234567");

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(locationService, never()).addLocation(any(LocationRequest.class));
    }

    @Test
    void createLocation_shouldReturnBadRequestWhenContactPhoneIsBlank() throws Exception {
        LocationRequest request = new LocationRequest();
        request.setBranchName("Central Branch");
        request.setAddress("ул. Ленина, 1");
        request.setCity("Москва");
        request.setContactPhone("");

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(locationService, never()).addLocation(any(LocationRequest.class));
    }

    @Test
    void createLocation_shouldReturnBadRequestWhenPhoneHasInvalidFormat() throws Exception {
        LocationRequest request = new LocationRequest();
        request.setBranchName("Central Branch");
        request.setAddress("ул. Ленина, 1");
        request.setCity("Москва");
        request.setContactPhone("invalid-phone");

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(locationService, never()).addLocation(any(LocationRequest.class));
    }

    @Test
    void createLocation_shouldReturnBadRequestWhenOpeningTimeAfterClosingTime() throws Exception {
        LocationRequest request = new LocationRequest();
        request.setBranchName("Central Branch");
        request.setAddress("ул. Ленина, 1");
        request.setCity("Москва");
        request.setOpeningTime(LocalTime.of(21, 0));
        request.setClosingTime(LocalTime.of(9, 0));
        request.setContactPhone("+74951234567");

        when(locationService.addLocation(any(LocationRequest.class)))
                .thenThrow(new ServiceException("Время открытия не может быть позже времени закрытия"));

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(locationService, times(1)).addLocation(any(LocationRequest.class));
    }

    @Test
    void createLocation_shouldReturnBadRequestWhenLocationAlreadyExists() throws Exception {
        LocationRequest request = new LocationRequest();
        request.setBranchName("Central Branch");
        request.setAddress("ул. Ленина, 1");
        request.setCity("Москва");
        request.setContactPhone("+74951234567");

        when(locationService.addLocation(any(LocationRequest.class)))
                .thenThrow(new ServiceException("Филиал с названием 'Central Branch' по адресу ул. Ленина, 1 в городе Москва уже существует"));

        mockMvc.perform(post("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(locationService, times(1)).addLocation(any(LocationRequest.class));
    }

    // ==================== GET LOCATION BY ID ====================

    @Test
    void findLocationById_shouldReturnLocationWhenFound() throws Exception {
        String locationId = "LOC-123";
        LocationResponse response = new LocationResponse(
                locationId, "Central Branch", "ул. Ленина, 1", "Москва",
                LocalTime.of(9, 0), LocalTime.of(21, 0), "+74951234567", 5
        );

        when(locationService.findLocationById(locationId)).thenReturn(response);

        mockMvc.perform(get("/api/locations/{id}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("LOC-123"))
                .andExpect(jsonPath("$.name").value("Central Branch"))
                .andExpect(jsonPath("$.city").value("Москва"))
                .andExpect(jsonPath("$.workplacesCount").value(5));

        verify(locationService, times(1)).findLocationById(locationId);
    }

    @Test
    void findLocationById_shouldReturnNotFoundWhenMissing() throws Exception {
        String locationId = "LOC-999";
        when(locationService.findLocationById(locationId))
                .thenThrow(new ServiceException("Локация с таким id не найдена"));

        mockMvc.perform(get("/api/locations/{id}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(locationService, times(1)).findLocationById(locationId);
    }

    // ==================== GET ALL LOCATIONS ====================

    @Test
    void getAllLocations_shouldReturnListOfLocations() throws Exception {
        List<LocationResponse> locations = Arrays.asList(
                new LocationResponse("LOC-123", "Central Branch", "ул. Ленина, 1", "Москва",
                        LocalTime.of(9, 0), LocalTime.of(21, 0), "+74951234567", 5),
                new LocationResponse("LOC-456", "North Branch", "ул. Пушкина, 10", "СПб",
                        LocalTime.of(10, 0), LocalTime.of(20, 0), "+78121234567", 3)
        );

        when(locationService.getAllLocation()).thenReturn(locations);

        mockMvc.perform(get("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("LOC-123"))
                .andExpect(jsonPath("$[0].name").value("Central Branch"))
                .andExpect(jsonPath("$[1].id").value("LOC-456"))
                .andExpect(jsonPath("$[1].name").value("North Branch"));

        verify(locationService, times(1)).getAllLocation();
    }

    @Test
    void getAllLocations_shouldReturnEmptyListWhenNoLocations() throws Exception {
        when(locationService.getAllLocation()).thenReturn(List.of());

        mockMvc.perform(get("/api/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(locationService, times(1)).getAllLocation();
    }

    // ==================== FIND LOCATIONS BY CITY ====================

    @Test
    void findLocationsByCity_shouldReturnLocationsWhenFound() throws Exception {
        String city = "Москва";
        List<LocationResponse> locations = Arrays.asList(
                new LocationResponse("LOC-123", "Central Branch", "ул. Ленина, 1", city,
                        LocalTime.of(9, 0), LocalTime.of(21, 0), "+74951234567", 5),
                new LocationResponse("LOC-456", "South Branch", "ул. Гагарина, 5", city,
                        LocalTime.of(10, 0), LocalTime.of(22, 0), "+74957654321", 2)
        );

        when(locationService.findLocationByCity(city)).thenReturn(locations);

        mockMvc.perform(get("/api/locations/city/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].city").value(city))
                .andExpect(jsonPath("$[1].city").value(city));

        verify(locationService, times(1)).findLocationByCity(city);
    }

    @Test
    void findLocationsByCity_shouldReturnEmptyListWhenNoLocations() throws Exception {
        String city = "UnknownCity";
        when(locationService.findLocationByCity(city)).thenReturn(List.of());

        mockMvc.perform(get("/api/locations/city/{city}", city)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(locationService, times(1)).findLocationByCity(city);
    }

    // ==================== UPDATE LOCATION ====================

    @Test
    void updateLocation_shouldUpdateSuccessfully() throws Exception {
        String locationId = "LOC-123";
        LocationRequest request = new LocationRequest();
        request.setBranchName("Updated Branch");
        request.setAddress("Новый адрес, 10");
        request.setCity("Москва");
        request.setOpeningTime(LocalTime.of(10, 0));
        request.setClosingTime(LocalTime.of(22, 0));
        request.setContactPhone("+74959999999");

        LocationResponse response = new LocationResponse(
                locationId, "Updated Branch", "Новый адрес, 10", "Москва",
                LocalTime.of(10, 0), LocalTime.of(22, 0), "+74959999999", 5
        );

        when(locationService.update(eq(locationId), any(LocationRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/locations/{id}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Branch"))
                .andExpect(jsonPath("$.address").value("Новый адрес, 10"));

        verify(locationService, times(1)).update(eq(locationId), any(LocationRequest.class));
    }

    @Test
    void updateLocation_shouldReturnNotFoundWhenLocationMissing() throws Exception {
        String locationId = "LOC-999";
        LocationRequest request = new LocationRequest();
        request.setBranchName("Updated Branch");
        request.setAddress("Новый адрес, 10");
        request.setCity("Москва");
        request.setContactPhone("+74959999999");

        when(locationService.update(eq(locationId), any(LocationRequest.class)))
                .thenThrow(new ServiceException("Локация с таким id не найдена"));

        mockMvc.perform(put("/api/locations/{id}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(locationService, times(1)).update(eq(locationId), any(LocationRequest.class));
    }

    @Test
    void updateLocation_shouldReturnBadRequestWhenOpeningTimeAfterClosingTime() throws Exception {
        String locationId = "LOC-123";
        LocationRequest request = new LocationRequest();
        request.setBranchName("Updated Branch");
        request.setAddress("Новый адрес, 10");
        request.setCity("Москва");
        request.setOpeningTime(LocalTime.of(22, 0));
        request.setClosingTime(LocalTime.of(9, 0));
        request.setContactPhone("+74959999999");

        when(locationService.update(eq(locationId), any(LocationRequest.class)))
                .thenThrow(new ServiceException("Время открытия не может быть позже времени закрытия"));

        mockMvc.perform(put("/api/locations/{id}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(locationService, times(1)).update(eq(locationId), any(LocationRequest.class));
    }

    @Test
    void updateLocation_shouldReturnBadRequestWhenPhoneHasInvalidFormat() throws Exception {
        String locationId = "LOC-123";
        LocationRequest request = new LocationRequest();
        request.setBranchName("Updated Branch");
        request.setAddress("Новый адрес, 10");
        request.setCity("Москва");
        request.setContactPhone("invalid");

        mockMvc.perform(put("/api/locations/{id}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(locationService, never()).update(eq(locationId), any(LocationRequest.class));
    }

    // ==================== DELETE LOCATION ====================

    @Test
    void deleteLocation_shouldDeleteSuccessfully() throws Exception {
        String locationId = "LOC-123";
        doNothing().when(locationService).delete(locationId);

        mockMvc.perform(delete("/api/locations/{id}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(locationService, times(1)).delete(locationId);
    }

    @Test
    void deleteLocation_shouldReturnNotFoundWhenLocationMissing() throws Exception {
        String locationId = "LOC-999";
        doThrow(new ServiceException("Локация с таким id не найдена"))
                .when(locationService).delete(locationId);

        mockMvc.perform(delete("/api/locations/{id}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(locationService, times(1)).delete(locationId);
    }

    @Test
    void deleteLocation_shouldReturnBadRequestWhenLocationHasWorkplaces() throws Exception {
        String locationId = "LOC-123";
        doThrow(new ServiceException("Нельзя удалить локацию с рабочими местами"))
                .when(locationService).delete(locationId);

        mockMvc.perform(delete("/api/locations/{id}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(locationService, times(1)).delete(locationId);
    }

    // ==================== IS LOCATION OPEN NOW ====================

    @Test
    void isLocationOpenNow_shouldReturnTrueWhenOpen() throws Exception {
        String locationId = "LOC-123";
        when(locationService.isOpenNow(locationId)).thenReturn(true);

        mockMvc.perform(get("/api/locations/{id}/is-open", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(locationService, times(1)).isOpenNow(locationId);
    }

    @Test
    void isLocationOpenNow_shouldReturnFalseWhenClosed() throws Exception {
        String locationId = "LOC-123";
        when(locationService.isOpenNow(locationId)).thenReturn(false);

        mockMvc.perform(get("/api/locations/{id}/is-open", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(locationService, times(1)).isOpenNow(locationId);
    }

    @Test
    void isLocationOpenNow_shouldReturnNotFoundWhenLocationMissing() throws Exception {
        String locationId = "LOC-999";
        when(locationService.isOpenNow(locationId))
                .thenThrow(new ServiceException("Локация с таким id не найдена"));

        mockMvc.perform(get("/api/locations/{id}/is-open", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(locationService, times(1)).isOpenNow(locationId);
    }

    // ==================== GET WORKPLACES COUNT ====================

    @Test
    void getWorkplacesCount_shouldReturnCount() throws Exception {
        String locationId = "LOC-123";
        int count = 10;

        when(locationService.getWorkplacesCount(locationId)).thenReturn(count);

        mockMvc.perform(get("/api/locations/{id}/workplaces-count", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(locationService, times(1)).getWorkplacesCount(locationId);
    }

    @Test
    void getWorkplacesCount_shouldReturnZeroWhenNoWorkplaces() throws Exception {
        String locationId = "LOC-123";
        int count = 0;

        when(locationService.getWorkplacesCount(locationId)).thenReturn(count);

        mockMvc.perform(get("/api/locations/{id}/workplaces-count", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(locationService, times(1)).getWorkplacesCount(locationId);
    }

    @Test
    void getWorkplacesCount_shouldReturnNotFoundWhenLocationMissing() throws Exception {
        String locationId = "LOC-999";
        when(locationService.getWorkplacesCount(locationId))
                .thenThrow(new ServiceException("Локация не найдена"));

        mockMvc.perform(get("/api/locations/{id}/workplaces-count", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(locationService, times(1)).getWorkplacesCount(locationId);
    }
}