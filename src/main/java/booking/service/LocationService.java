package booking.service;

import booking.dto.mapper.LocationMapper;
import booking.dto.request.LocationRequest;
import booking.dto.response.LocationResponse;
import booking.entity.Location;
import booking.exception.ServiceException;
import booking.repo.LocationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public LocationService(LocationRepository locationRepository, LocationMapper locationMapper) {
        this.locationRepository = locationRepository;
        this.locationMapper = locationMapper;
    }

    public LocationResponse add(LocationRequest request){
        Location existingLocation = locationRepository
                .findByBranchNameAndAddressAndCity(
                        request.getBranchName(),
                        request.getAddress(),
                        request.getCity()
                )
                .orElse(null);

        if (existingLocation != null) {
            throw new ServiceException(
                    "Филиал с названием '" + request.getBranchName() +
                            "' по адресу " + request.getAddress() +
                            " в городе " + request.getCity() + " уже существует"
            );
        }

        if (request.getOpeningTime() != null && request.getClosingTime() != null &&
                request.getOpeningTime().isAfter(request.getClosingTime())) {
            throw new ServiceException("Время открытия не может быть позже времени закрытия");
        }

        Location location = locationMapper.toEntity(request);
        Location saved = locationRepository.save(location);
        return locationMapper.toResponse(saved);
    }

    public LocationResponse findById(String id){
        Location location = locationRepository.findById(id)
                .orElseThrow(()-> new ServiceException("Локация с таким id не найдена"));
        return locationMapper.toResponse(location);
    }
    public List<LocationResponse> getAll(){
        return locationRepository.findAll().stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<LocationResponse> findByCity(String city){
        return locationRepository.findByCity(city).stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());
    }

    public LocationResponse update(String id, LocationRequest request){
        Location location = locationRepository.findById(id)
                .orElseThrow(()-> new ServiceException("Локация с таким id не найдена"));
        location.setBranchName(request.getBranchName());
        location.setAddress(request.getAddress());
        location.setCity(request.getCity());

        if (request.getOpeningTime() != null) {
            location.setOpeningTime(request.getOpeningTime());
        }
        if (request.getClosingTime() != null) {
            location.setClosingTime(request.getClosingTime());
        }
        if (request.getContactPhone() != null) {
            location.setContactPhone(request.getContactPhone());
        }
        if (location.getOpeningTime() != null && location.getClosingTime() != null &&
                location.getOpeningTime().isAfter(location.getClosingTime())) {
            throw new ServiceException("Время открытия не может быть позже времени закрытия");
        }
        return locationMapper.toResponse(locationRepository.save(location));
    }

    public void delete(String id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Локация с таким id не найдена"));
        if (location.getWorkplaces() != null && !location.getWorkplaces().isEmpty()) {
            throw new ServiceException("Нельзя удалить локацию с рабочими местами");
        }
        locationRepository.delete(location);
    }

    public boolean isOpenNow(String id){
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Локация с таким id не найдена"));
        return location.isOpenAt(LocalTime.now());
    }
}
