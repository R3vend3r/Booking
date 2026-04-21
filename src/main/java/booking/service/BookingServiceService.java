package booking.service;

import booking.dto.mapper.BookingServiceMapper;
import booking.dto.request.BookingServiceRequest;
import booking.dto.response.BookingServiceResponse;
import booking.entity.AdditionalService;
import booking.entity.Booking;
import booking.entity.BookingService;
import booking.entity.BookingServiceId;
import booking.entity.User;
import booking.enums.PaymentStatus;
import booking.exception.ServiceException;
import booking.repo.AdditionalServiceRepository;
import booking.repo.BookingRepository;
import booking.repo.BookingServiceRepository;
import booking.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingServiceService {

    private final BookingServiceRepository bookingServiceRepository;
    private final BookingRepository bookingRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final UserRepository userRepository;
    private final BookingServiceMapper bookingServiceMapper;

    public BookingServiceService(BookingServiceRepository bookingServiceRepository,
                                 BookingRepository bookingRepository,
                                 AdditionalServiceRepository additionalServiceRepository,
                                 UserRepository userRepository,
                                 BookingServiceMapper bookingServiceMapper) {
        this.bookingServiceRepository = bookingServiceRepository;
        this.bookingRepository = bookingRepository;
        this.additionalServiceRepository = additionalServiceRepository;
        this.userRepository = userRepository;
        this.bookingServiceMapper = bookingServiceMapper;
    }

    @Transactional
    public BookingServiceResponse addServiceToBooking(BookingServiceRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ServiceException("Бронирование с ID " + request.getBookingId() + " не найдено"));

        checkBookingOwnership(booking);
        checkBookingNotPaid(booking);

        AdditionalService service = additionalServiceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ServiceException("Услуга с ID " + request.getServiceId() + " не найдена"));

        BookingServiceId id = new BookingServiceId(request.getBookingId(), request.getServiceId());
        if (bookingServiceRepository.findById(id).isPresent()) {
            throw new ServiceException("Услуга уже добавлена в бронирование");
        }

        if (request.getQuantity() <= 0) {
            throw new ServiceException("Количество должно быть больше 0");
        }

        BookingService bookingService = bookingServiceMapper.toEntity(
                request.getBookingId(),
                request.getServiceId(),
                request.getQuantity(),
                service.getPrice()
        );

        bookingService.setBooking(booking);
        bookingService.setService(service);

        BookingService saved = bookingServiceRepository.save(bookingService);
        return bookingServiceMapper.toResponse(saved);
    }

    @Transactional
    public BookingServiceResponse updateServiceQuantity(String bookingId, String serviceId, int quantity) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ServiceException("Бронирование с ID " + bookingId + " не найдено"));

        checkBookingOwnership(booking);
        checkBookingNotPaid(booking);

        BookingServiceId id = new BookingServiceId(bookingId, serviceId);
        BookingService bookingService = bookingServiceRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Услуга не найдена в бронировании"));

        if (quantity <= 0) {
            throw new ServiceException("Количество должно быть больше 0");
        }

        bookingService.setQuantity(quantity);
        BookingService saved = bookingServiceRepository.save(bookingService);
        return bookingServiceMapper.toResponse(saved);
    }

    @Transactional
    public void removeServiceFromBooking(String bookingId, String serviceId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ServiceException("Бронирование с ID " + bookingId + " не найдено"));

        checkBookingOwnership(booking);
        checkBookingNotPaid(booking);

        BookingServiceId id = new BookingServiceId(bookingId, serviceId);
        BookingService bookingService = bookingServiceRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Услуга не найдена в бронировании"));

        bookingServiceRepository.delete(bookingService);
    }

    @Transactional(readOnly = true)
    public List<BookingServiceResponse> getServicesByBookingId(String bookingId) {
        List<BookingService> bookingServices = bookingServiceRepository.findByBookingIdWithService(bookingId);
        return bookingServiceMapper.toResponseList(bookingServices);
    }

    @Transactional(readOnly = true)
    public int getTotalQuantityForService(String serviceId) {
        Integer total = bookingServiceRepository.getTotalQuantityForService(serviceId);
        return total != null ? total : 0;
    }

    private void checkBookingOwnership(Booking booking) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) return;

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ServiceException("Пользователь не найден"));

        if (user.getClient() != null && !user.getClient().getId().equals(booking.getClient().getId())) {
            throw new ServiceException("Вы можете управлять услугами только в своём бронировании");
        }
    }

    private void checkBookingNotPaid(Booking booking) {
        if (booking.getContract() != null && booking.getContract().getPaymentStatus() == PaymentStatus.PAID) {
            throw new ServiceException("Нельзя изменять оплаченное бронирование");
        }
    }
}