package booking.controller;

import booking.dto.request.ContractRequest;
import booking.dto.response.ContractResponse;
import booking.enums.PaymentMethod;
import booking.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    public ResponseEntity<ContractResponse> createContract(@Valid @RequestBody ContractRequest request) {
        ContractResponse response = contractService.createContract(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContractResponse> getContractById(@PathVariable String id) {
        ContractResponse response = contractService.getContractById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ContractResponse> getContractByBookingId(@PathVariable String bookingId) {
        ContractResponse response = contractService.getContractByBookingId(bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ContractResponse> getContractWithDetails(@PathVariable String id) {
        ContractResponse response = contractService.getContractWithDetails(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ContractResponse>> getAllContracts() {
        List<ContractResponse> responses = contractService.getAllContracts();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/with-bookings")
    public ResponseEntity<List<ContractResponse>> getAllContractsWithBookings() {
        List<ContractResponse> responses = contractService.getAllContractsWithBookings();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<ContractResponse>> getPendingContracts() {
        List<ContractResponse> responses = contractService.getPendingContracts();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{contractId}/pay")
    public ResponseEntity<ContractResponse> payContract(
            @PathVariable String contractId,
            @RequestParam PaymentMethod method) {
        ContractResponse response = contractService.payContract(contractId, method);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{contractId}/cancel")
    public ResponseEntity<ContractResponse> cancelContract(@PathVariable String contractId) {
        ContractResponse response = contractService.cancelContract(contractId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/total-income")
    public ResponseEntity<Long> getTotalIncome() {
        Long total = contractService.getTotalIncome();
        return ResponseEntity.ok(total);
    }
}