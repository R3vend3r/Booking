package booking.controller;

import booking.dto.request.WorkPlaceRequest;
import booking.dto.response.RecentWorkplaceResponse;
import booking.dto.response.WorkPlaceResponse;
import booking.service.WorkPlaceService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/workplaces")
public class WorkPlaceController {

    private final WorkPlaceService workPlaceService;

    public WorkPlaceController(WorkPlaceService workPlaceService) {
        this.workPlaceService = workPlaceService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<WorkPlaceResponse> createWorkPlace(@Valid @RequestBody WorkPlaceRequest request) {
        WorkPlaceResponse response = workPlaceService.add(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkPlaceResponse> getWorkPlaceById(@PathVariable String id) {
        WorkPlaceResponse response = workPlaceService.findById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<WorkPlaceResponse>> getAllWorkPlaces() {
        List<WorkPlaceResponse> responses = workPlaceService.getAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<WorkPlaceResponse>> getWorkPlacesByLocation(@PathVariable String locationId) {
        List<WorkPlaceResponse> responses = workPlaceService.findByLocationId(locationId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/location/{locationId}/available")
    public ResponseEntity<List<WorkPlaceResponse>> getAvailableWorkPlacesByLocation(@PathVariable String locationId) {
        List<WorkPlaceResponse> responses = workPlaceService.findAvailableByLocationId(locationId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/location/{locationId}/available-by-time")
    public ResponseEntity<List<WorkPlaceResponse>> getAvailableWorkPlacesByLocationAndTime(
            @PathVariable String locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<WorkPlaceResponse> responses = workPlaceService.findAvailableByLocationAndTime(locationId, startTime, endTime);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<WorkPlaceResponse> updateWorkPlace(
            @PathVariable String id,
            @Valid @RequestBody WorkPlaceRequest request) {
        WorkPlaceResponse response = workPlaceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> deleteWorkPlace(@PathVariable String id) {
        workPlaceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<WorkPlaceResponse> toggleAvailability(@PathVariable String id) {
        WorkPlaceResponse response = workPlaceService.toggleAvailability(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recently-booked")
    public ResponseEntity<List<RecentWorkplaceResponse>> getRecentlyBooked(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(workPlaceService.getRecentlyBooked(limit));
    }
}