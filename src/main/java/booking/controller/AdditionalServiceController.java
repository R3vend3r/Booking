package booking.controller;

import booking.dto.request.ServiceRequest;
import booking.dto.response.ServiceResponse;
import booking.service.AdditionalServiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class AdditionalServiceController {
    private final AdditionalServiceService additionalServiceService;

    public AdditionalServiceController(AdditionalServiceService serviceService) {
        this.additionalServiceService = serviceService;
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(@Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = additionalServiceService.addService(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(@PathVariable String id) {
        ServiceResponse response = additionalServiceService.findServiceById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ServiceResponse> getServiceByName(@RequestParam String name) {
        ServiceResponse response = additionalServiceService.findServiceByName(name);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        List<ServiceResponse> responses = additionalServiceService.getAllServices();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @PathVariable String id,
            @Valid @RequestBody ServiceRequest request) {
        ServiceResponse response = additionalServiceService.updateService(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable String id) {
        additionalServiceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
