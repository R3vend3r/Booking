package booking.controller;

import booking.dto.request.BookingRequest;
import booking.dto.response.BookingResponse;
import booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable String id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/with-services")
    public ResponseEntity<BookingResponse> getBookingByIdWithServices(@PathVariable String id) {
        BookingResponse response = bookingService.getBookingByIdWithServices(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> responses = bookingService.getAllBookings();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        List<BookingResponse> responses = bookingService.getMyBookings();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByClient(@PathVariable String clientId) {
        List<BookingResponse> responses = bookingService.getBookingsByClient(clientId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/workplace/{workPlaceId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<BookingResponse>> getBookingsByWorkPlace(@PathVariable String workPlaceId) {
        List<BookingResponse> responses = bookingService.getBookingsByWorkPlace(workPlaceId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<BookingResponse>> getActiveBookings() {
        List<BookingResponse> responses = bookingService.getActiveBookings();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<List<BookingResponse>> getBookingsByDateRange(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        List<BookingResponse> responses = bookingService.getBookingsByDateRange(start, end);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable String id,
            @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable String id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String id) {
        BookingResponse response = bookingService.cancelBooking(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/workplace/{workPlaceId}/check")
    public ResponseEntity<Boolean> checkWorkplaceAvailability(
            @PathVariable String workPlaceId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end,
            @RequestParam(required = false) String excludeBookingId) {
        boolean isAvailable = !bookingService.isWorkplaceOccupied(workPlaceId, start, end, excludeBookingId);
        return ResponseEntity.ok(isAvailable);
    }
}
