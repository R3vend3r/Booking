package booking.service;

import booking.dto.mapper.BookingMapper;
import booking.dto.request.BookingRequest;
import booking.dto.response.BookingResponse;
import booking.entity.Booking;
import booking.entity.Client;
import booking.entity.Location;
import booking.entity.User;
import booking.entity.WorkPlace;
import booking.entity.Contract;
import booking.enums.PaymentStatus;
import booking.exception.ServiceException;
import booking.repo.BookingRepository;
import booking.repo.ClientRepository;
import booking.repo.ContractRepository;
import booking.repo.UserRepository;
import booking.repo.WorkPlaceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final WorkPlaceRepository workPlaceRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final BookingMapper bookingMapper;

    public BookingService(BookingRepository bookingRepository,
                          ClientRepository clientRepository,
                          WorkPlaceRepository workPlaceRepository,
                          UserRepository userRepository,
                          ContractRepository contractRepository,
                          BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
        this.workPlaceRepository = workPlaceRepository;
        this.userRepository = userRepository;
        this.contractRepository = contractRepository;
        this.bookingMapper = bookingMapper;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ServiceException("Пользователь не найден"));

        Client client = user.getClient();
        if (client == null) {
            throw new ServiceException("Клиент не найден");
        }

        return bookingRepository.findByClientIdWithDetails(client.getId()).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ServiceException("Пользователь не найден"));
        Client client = user.getClient();
        if (client == null) {
            throw new ServiceException("Клиент не найден");
        }

        WorkPlace workPlace = workPlaceRepository.findById(request.getWorkPlaceId())
                .orElseThrow(() -> new ServiceException("Рабочее место с ID " + request.getWorkPlaceId() + " не найдено"));

        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new ServiceException("Время окончания должно быть позже времени начала");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException("Время бронирования не может быть в прошлом");
        }

        if (request.getEndTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException("Время окончания бронирования не может быть в прошлом");
        }

        boolean isOccupied = isWorkplaceOccupied(
                request.getWorkPlaceId(),
                request.getStartTime(),
                request.getEndTime(),
                null
        );

        if (isOccupied) {
            throw new ServiceException("Рабочее место уже забронировано на выбранное время");
        }

        Location location = workPlace.getLocation();
        if (!location.isOpenAt(request.getStartTime().toLocalTime())) {
            throw new ServiceException("Время начала вне часов работы локации (" + location.getOpeningTime() + " - " + location.getClosingTime() + ")");
        }
        if (!location.isOpenAt(request.getEndTime().toLocalTime())) {
            throw new ServiceException("Время окончания вне часов работы локации (" + location.getOpeningTime() + " - " + location.getClosingTime() + ")");
        }

        Booking booking = bookingMapper.toEntity(request);
        booking.setClient(client);
        booking.setWorkPlace(workPlace);
//        booking.createContract();

        Booking saved = bookingRepository.save(booking);
        return bookingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingById(String id) {
        Booking booking = bookingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ServiceException("Бронирование с ID " + id + " не найдено"));
        return bookingMapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByIdWithServices(String id) {
        Booking booking = bookingRepository.findByIdWithServices(id)
                .orElseThrow(() -> new ServiceException("Бронирование с ID " + id + " не найдено"));
        return bookingMapper.toResponseWithServices(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAllWithDetails().stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByClient(String clientId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ServiceException("Клиент с ID " + clientId + " не найден"));

        return bookingRepository.findByClientId(clientId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByWorkPlace(String workPlaceId) {
        workPlaceRepository.findById(workPlaceId)
                .orElseThrow(() -> new ServiceException("Рабочее место с ID " + workPlaceId + " не найдено"));

        return bookingRepository.findByWorkPlaceId(workPlaceId).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getActiveBookings() {
        return bookingRepository.findActiveBookings(LocalDateTime.now()).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingResponse updateBooking(String id, BookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Бронирование с ID " + id + " не найдено"));

        checkBookingOwnership(booking);

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new ServiceException("Время начала не может быть позже времени окончания");
        }

        boolean timeChanged = !booking.getStartTime().equals(request.getStartTime()) ||
                !booking.getEndTime().equals(request.getEndTime());

        if (timeChanged && request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException("Нельзя перенести бронирование в прошлое");
        }

        if (!booking.getWorkPlace().getId().equals(request.getWorkPlaceId()) ||
                !booking.getStartTime().equals(request.getStartTime()) ||
                !booking.getEndTime().equals(request.getEndTime())) {

            boolean isOccupied = isWorkplaceOccupied(
                    request.getWorkPlaceId(),
                    request.getStartTime(),
                    request.getEndTime(),
                    id
            );

            if (isOccupied) {
                throw new ServiceException("Рабочее место уже забронировано на выбранное время");
            }
        }

        WorkPlace workPlace = workPlaceRepository.findById(request.getWorkPlaceId())
                .orElseThrow(() -> new ServiceException("Рабочее место не найдено"));

        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setWorkPlace(workPlace);

        Booking updated = bookingRepository.save(booking);
        return bookingMapper.toResponse(updated);
    }

    @Transactional
    public void deleteBooking(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Бронирование с ID " + id + " не найдено"));

        checkBookingOwnership(booking);

        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException("Нельзя отменить уже начавшееся бронирование");
        }

        bookingRepository.delete(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(String id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Бронирование с ID " + id + " не найдено"));

        checkBookingOwnership(booking);

        if (booking.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException("Нельзя отменить уже начавшееся бронирование");
        }

        if (booking.getContract() != null) {
            Contract contract = booking.getContract();
            if (contract.getPaymentStatus() == PaymentStatus.PAID) {
                throw new ServiceException("Нельзя отменить оплаченное бронирование");
            }
            contract.markAsCancelled();
            contractRepository.save(contract);
        } else {
            Contract contract = new Contract();
            contract.setBooking(booking);
            contract.setTotalAmount(booking.getTotalAmount());
            contract.markAsCancelled();
            contractRepository.save(contract);
            booking.setContract(contract);
        }

        return bookingMapper.toResponse(booking);
    }

    private void checkBookingOwnership(Booking booking) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ServiceException("Пользователь не найден"));

        if (user.getClient() != null && !user.getClient().getId().equals(booking.getClient().getId())) {
            throw new ServiceException("Вы можете редактировать только свои бронирования");
        }
    }

    @Transactional(readOnly = true)
    public boolean isWorkplaceOccupied(String workPlaceId, LocalDateTime start, LocalDateTime end, String excludeBookingId) {
        List<Booking> bookings = bookingRepository.findByWorkPlaceId(workPlaceId);

        return bookings.stream()
                .filter(booking -> !booking.getId().equals(excludeBookingId))
                .anyMatch(booking ->
                        (start.isBefore(booking.getEndTime()) && end.isAfter(booking.getStartTime()))
                );
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ServiceException("Начальная дата не может быть позже конечной");
        }
        return bookingRepository.findByStartTimeBetween(start, end).stream()
                .map(bookingMapper::toResponse)
                .collect(Collectors.toList());
    }

}