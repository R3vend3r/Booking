package booking.service;

import booking.dto.mapper.WorkPlaceMapper;
import booking.dto.request.WorkPlaceRequest;
import booking.dto.response.WorkPlaceResponse;
import booking.entity.Location;
import booking.entity.WorkPlace;
import booking.exception.ServiceException;
import booking.repo.LocationRepository;
import booking.repo.WorkPlaceRepository;
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
class WorkPlaceServiceTest {

    @Mock
    private WorkPlaceRepository workPlaceRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private WorkPlaceMapper workPlaceMapper;

    @InjectMocks
    private WorkPlaceService workPlaceService;

    @Test
    void add_shouldSaveAndReturnResponseWhenSuccess() {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("LOC-123");
        request.setCapacity(4);
        request.setPriceForHour(500);

        Location location = new Location();
        location.setId("LOC-123");

        WorkPlace workPlace = new WorkPlace();
        workPlace.setName("Workstation 1");

        WorkPlace savedWorkPlace = new WorkPlace();
        savedWorkPlace.setId("WP-123");

        WorkPlaceResponse response = new WorkPlaceResponse("WP-123", "Workstation 1", 4, null, "LOC-123", 500, true);

        when(locationRepository.findById("LOC-123")).thenReturn(Optional.of(location));
        when(workPlaceRepository.findByLocationId("LOC-123")).thenReturn(List.of());
        when(workPlaceMapper.toEntity(request)).thenReturn(workPlace);
        when(workPlaceRepository.save(workPlace)).thenReturn(savedWorkPlace);
        when(workPlaceMapper.toResponse(savedWorkPlace)).thenReturn(response);

        WorkPlaceResponse result = workPlaceService.add(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("WP-123");
    }

    @Test
    void add_shouldThrowExceptionWhenLocationNotFound() {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setLocationId("LOC-999");

        when(locationRepository.findById("LOC-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workPlaceService.add(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Локация с ID LOC-999 не найдена");
    }

    @Test
    void add_shouldThrowExceptionWhenNameAlreadyExistsInLocation() {
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Workstation 1");
        request.setLocationId("LOC-123");

        Location location = new Location();
        location.setId("LOC-123");

        WorkPlace existingWorkPlace = new WorkPlace();
        existingWorkPlace.setName("Workstation 1");

        when(locationRepository.findById("LOC-123")).thenReturn(Optional.of(location));
        when(workPlaceRepository.findByLocationId("LOC-123")).thenReturn(List.of(existingWorkPlace));

        assertThatThrownBy(() -> workPlaceService.add(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Рабочее место с названием 'Workstation 1' уже существует в этой локации");
    }

    @Test
    void findById_shouldReturnResponseWhenFound() {
        String id = "WP-123";
        WorkPlace workPlace = new WorkPlace();
        workPlace.setId(id);
        WorkPlaceResponse response = new WorkPlaceResponse();

        when(workPlaceRepository.findById(id)).thenReturn(Optional.of(workPlace));
        when(workPlaceMapper.toResponse(workPlace)).thenReturn(response);

        WorkPlaceResponse result = workPlaceService.findById(id);

        assertThat(result).isNotNull();
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        String id = "WP-999";
        when(workPlaceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workPlaceService.findById(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Данного рабочего места не существует");
    }

    @Test
    void getAll_shouldReturnListOfResponses() {
        List<WorkPlace> workplaces = Arrays.asList(new WorkPlace(), new WorkPlace());
        List<WorkPlaceResponse> responses = Arrays.asList(new WorkPlaceResponse(), new WorkPlaceResponse());

        when(workPlaceRepository.findAll()).thenReturn(workplaces);
        when(workPlaceMapper.toResponse(workplaces.get(0))).thenReturn(responses.get(0));
        when(workPlaceMapper.toResponse(workplaces.get(1))).thenReturn(responses.get(1));

        List<WorkPlaceResponse> result = workPlaceService.getAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void findByLocationId_shouldReturnListOfResponses() {
        String locationId = "LOC-123";
        Location location = new Location();
        List<WorkPlace> workplaces = Arrays.asList(new WorkPlace(), new WorkPlace());
        List<WorkPlaceResponse> responses = Arrays.asList(new WorkPlaceResponse(), new WorkPlaceResponse());

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(workPlaceRepository.findByLocationId(locationId)).thenReturn(workplaces);
        when(workPlaceMapper.toResponse(workplaces.get(0))).thenReturn(responses.get(0));
        when(workPlaceMapper.toResponse(workplaces.get(1))).thenReturn(responses.get(1));

        List<WorkPlaceResponse> result = workPlaceService.findByLocationId(locationId);

        assertThat(result).hasSize(2);
    }

    @Test
    void findAvailableByLocationId_shouldReturnListOfAvailableWorkplaces() {
        String locationId = "LOC-123";
        Location location = new Location();
        List<WorkPlace> workplaces = Arrays.asList(new WorkPlace(), new WorkPlace());

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(workPlaceRepository.findByLocationIdAndAvailable(locationId, true)).thenReturn(workplaces);
        when(workPlaceMapper.toResponse(any(WorkPlace.class))).thenReturn(new WorkPlaceResponse());

        List<WorkPlaceResponse> result = workPlaceService.findAvailableByLocationId(locationId);

        assertThat(result).hasSize(2);
    }

    @Test
    void update_shouldUpdateSuccessfully() {
        String id = "WP-123";
        WorkPlaceRequest request = new WorkPlaceRequest();
        request.setName("Updated Workstation");
        request.setLocationId("LOC-123");

        Location location = new Location();
        location.setId("LOC-123");

        WorkPlace workPlace = new WorkPlace();
        workPlace.setId(id);
        workPlace.setName("Old Name");

        WorkPlace updatedWorkPlace = new WorkPlace();
        updatedWorkPlace.setId(id);
        updatedWorkPlace.setName("Updated Workstation");

        WorkPlaceResponse response = new WorkPlaceResponse();

        when(workPlaceRepository.findById(id)).thenReturn(Optional.of(workPlace));
        when(locationRepository.findById("LOC-123")).thenReturn(Optional.of(location));
        when(workPlaceRepository.findByLocationId("LOC-123")).thenReturn(List.of());
        when(workPlaceRepository.save(workPlace)).thenReturn(updatedWorkPlace);
        when(workPlaceMapper.toResponse(updatedWorkPlace)).thenReturn(response);

        WorkPlaceResponse result = workPlaceService.update(id, request);

        assertThat(result).isNotNull();
    }

    @Test
    void delete_shouldDeleteSuccessfully() {
        String id = "WP-123";
        WorkPlace workPlace = new WorkPlace();
        workPlace.setId(id);
        workPlace.setBookings(List.of());

        when(workPlaceRepository.findById(id)).thenReturn(Optional.of(workPlace));

        workPlaceService.delete(id);

        verify(workPlaceRepository, times(1)).delete(workPlace);
    }

    @Test
    void delete_shouldThrowExceptionWhenHasBookings() {
        String id = "WP-123";
        WorkPlace workPlace = new WorkPlace();
        workPlace.setId(id);
        workPlace.setBookings(List.of(new booking.entity.Booking()));

        when(workPlaceRepository.findById(id)).thenReturn(Optional.of(workPlace));

        assertThatThrownBy(() -> workPlaceService.delete(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Нельзя удалить рабочее место, у которого есть бронирования");
    }

    @Test
    void toggleAvailability_shouldToggleSuccessfully() {
        String id = "WP-123";
        WorkPlace workPlace = new WorkPlace();
        workPlace.setId(id);
        workPlace.setAvailable(true);

        WorkPlace toggledWorkPlace = new WorkPlace();
        toggledWorkPlace.setId(id);
        toggledWorkPlace.setAvailable(false);

        WorkPlaceResponse response = new WorkPlaceResponse();
        response.setAvailable(false);

        when(workPlaceRepository.findById(id)).thenReturn(Optional.of(workPlace));
        when(workPlaceRepository.save(workPlace)).thenReturn(toggledWorkPlace);
        when(workPlaceMapper.toResponse(toggledWorkPlace)).thenReturn(response);

        WorkPlaceResponse result = workPlaceService.toggleAvailability(id);

        assertThat(result.isAvailable()).isFalse();
    }
}