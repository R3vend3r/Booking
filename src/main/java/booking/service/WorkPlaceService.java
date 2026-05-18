package booking.service;

import booking.dto.mapper.WorkPlaceMapper;
import booking.dto.request.WorkPlaceRequest;
import booking.dto.response.RecentWorkplaceResponse;
import booking.dto.response.WorkPlaceResponse;
import booking.dto.response.WorkplaceStatusResponse;
import booking.entity.Location;
import booking.entity.WorkPlace;
import booking.exception.ServiceException;
import booking.repo.BookingRepository;
import booking.repo.LocationRepository;
import booking.repo.WorkPlaceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkPlaceService {
    private final WorkPlaceRepository workPlaceRepository;
    private final LocationRepository locationRepository;
    private final BookingRepository bookingRepository;
    private final WorkPlaceMapper workPlaceMapper;

    public WorkPlaceService(WorkPlaceRepository workPlaceRepository, LocationRepository locationRepository, BookingRepository bookingRepository, WorkPlaceMapper workPlaceMapper) {
        this.workPlaceRepository = workPlaceRepository;
        this.locationRepository = locationRepository;
        this.bookingRepository = bookingRepository;
        this.workPlaceMapper = workPlaceMapper;
    }

    @Transactional
    public WorkPlaceResponse add(WorkPlaceRequest request) {
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ServiceException("Локация с ID " + request.getLocationId() + " не найдена", HttpStatus.NOT_FOUND));

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
        WorkPlace workPlace = workPlaceRepository.findById(id).orElseThrow(()-> new ServiceException("Данного рабочего места не существует", HttpStatus.NOT_FOUND));
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
                .orElseThrow(() -> new ServiceException("Локация с ID " + locationId + " не найдена", HttpStatus.NOT_FOUND));

        return workPlaceRepository.findByLocationId(locationId).stream()
                .map(workPlaceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WorkPlaceResponse> findAvailableByLocationId(String locationId) {
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ServiceException("Локация с ID " + locationId + " не найдена", HttpStatus.NOT_FOUND));

        return workPlaceRepository.findByLocationIdAndAvailable(locationId, true).stream()
                .map(workPlaceMapper::toResponse)
                .collect(Collectors.toList());
    }
    @Transactional
    public WorkPlaceResponse update(String id, WorkPlaceRequest request) {
        WorkPlace workPlace = workPlaceRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Рабочее место с ID " + id + " не найдено", HttpStatus.NOT_FOUND));

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ServiceException("Локация с ID " + request.getLocationId() + " не найдена", HttpStatus.NOT_FOUND));

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
                .orElseThrow(() -> new ServiceException("Рабочее место с ID " + id + " не найдено", HttpStatus.NOT_FOUND));

        if (workPlace.getBookings() != null && !workPlace.getBookings().isEmpty()) {
            throw new ServiceException("Нельзя удалить рабочее место, у которого есть бронирования");
        }

        workPlaceRepository.delete(workPlace);
    }

    @Transactional(readOnly = true)
    public List<WorkPlaceResponse> findAvailableByLocationAndTime(String locationId, LocalDateTime startTime, LocalDateTime endTime) {
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ServiceException("Локация с ID " + locationId + " не найдена", HttpStatus.NOT_FOUND));

        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new ServiceException("Время начала должно быть раньше времени окончания");
        }

        LocalDateTime now = LocalDateTime.now();
        return workPlaceRepository.findAvailableByLocationIdAndTimeRange(locationId, startTime, endTime, now).stream()
                .map(workPlaceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WorkPlaceResponse toggleAvailability(String id) {
        WorkPlace workPlace = workPlaceRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Рабочее место с ID " + id + " не найдено", HttpStatus.NOT_FOUND));

        workPlace.setAvailable(!workPlace.isAvailable());
        WorkPlace updated = workPlaceRepository.save(workPlace);
        return workPlaceMapper.toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<WorkplaceStatusResponse> findWorkplacesWithStatusByLocation(String locationId) {
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ServiceException("Локация с ID " + locationId + " не найдена", HttpStatus.NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        return workPlaceRepository.findByLocationId(locationId).stream()
                .map(wp -> {
                    List<Object[]> currentRange = bookingRepository.findCurrentBookingTimeRange(wp.getId(), now);
                    boolean availableNow = currentRange.isEmpty();
                    LocalDateTime currentStart = availableNow ? null : (LocalDateTime) currentRange.get(0)[0];
                    LocalDateTime currentEnd = availableNow ? null : (LocalDateTime) currentRange.get(0)[1];

                    List<Object[]> nextRange = bookingRepository.findNextBookingTimeRange(wp.getId(), now);
                    LocalDateTime nextStart = nextRange.isEmpty() ? null : (LocalDateTime) nextRange.get(0)[0];
                    LocalDateTime nextEnd = nextRange.isEmpty() ? null : (LocalDateTime) nextRange.get(0)[1];

                    return new WorkplaceStatusResponse(
                            wp.getId(), wp.getName(), wp.getCapacity(), wp.getDescription(),
                            wp.getLocation().getId(), wp.getPriceForHour(), wp.isAvailable(),
                            availableNow, currentStart, currentEnd, nextStart, nextEnd
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RecentWorkplaceResponse> getRecentlyBooked(int limit) {
        LocalDateTime now = LocalDateTime.now();
        return workPlaceRepository.findRecentlyBookedWorkplaces(limit).stream()
                .map(wp -> {
                    List<LocalDateTime> currentEnd = bookingRepository.findCurrentBookingEndTime(wp.getId(), now);
                    boolean availableNow = currentEnd.isEmpty();
                    LocalDateTime nextAvailable = availableNow ? null : currentEnd.get(0);
                    String locationName = wp.getLocation() != null ? wp.getLocation().getBranchName() : "";
                    String locationId = wp.getLocation() != null ? wp.getLocation().getId() : "";
                    return new RecentWorkplaceResponse(wp.getId(), wp.getName(), wp.getPriceForHour(),
                            locationId, locationName, availableNow, nextAvailable);
                })
                .collect(Collectors.toList());
    }
}
