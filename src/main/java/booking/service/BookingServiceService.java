package booking.service;

import booking.dto.mapper.BookingServiceMapper;
import booking.dto.request.BookingServiceRequest;
import booking.dto.response.BookingServiceResponse;
import booking.entity.AdditionalService;
import booking.entity.Booking;
import booking.entity.BookingService;
import booking.entity.BookingServiceId;
import booking.exception.ServiceException;
import booking.repo.AdditionalServiceRepository;
import booking.repo.BookingRepository;
import booking.repo.BookingServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingServiceService {

    private final BookingServiceRepository bookingServiceRepository;
    private final BookingRepository bookingRepository;
    private final AdditionalServiceRepository additionalServiceRepository;
    private final BookingServiceMapper bookingServiceMapper;

    public BookingServiceService(BookingServiceRepository bookingServiceRepository,
                                 BookingRepository bookingRepository,
                                 AdditionalServiceRepository additionalServiceRepository,
                                 BookingServiceMapper bookingServiceMapper) {
        this.bookingServiceRepository = bookingServiceRepository;
        this.bookingRepository = bookingRepository;
        this.additionalServiceRepository = additionalServiceRepository;
        this.bookingServiceMapper = bookingServiceMapper;
    }

    @Transactional
    public BookingServiceResponse addServiceToBooking(BookingServiceRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ServiceException("Бронирование с ID " + request.getBookingId() + " не найдено"));

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
}