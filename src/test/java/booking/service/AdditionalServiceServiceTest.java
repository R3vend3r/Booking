package booking.service;

import booking.dto.mapper.ServiceMapper;
import booking.dto.request.ServiceRequest;
import booking.dto.response.ServiceResponse;
import booking.entity.AdditionalService;
import booking.exception.ServiceException;
import booking.repo.AdditionalServiceRepository;
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
class AdditionalServiceServiceTest {

    @Mock
    private AdditionalServiceRepository serviceRepository;

    @Mock
    private ServiceMapper serviceMapper;

    @InjectMocks
    private AdditionalServiceService additionalServiceService;

    @Test
    void addService_shouldSaveAndReturnResponseWhenSuccess() {
        ServiceRequest request = new ServiceRequest();
        request.setName("WiFi");
        request.setDescription("High-speed internet");
        request.setPrice(1500);

        AdditionalService entity = new AdditionalService();
        entity.setName("WiFi");

        AdditionalService savedEntity = new AdditionalService();
        savedEntity.setId("S-123");
        savedEntity.setName("WiFi");

        ServiceResponse response = new ServiceResponse("S-123", "WiFi", "High-speed internet", 1500);

        when(serviceRepository.findByName("wifi")).thenReturn(Optional.empty());
        when(serviceMapper.toEntity(request)).thenReturn(entity);
        when(serviceRepository.save(entity)).thenReturn(savedEntity);
        when(serviceMapper.toResponse(savedEntity)).thenReturn(response);

        ServiceResponse result = additionalServiceService.addService(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("S-123");
        assertThat(result.getName()).isEqualTo("WiFi");
        verify(serviceRepository, times(1)).findByName("wifi");
        verify(serviceRepository, times(1)).save(entity);
    }

    @Test
    void addService_shouldThrowExceptionWhenNameAlreadyExists() {
        ServiceRequest request = new ServiceRequest();
        request.setName("WiFi");

        AdditionalService existingService = new AdditionalService();
        existingService.setName("WiFi");

        when(serviceRepository.findByName("wifi")).thenReturn(Optional.of(existingService));

        assertThatThrownBy(() -> additionalServiceService.addService(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Услуга с таким названием уже существует");
        verify(serviceRepository, never()).save(any());
    }

    @Test
    void findServiceById_shouldReturnResponseWhenFound() {
        String id = "S-123";
        AdditionalService service = new AdditionalService();
        service.setId(id);
        service.setName("WiFi");

        ServiceResponse response = new ServiceResponse(id, "WiFi", "Description", 1500);

        when(serviceRepository.findById(id)).thenReturn(Optional.of(service));
        when(serviceMapper.toResponse(service)).thenReturn(response);

        ServiceResponse result = additionalServiceService.findServiceById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        verify(serviceRepository, times(1)).findById(id);
    }

    @Test
    void findServiceById_shouldThrowExceptionWhenNotFound() {
        String id = "S-999";
        when(serviceRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> additionalServiceService.findServiceById(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Такой услуги не существует");
    }

    @Test
    void getAllServices_shouldReturnListOfResponses() {
        List<AdditionalService> services = Arrays.asList(
                new AdditionalService(), new AdditionalService()
        );
        List<ServiceResponse> responses = Arrays.asList(
                new ServiceResponse(), new ServiceResponse()
        );

        when(serviceRepository.findAll()).thenReturn(services);
        when(serviceMapper.toResponse(services.get(0))).thenReturn(responses.get(0));
        when(serviceMapper.toResponse(services.get(1))).thenReturn(responses.get(1));

        List<ServiceResponse> result = additionalServiceService.getAllServices();

        assertThat(result).hasSize(2);
        verify(serviceRepository, times(1)).findAll();
    }

    @Test
    void findServiceByName_shouldReturnResponseWhenFound() {
        String name = "WiFi";
        AdditionalService service = new AdditionalService();
        service.setName(name);
        ServiceResponse response = new ServiceResponse("S-123", name, "Desc", 1500);

        when(serviceRepository.findByName(name)).thenReturn(Optional.of(service));
        when(serviceMapper.toResponse(service)).thenReturn(response);
        ServiceResponse result = additionalServiceService.findServiceByName(name);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    void findServiceByName_shouldThrowExceptionWhenNotFound() {
        String name = "NonExistent";
        when(serviceRepository.findByName(name)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> additionalServiceService.findServiceByName(name))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Такой услуги не существует");
    }

    @Test
    void updateService_shouldUpdateSuccessfully() {
        String id = "S-123";
        ServiceRequest request = new ServiceRequest();
        request.setName("Premium WiFi");
        request.setDescription("Updated");
        request.setPrice(2000);

        AdditionalService existingService = new AdditionalService();
        existingService.setId(id);
        existingService.setName("WiFi");

        AdditionalService updatedService = new AdditionalService();
        updatedService.setId(id);
        updatedService.setName("Premium WiFi");

        ServiceResponse response = new ServiceResponse(id, "Premium WiFi", "Updated", 2000);

        when(serviceRepository.findById(id)).thenReturn(Optional.of(existingService));
        when(serviceRepository.findByName("Premium WiFi")).thenReturn(Optional.empty());
        when(serviceRepository.save(existingService)).thenReturn(updatedService);
        when(serviceMapper.toResponse(updatedService)).thenReturn(response);

        ServiceResponse result = additionalServiceService.updateService(id, request);

        assertThat(result.getName()).isEqualTo("Premium WiFi");
        verify(serviceRepository, times(1)).save(existingService);
    }

    @Test
    void updateService_shouldThrowExceptionWhenDuplicateName() {
        String id = "S-123";
        ServiceRequest request = new ServiceRequest();
        request.setName("Existing Name");

        AdditionalService existingService = new AdditionalService();
        existingService.setId(id);
        existingService.setName("WiFi");

        AdditionalService duplicateService = new AdditionalService();
        duplicateService.setName("Existing Name");

        when(serviceRepository.findById(id)).thenReturn(Optional.of(existingService));
        when(serviceRepository.findByName("Existing Name")).thenReturn(Optional.of(duplicateService));

        assertThatThrownBy(() -> additionalServiceService.updateService(id, request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Услуга с названием 'Existing Name' уже существует");
    }

    @Test
    void deleteService_shouldDeleteSuccessfully() {
        String id = "S-123";
        AdditionalService service = new AdditionalService();
        service.setId(id);
        service.setBookingServices(null);

        when(serviceRepository.findById(id)).thenReturn(Optional.of(service));
        additionalServiceService.deleteService(id);

        verify(serviceRepository, times(1)).delete(service);
    }

    @Test
    void deleteService_shouldThrowExceptionWhenHasBookings() {
        String id = "S-123";
        AdditionalService service = new AdditionalService();
        service.setId(id);
        service.setBookingServices(List.of(new booking.entity.BookingService()));

        when(serviceRepository.findById(id)).thenReturn(Optional.of(service));

        assertThatThrownBy(() -> additionalServiceService.deleteService(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Нельзя удалить услугу, которая используется в бронированиях");
        verify(serviceRepository, never()).delete(any());
    }
}