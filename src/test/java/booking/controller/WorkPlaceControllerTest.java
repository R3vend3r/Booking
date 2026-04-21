package booking.controller;

import booking.dto.request.WorkPlaceRequest;
import booking.dto.response.WorkPlaceResponse;
import booking.exception.GlobalExceptionHandler;
import booking.exception.ServiceException;
import booking.service.WorkPlaceService;
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
class WorkPlaceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WorkPlaceService workPlaceService;

    @InjectMocks
    private WorkPlaceController workPlaceController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(workPlaceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
                .build();
    }

    // ==================== CREATE WORKPLACE ====================

    @Test
    void createWorkPlace_shouldReturnCreatedWhenSuccess() throws Exception {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("LOC-123");
        request.setCapacity(4);
        request.setDescription("Комфортное рабочее место с компьютером");
        request.setPriceForHour(500);

        WorkPlaceResponse response = new WorkPlaceResponse(
                "WP-123", "Workstation 1", 4, "Комфортное рабочее место с компьютером",
                "LOC-123", 500, true
        );

        when(workPlaceService.add(any(WorkPlaceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("WP-123"))
                .andExpect(jsonPath("$.name").value("Workstation 1"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$.locationId").value("LOC-123"))
                .andExpect(jsonPath("$.priceForHour").value(500))
                .andExpect(jsonPath("$.available").value(true));

        verify(workPlaceService, times(1)).add(any(WorkPlaceRequest.class));
    }

    @Test
    void createWorkPlace_shouldReturnBadRequestWhenNameIsBlank() throws Exception {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("");
        request.setLocationId("LOC-123");
        request.setCapacity(4);
        request.setDescription("Description");
        request.setPriceForHour(500);

        mockMvc.perform(post("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workPlaceService, never()).add(any(WorkPlaceRequest.class));
    }

    @Test
    void createWorkPlace_shouldReturnBadRequestWhenLocationIdIsBlank() throws Exception {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("");
        request.setCapacity(4);
        request.setDescription("Description");
        request.setPriceForHour(500);

        mockMvc.perform(post("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workPlaceService, never()).add(any(WorkPlaceRequest.class));
    }

    @Test
    void createWorkPlace_shouldReturnBadRequestWhenCapacityIsNull() throws Exception {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("LOC-123");
        request.setCapacity(null);
        request.setDescription("Description");
        request.setPriceForHour(500);

        mockMvc.perform(post("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workPlaceService, never()).add(any(WorkPlaceRequest.class));
    }

    @Test
    void createWorkPlace_shouldReturnBadRequestWhenCapacityIsZero() throws Exception {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("LOC-123");
        request.setCapacity(0);
        request.setDescription("Description");
        request.setPriceForHour(500);

        mockMvc.perform(post("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workPlaceService, never()).add(any(WorkPlaceRequest.class));
    }

    @Test
    void createWorkPlace_shouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("LOC-123");
        request.setCapacity(4);
        request.setDescription("");
        request.setPriceForHour(500);

        mockMvc.perform(post("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workPlaceService, never()).add(any(WorkPlaceRequest.class));
    }

    @Test
    void createWorkPlace_shouldReturnBadRequestWhenPriceForHourIsNull() throws Exception {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("LOC-123");
        request.setCapacity(4);
        request.setDescription("Description");
        request.setPriceForHour(null);

        mockMvc.perform(post("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workPlaceService, never()).add(any(WorkPlaceRequest.class));
    }

    @Test
    void createWorkPlace_shouldReturnBadRequestWhenPriceForHourIsNegative() throws Exception {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("LOC-123");
        request.setCapacity(4);
        request.setDescription("Description");
        request.setPriceForHour(-100);

        mockMvc.perform(post("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workPlaceService, never()).add(any(WorkPlaceRequest.class));
    }

    @Test
    void createWorkPlace_shouldReturnNotFoundWhenLocationMissing() throws Exception {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("LOC-999");
        request.setCapacity(4);
        request.setDescription("Description");
        request.setPriceForHour(500);

        when(workPlaceService.add(any(WorkPlaceRequest.class)))
                .thenThrow(new ServiceException("Локация с ID LOC-999 не найдена"));

        mockMvc.perform(post("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(workPlaceService, times(1)).add(any(WorkPlaceRequest.class));
    }

    @Test
    void createWorkPlace_shouldReturnBadRequestWhenNameAlreadyExistsInLocation() throws Exception {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("LOC-123");
        request.setCapacity(4);
        request.setDescription("Description");
        request.setPriceForHour(500);

        when(workPlaceService.add(any(WorkPlaceRequest.class)))
                .thenThrow(new ServiceException("Рабочее место с названием 'Workstation 1' уже существует в этой локации"));

        mockMvc.perform(post("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workPlaceService, times(1)).add(any(WorkPlaceRequest.class));
    }

    // ==================== GET WORKPLACE BY ID ====================

    @Test
    void getWorkPlaceById_shouldReturnWorkPlaceWhenFound() throws Exception {
        String workplaceId = "WP-123";
        WorkPlaceResponse response = new WorkPlaceResponse(
                workplaceId, "Workstation 1", 4, "Description",
                "LOC-123", 500, true
        );

        when(workPlaceService.findById(workplaceId)).thenReturn(response);

        mockMvc.perform(get("/api/workplaces/{id}", workplaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("WP-123"))
                .andExpect(jsonPath("$.name").value("Workstation 1"))
                .andExpect(jsonPath("$.available").value(true));

        verify(workPlaceService, times(1)).findById(workplaceId);
    }

    @Test
    void getWorkPlaceById_shouldReturnNotFoundWhenMissing() throws Exception {
        String workplaceId = "WP-999";
        when(workPlaceService.findById(workplaceId))
                .thenThrow(new ServiceException("Данного рабочего места не существует"));

        mockMvc.perform(get("/api/workplaces/{id}", workplaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(workPlaceService, times(1)).findById(workplaceId);
    }

    // ==================== GET ALL WORKPLACES ====================

    @Test
    void getAllWorkPlaces_shouldReturnListOfWorkplaces() throws Exception {
        List<WorkPlaceResponse> workplaces = Arrays.asList(
                new WorkPlaceResponse("WP-123", "Workstation 1", 4, "Desc1", "LOC-123", 500, true),
                new WorkPlaceResponse("WP-456", "Workstation 2", 2, "Desc2", "LOC-123", 400, true)
        );

        when(workPlaceService.getAll()).thenReturn(workplaces);

        mockMvc.perform(get("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("WP-123"))
                .andExpect(jsonPath("$[1].id").value("WP-456"));

        verify(workPlaceService, times(1)).getAll();
    }

    @Test
    void getAllWorkPlaces_shouldReturnEmptyListWhenNoWorkplaces() throws Exception {
        when(workPlaceService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/workplaces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(workPlaceService, times(1)).getAll();
    }

    // ==================== GET WORKPLACES BY LOCATION ====================

    @Test
    void getWorkPlacesByLocation_shouldReturnWorkplacesWhenLocationExists() throws Exception {
        String locationId = "LOC-123";
        List<WorkPlaceResponse> workplaces = Arrays.asList(
                new WorkPlaceResponse("WP-123", "Workstation 1", 4, "Desc1", locationId, 500, true),
                new WorkPlaceResponse("WP-456", "Workstation 2", 2, "Desc2", locationId, 400, true)
        );

        when(workPlaceService.findByLocationId(locationId)).thenReturn(workplaces);

        mockMvc.perform(get("/api/workplaces/location/{locationId}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].locationId").value(locationId))
                .andExpect(jsonPath("$[1].locationId").value(locationId));

        verify(workPlaceService, times(1)).findByLocationId(locationId);
    }

    @Test
    void getWorkPlacesByLocation_shouldReturnNotFoundWhenLocationMissing() throws Exception {
        String locationId = "LOC-999";
        when(workPlaceService.findByLocationId(locationId))
                .thenThrow(new ServiceException("Локация с ID LOC-999 не найдена"));

        mockMvc.perform(get("/api/workplaces/location/{locationId}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(workPlaceService, times(1)).findByLocationId(locationId);
    }

    @Test
    void getWorkPlacesByLocation_shouldReturnEmptyListWhenNoWorkplaces() throws Exception {
        String locationId = "LOC-123";
        when(workPlaceService.findByLocationId(locationId)).thenReturn(List.of());

        mockMvc.perform(get("/api/workplaces/location/{locationId}", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(workPlaceService, times(1)).findByLocationId(locationId);
    }

    // ==================== GET AVAILABLE WORKPLACES BY LOCATION ====================

    @Test
    void getAvailableWorkPlacesByLocation_shouldReturnAvailableWorkplaces() throws Exception {
        String locationId = "LOC-123";
        List<WorkPlaceResponse> workplaces = Arrays.asList(
                new WorkPlaceResponse("WP-123", "Workstation 1", 4, "Desc1", locationId, 500, true),
                new WorkPlaceResponse("WP-456", "Workstation 2", 2, "Desc2", locationId, 400, true)
        );

        when(workPlaceService.findAvailableByLocationId(locationId)).thenReturn(workplaces);

        mockMvc.perform(get("/api/workplaces/location/{locationId}/available", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[1].available").value(true));

        verify(workPlaceService, times(1)).findAvailableByLocationId(locationId);
    }

    @Test
    void getAvailableWorkPlacesByLocation_shouldReturnNotFoundWhenLocationMissing() throws Exception {
        String locationId = "LOC-999";
        when(workPlaceService.findAvailableByLocationId(locationId))
                .thenThrow(new ServiceException("Локация с ID LOC-999 не найдена"));

        mockMvc.perform(get("/api/workplaces/location/{locationId}/available", locationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(workPlaceService, times(1)).findAvailableByLocationId(locationId);
    }

    // ==================== UPDATE WORKPLACE ====================

    @Test
    void updateWorkPlace_shouldUpdateSuccessfully() throws Exception {
        String workplaceId = "WP-123";
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Updated Workstation");
        request.setLocationId("LOC-123");
        request.setCapacity(6);
        request.setDescription("Updated description");
        request.setPriceForHour(600);

        WorkPlaceResponse response = new WorkPlaceResponse(
                workplaceId, "Updated Workstation", 6, "Updated description",
                "LOC-123", 600, true
        );

        when(workPlaceService.update(eq(workplaceId), any(WorkPlaceRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/workplaces/{id}", workplaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Workstation"))
                .andExpect(jsonPath("$.capacity").value(6))
                .andExpect(jsonPath("$.priceForHour").value(600));

        verify(workPlaceService, times(1)).update(eq(workplaceId), any(WorkPlaceRequest.class));
    }

    @Test
    void updateWorkPlace_shouldReturnNotFoundWhenWorkplaceMissing() throws Exception {
        String workplaceId = "WP-999";
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Updated Workstation");
        request.setLocationId("LOC-123");
        request.setCapacity(4);
        request.setDescription("Description");
        request.setPriceForHour(500);

        when(workPlaceService.update(eq(workplaceId), any(WorkPlaceRequest.class)))
                .thenThrow(new ServiceException("Рабочее место с ID WP-999 не найдено"));

        mockMvc.perform(put("/api/workplaces/{id}", workplaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(workPlaceService, times(1)).update(eq(workplaceId), any(WorkPlaceRequest.class));
    }

    @Test
    void updateWorkPlace_shouldReturnBadRequestWhenDuplicateName() throws Exception {
        String workplaceId = "WP-123";
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Existing Name");
        request.setLocationId("LOC-123");
        request.setCapacity(4);
        request.setDescription("Description");
        request.setPriceForHour(500);

        when(workPlaceService.update(eq(workplaceId), any(WorkPlaceRequest.class)))
                .thenThrow(new ServiceException("Рабочее место с названием 'Existing Name' уже существует в этой локации"));

        mockMvc.perform(put("/api/workplaces/{id}", workplaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workPlaceService, times(1)).update(eq(workplaceId), any(WorkPlaceRequest.class));
    }

    // ==================== DELETE WORKPLACE ====================

    @Test
    void deleteWorkPlace_shouldDeleteSuccessfully() throws Exception {
        String workplaceId = "WP-123";
        doNothing().when(workPlaceService).delete(workplaceId);

        mockMvc.perform(delete("/api/workplaces/{id}", workplaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(workPlaceService, times(1)).delete(workplaceId);
    }

    @Test
    void deleteWorkPlace_shouldReturnNotFoundWhenWorkplaceMissing() throws Exception {
        String workplaceId = "WP-999";
        doThrow(new ServiceException("Рабочее место с ID WP-999 не найдено"))
                .when(workPlaceService).delete(workplaceId);

        mockMvc.perform(delete("/api/workplaces/{id}", workplaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(workPlaceService, times(1)).delete(workplaceId);
    }

    @Test
    void deleteWorkPlace_shouldReturnBadRequestWhenWorkplaceHasBookings() throws Exception {
        String workplaceId = "WP-123";
        doThrow(new ServiceException("Нельзя удалить рабочее место, у которого есть бронирования"))
                .when(workPlaceService).delete(workplaceId);

        mockMvc.perform(delete("/api/workplaces/{id}", workplaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(workPlaceService, times(1)).delete(workplaceId);
    }

    // ==================== TOGGLE AVAILABILITY ====================

    @Test
    void toggleAvailability_shouldToggleSuccessfully() throws Exception {
        String workplaceId = "WP-123";
        WorkPlaceResponse response = new WorkPlaceResponse(
                workplaceId, "Workstation 1", 4, "Description",
                "LOC-123", 500, false
        );

        when(workPlaceService.toggleAvailability(workplaceId)).thenReturn(response);

        mockMvc.perform(patch("/api/workplaces/{id}/toggle-availability", workplaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(workPlaceService, times(1)).toggleAvailability(workplaceId);
    }

    @Test
    void toggleAvailability_shouldReturnNotFoundWhenWorkplaceMissing() throws Exception {
        String workplaceId = "WP-999";
        when(workPlaceService.toggleAvailability(workplaceId))
                .thenThrow(new ServiceException("Рабочее место с ID WP-999 не найдено"));

        mockMvc.perform(patch("/api/workplaces/{id}/toggle-availability", workplaceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(workPlaceService, times(1)).toggleAvailability(workplaceId);
    }
}