package booking.service;

import booking.dto.mapper.LocationMapper;
import booking.dto.request.LocationRequest;
import booking.dto.response.LocationResponse;
import booking.entity.Location;
import booking.entity.WorkPlace;
import booking.exception.ServiceException;
import booking.repo.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationService locationService;

    @Test
    void addLocation_shouldSaveAndReturnResponseWhenSuccess() {
        LocationRequest request = new LocationRequest();
        request.setBranchName("Central Branch");
        request.setAddress("ул. Ленина, 1");
        request.setCity("Москва");
        request.setOpeningTime(LocalTime.of(9, 0));
        request.setClosingTime(LocalTime.of(21, 0));
        request.setContactPhone("+74951234567");

        Location location = new Location();
        location.setBranchName("Central Branch");

        Location savedLocation = new Location();
        savedLocation.setId("LOC-123");

        LocationResponse response = new LocationResponse("LOC-123", "Central Branch", "ул. Ленина, 1", "Москва",
                LocalTime.of(9, 0), LocalTime.of(21, 0), "+74951234567", 0);

        when(locationRepository.findByBranchNameAndAddressAndCity(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(locationMapper.toEntity(request)).thenReturn(location);
        when(locationRepository.save(location)).thenReturn(savedLocation);
        when(locationMapper.toResponse(savedLocation)).thenReturn(response);

        LocationResponse result = locationService.addLocation(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("LOC-123");
    }

    @Test
    void addLocation_shouldThrowExceptionWhenLocationAlreadyExists() {
        LocationRequest request = new LocationRequest();
        request.setBranchName("Central Branch");
        request.setAddress("ул. Ленина, 1");
        request.setCity("Москва");

        Location existingLocation = new Location();

        when(locationRepository.findByBranchNameAndAddressAndCity(any(), any(), any()))
                .thenReturn(Optional.of(existingLocation));

        assertThatThrownBy(() -> locationService.addLocation(request))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    void addLocation_shouldThrowExceptionWhenOpeningTimeAfterClosingTime() {
        LocationRequest request = new LocationRequest();
        request.setOpeningTime(LocalTime.of(21, 0));
        request.setClosingTime(LocalTime.of(9, 0));

        assertThatThrownBy(() -> locationService.addLocation(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Время открытия не может быть позже времени закрытия");
    }

    @Test
    void findLocationById_shouldReturnResponseWhenFound() {
        String id = "LOC-123";
        Location location = new Location();
        location.setId(id);
        LocationResponse response = new LocationResponse();

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationMapper.toResponse(location)).thenReturn(response);

        LocationResponse result = locationService.findLocationById(id);

        assertThat(result).isNotNull();
    }

    @Test
    void findLocationById_shouldThrowExceptionWhenNotFound() {
        String id = "LOC-999";
        when(locationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.findLocationById(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Локация с таким id не найдена");
    }

    @Test
    void getAllLocation_shouldReturnListOfResponses() {
        List<Location> locations = Arrays.asList(new Location(), new Location());
        List<LocationResponse> responses = Arrays.asList(new LocationResponse(), new LocationResponse());

        when(locationRepository.findAll()).thenReturn(locations);
        when(locationMapper.toResponse(locations.get(0))).thenReturn(responses.get(0));
        when(locationMapper.toResponse(locations.get(1))).thenReturn(responses.get(1));

        List<LocationResponse> result = locationService.getAllLocation();

        assertThat(result).hasSize(2);
    }

    @Test
    void findLocationByCity_shouldReturnListOfResponses() {
        String city = "Москва";
        List<Location> locations = Arrays.asList(new Location(), new Location());
        List<LocationResponse> responses = Arrays.asList(new LocationResponse(), new LocationResponse());

        when(locationRepository.findByCity(city)).thenReturn(locations);
        when(locationMapper.toResponse(locations.get(0))).thenReturn(responses.get(0));
        when(locationMapper.toResponse(locations.get(1))).thenReturn(responses.get(1));

        List<LocationResponse> result = locationService.findLocationByCity(city);
        assertThat(result).hasSize(2);
    }

    @Test
    void update_shouldUpdateSuccessfully() {
        String id = "LOC-123";
        LocationRequest request = new LocationRequest();
        request.setBranchName("Updated Branch");
        request.setAddress("New Address");
        request.setCity("СПб");
        request.setOpeningTime(LocalTime.of(10, 0));
        request.setClosingTime(LocalTime.of(22, 0));

        Location location = new Location();
        location.setId(id);

        Location updatedLocation = new Location();
        updatedLocation.setId(id);
        updatedLocation.setBranchName("Updated Branch");

        LocationResponse response = new LocationResponse();

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationRepository.save(location)).thenReturn(updatedLocation);
        when(locationMapper.toResponse(updatedLocation)).thenReturn(response);

        LocationResponse result = locationService.update(id, request);

        assertThat(result).isNotNull();
        verify(locationRepository, times(1)).save(location);
    }

    @Test
    void delete_shouldDeleteSuccessfully() {
        String id = "LOC-123";
        Location location = new Location();
        location.setId(id);
        location.setWorkplaces(List.of());

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        locationService.delete(id);

        verify(locationRepository, times(1)).delete(location);
    }

    @Test
    void delete_shouldThrowExceptionWhenHasWorkplaces() {
        String id = "LOC-123";
        Location location = new Location();
        location.setId(id);
        location.setWorkplaces(List.of(new WorkPlace()));

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        assertThatThrownBy(() -> locationService.delete(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Нельзя удалить локацию с рабочими местами");
    }

    @Test
    void isOpenNow_shouldReturnTrueWhenOpen() {
        String id = "LOC-123";
        Location location = new Location();
        location.setOpeningTime(LocalTime.of(9, 0));
        location.setClosingTime(LocalTime.of(21, 0));

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        boolean result = locationService.isOpenNow(id);

        assertThat(result).isTrue();
    }

    @Test
    void getWorkplacesCount_shouldReturnCount() {
        String id = "LOC-123";
        Location location = new Location();
        location.setWorkplaces(Arrays.asList(new WorkPlace(), new WorkPlace(), new WorkPlace()));

        when(locationRepository.findById(id)).thenReturn(Optional.of(location));

        int result = locationService.getWorkplacesCount(id);

        assertThat(result).isEqualTo(3);
    }
}