package booking.controller;

import booking.dto.request.LocationRequest;
import booking.dto.response.LocationResponse;
import booking.service.LocationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody LocationRequest request) {
        LocationResponse response = locationService.addLocation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> findLocationById(@PathVariable String id) {
        LocationResponse response = locationService.findLocationById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        List<LocationResponse> locations = locationService.getAllLocation();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<LocationResponse>> findLocationsByCity(@PathVariable String city) {
        List<LocationResponse> locations = locationService.findLocationByCity(city);
        return ResponseEntity.ok(locations);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<LocationResponse> updateLocation(
            @PathVariable String id,
            @Valid @RequestBody LocationRequest request) {
        LocationResponse response = locationService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER')")
    public ResponseEntity<Void> deleteLocation(@PathVariable String id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/is-open")
    public ResponseEntity<Boolean> isLocationOpenNow(@PathVariable String id) {
        boolean isOpen = locationService.isOpenNow(id);
        return ResponseEntity.ok(isOpen);
    }

    @GetMapping("/{id}/workplaces-count")
    public ResponseEntity<Integer> getWorkplacesCount(@PathVariable String id) {
        int count = locationService.getWorkplacesCount(id);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/with-available-workplaces")
    public ResponseEntity<List<LocationResponse>> getLocationsWithAvailableWorkplaces() {
        return ResponseEntity.ok(locationService.getLocationsWithAvailableWorkplaces());
    }
}