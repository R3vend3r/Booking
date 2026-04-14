package booking.controller;

import booking.dto.request.WorkPlaceRequest;
import booking.dto.response.WorkPlaceResponse;
import booking.service.WorkPlaceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workplaces")
public class WorkPlaceController {

    private final WorkPlaceService workPlaceService;

    public WorkPlaceController(WorkPlaceService workPlaceService) {
        this.workPlaceService = workPlaceService;
    }

    @PostMapping
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
    @PutMapping("/{id}")
    public ResponseEntity<WorkPlaceResponse> updateWorkPlace(
            @PathVariable String id,
            @Valid @RequestBody WorkPlaceRequest request) {
        WorkPlaceResponse response = workPlaceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkPlace(@PathVariable String id) {
        workPlaceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<WorkPlaceResponse> toggleAvailability(@PathVariable String id) {
        WorkPlaceResponse response = workPlaceService.toggleAvailability(id);
        return ResponseEntity.ok(response);
    }
}