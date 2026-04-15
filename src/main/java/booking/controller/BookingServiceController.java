package booking.controller;

import booking.dto.request.BookingServiceRequest;
import booking.dto.response.BookingServiceResponse;
import booking.service.BookingServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings/services")
public class BookingServiceController {

    private final BookingServiceService bookingServiceService;

    public BookingServiceController(BookingServiceService bookingServiceService) {
        this.bookingServiceService = bookingServiceService;
    }

    @PostMapping
    public ResponseEntity<BookingServiceResponse> addServiceToBooking(@Valid @RequestBody BookingServiceRequest request) {
        BookingServiceResponse response = bookingServiceService.addServiceToBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{bookingId}/{serviceId}")
    public ResponseEntity<BookingServiceResponse> updateServiceQuantity(
            @PathVariable String bookingId,
            @PathVariable String serviceId,
            @RequestParam int quantity) {
        BookingServiceResponse response = bookingServiceService.updateServiceQuantity(bookingId, serviceId, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bookingId}/{serviceId}")
    public ResponseEntity<Void> removeServiceFromBooking(
            @PathVariable String bookingId,
            @PathVariable String serviceId) {
        bookingServiceService.removeServiceFromBooking(bookingId, serviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<BookingServiceResponse>> getServicesByBookingId(@PathVariable String bookingId) {
        List<BookingServiceResponse> responses = bookingServiceService.getServicesByBookingId(bookingId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/service/{serviceId}/total-quantity")
    public ResponseEntity<Integer> getTotalQuantityForService(@PathVariable String serviceId) {
        int total = bookingServiceService.getTotalQuantityForService(serviceId);
        return ResponseEntity.ok(total);
    }
}