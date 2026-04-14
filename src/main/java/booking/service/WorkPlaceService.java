package booking.service;

import booking.dto.mapper.WorkPlaceMapper;
import booking.dto.request.WorkPlaceRequest;
import booking.dto.response.WorkPlaceResponse;
import booking.entity.Location;
import booking.entity.WorkPlace;
import booking.exception.ServiceException;
import booking.repo.LocationRepository;
import booking.repo.WorkPlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkPlaceService {
    private final WorkPlaceRepository workPlaceRepository;
    private final LocationRepository locationRepository;
    private final WorkPlaceMapper workPlaceMapper;

    public WorkPlaceService(WorkPlaceRepository workPlaceRepository, LocationRepository locationRepository, WorkPlaceMapper workPlaceMapper) {
        this.workPlaceRepository = workPlaceRepository;
        this.locationRepository = locationRepository;
        this.workPlaceMapper = workPlaceMapper;
    }

    @Transactional
    public WorkPlaceResponse add(WorkPlaceRequest request) {
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ServiceException("Локация с ID " + request.getLocationId() + " не найдена"));

        List<WorkPlace> existing = workPlaceRepository.findByLocationId(request.getLocationId());
        boolean nameExists = existing.stream()
                .anyMatch(wp -> wp.getName().equalsIgnoreCase(request.getName()));

        if (nameExists) {
            throw new ServiceException("Рабочее место с названием '" + request.getName() +
                    "' уже существует в этой локации");
        }

        WorkPlace workPlace = workPlaceMapper.toEntity(request);
        workPlace.setLocation(location);

        WorkPlace saved = workPlaceRepository.save(workPlace);
        return workPlaceMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public WorkPlaceResponse findById(String id){
        WorkPlace workPlace = workPlaceRepository.findById(id).orElseThrow(()-> new ServiceException("Данного рабочего места не существует"));
        return workPlaceMapper.toResponse(workPlace);
    }

    @Transactional(readOnly = true)
    public List<WorkPlaceResponse> getAll(){
        return workPlaceRepository.findAll().stream()
                .map(workPlaceMapper::toResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<WorkPlaceResponse> findByLocationId(String locationId) {
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ServiceException("Локация с ID " + locationId + " не найдена"));

        return workPlaceRepository.findByLocationId(locationId).stream()
                .map(workPlaceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkPlaceResponse> findAvailableByLocationId(String locationId) {
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ServiceException("Локация с ID " + locationId + " не найдена"));

        return workPlaceRepository.findByLocationIdAndAvailable(locationId, true).stream()
                .map(workPlaceMapper::toResponse)
                .collect(Collectors.toList());
    }
    @Transactional
    public WorkPlaceResponse update(String id, WorkPlaceRequest request) {
        WorkPlace workPlace = workPlaceRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Рабочее место с ID " + id + " не найдено"));

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ServiceException("Локация с ID " + request.getLocationId() + " не найдена"));

        if (!workPlace.getName().equalsIgnoreCase(request.getName())) {
            List<WorkPlace> existing = workPlaceRepository.findByLocationId(request.getLocationId());
            boolean nameExists = existing.stream()
                    .anyMatch(wp -> wp.getName().equalsIgnoreCase(request.getName()));

            if (nameExists) {
                throw new ServiceException("Рабочее место с названием '" + request.getName() +
                        "' уже существует в этой локации");
            }
        }

        workPlace.setName(request.getName());
        workPlace.setCapacity(request.getCapacity());
        workPlace.setDescription(request.getDescription());
        workPlace.setPriceForHour(request.getPriceForHour());
        workPlace.setLocation(location);

        WorkPlace updated = workPlaceRepository.save(workPlace);
        return workPlaceMapper.toResponse(updated);
    }
    @Transactional
    public void delete(String id) {
        WorkPlace workPlace = workPlaceRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Рабочее место с ID " + id + " не найдено"));

        if (workPlace.getBookings() != null && !workPlace.getBookings().isEmpty()) {
            throw new ServiceException("Нельзя удалить рабочее место, у которого есть бронирования");
        }

        workPlaceRepository.delete(workPlace);
    }

    @Transactional
    public WorkPlaceResponse toggleAvailability(String id) {
        WorkPlace workPlace = workPlaceRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Рабочее место с ID " + id + " не найдено"));

        workPlace.setAvailable(!workPlace.isAvailable());
        WorkPlace updated = workPlaceRepository.save(workPlace);
        return workPlaceMapper.toResponse(updated);
    }
}
