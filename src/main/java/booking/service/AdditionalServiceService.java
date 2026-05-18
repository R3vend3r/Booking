package booking.service;

import booking.dto.mapper.ServiceMapper;
import booking.dto.request.ServiceRequest;
import booking.dto.response.ServiceResponse;
import booking.entity.AdditionalService;
import booking.exception.ServiceException;
import booking.repo.AdditionalServiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdditionalServiceService {
    private final AdditionalServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;

    public AdditionalServiceService(AdditionalServiceRepository serviceRepository, ServiceMapper serviceMapper) {
        this.serviceRepository = serviceRepository;
        this.serviceMapper = serviceMapper;
    }

    @Transactional
    public ServiceResponse addService(ServiceRequest request){
        if(serviceRepository.findByName(request.getName().toLowerCase()).isPresent()){
           throw new ServiceException("Услуга с таким названием уже существует");
        }

        AdditionalService service = serviceMapper.toEntity(request);
        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Transactional(readOnly = true)
    public ServiceResponse findServiceById(String id){
        AdditionalService service = serviceRepository.findById(id).orElseThrow(() -> new ServiceException("Такая услуга не найдена", HttpStatus.NOT_FOUND));
        return serviceMapper.toResponse(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceResponse> getAllServices(){
        return serviceRepository.findAll().stream()
                .map(serviceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceResponse findServiceByName(String name){
        AdditionalService service = serviceRepository.findByName(name).orElseThrow(() -> new ServiceException("Такая услуга не найдена", HttpStatus.NOT_FOUND));
        return serviceMapper.toResponse(service);
    }

    @Transactional
    public ServiceResponse updateService(String id, ServiceRequest request){
        AdditionalService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Такая услуга не найдена", HttpStatus.NOT_FOUND));

        if (!service.getName().equals(request.getName())) {
            if (serviceRepository.findByName(request.getName()).isPresent()) {
                throw new ServiceException("Услуга с названием '" + request.getName() + "' уже существует", HttpStatus.NOT_FOUND);
            }
        }

        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());

        return serviceMapper.toResponse(serviceRepository.save(service));
    }

    @Transactional
    public void deleteService(String id) {
        AdditionalService service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Услуга с ID " + id + " не найдена", HttpStatus.NOT_FOUND));

        if (service.getBookingServices() != null && !service.getBookingServices().isEmpty()) {
            throw new ServiceException("Нельзя удалить услугу, которая используется в бронированиях");
        }

        serviceRepository.delete(service);
    }
}
