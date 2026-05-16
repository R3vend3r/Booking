package booking.controller;

import booking.dto.auth.CreateManagerRequest;
import booking.dto.auth.ManagerResponse;
import booking.dto.auth.UpdateManagerRequest;
import booking.dto.auth.UserResponse;
import booking.dto.response.ClientResponse;
import booking.dto.response.RevenueEntry;
import booking.dto.response.StatsSummary;
import booking.dto.response.TopServiceEntry;
import booking.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users/active")
    public ResponseEntity<List<UserResponse>> getAllActiveClients() {
        return ResponseEntity.ok(adminService.getAllActiveClients());
    }

    @GetMapping("/users/active/paginated")
    public ResponseEntity<Map<String, Object>> getActiveClientsPaginated(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "") String q) {
        if (limit < 1) limit = 10;
        if (offset < 0) offset = 0;
        if (q.isEmpty()) {
            return ResponseEntity.ok(adminService.getActiveClientsPaginated(limit, offset));
        }
        return ResponseEntity.ok(adminService.searchActiveClientsPaginated(q, limit, offset));
    }

    @GetMapping("/clients-with-bookings")
    public ResponseEntity<List<ClientResponse>> getClientsWithBookings(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        if (limit < 1) limit = 10;
        if (offset < 0) offset = 0;
        return ResponseEntity.ok(adminService.getClientsWithBookings(limit, offset));
    }

    @PostMapping("/users/{userId}/disable")
    public ResponseEntity<Void> disableUser(@PathVariable Long userId) {
        adminService.disableUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/enable")
    public ResponseEntity<Void> enableUser(@PathVariable Long userId) {
        adminService.enableUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/reports/revenue/day")
    public ResponseEntity<List<RevenueEntry>> getRevenueByDay() {
        return ResponseEntity.ok(adminService.getRevenueByDay());
    }

    @GetMapping("/reports/revenue/week")
    public ResponseEntity<List<RevenueEntry>> getRevenueByWeek() {
        return ResponseEntity.ok(adminService.getRevenueByWeek());
    }

    @GetMapping("/reports/revenue/month")
    public ResponseEntity<List<RevenueEntry>> getRevenueByMonth() {
        return ResponseEntity.ok(adminService.getRevenueByMonth());
    }

    @GetMapping("/reports/top-services")
    public ResponseEntity<List<TopServiceEntry>> getTopServices() {
        return ResponseEntity.ok(adminService.getTopServices());
    }

    @GetMapping("/reports/summary")
    public ResponseEntity<StatsSummary> getStatsSummary() {
        return ResponseEntity.ok(adminService.getStatsSummary());
    }

    @GetMapping("/managers")
    public ResponseEntity<List<ManagerResponse>> getManagers() {
        return ResponseEntity.ok(adminService.getManagers());
    }

    @PostMapping("/managers")
    public ResponseEntity<ManagerResponse> createManager(@Valid @RequestBody CreateManagerRequest request) {
        return ResponseEntity.ok(adminService.createManager(request));
    }

    @PutMapping("/managers/{id}")
    public ResponseEntity<ManagerResponse> updateManager(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateManagerRequest request) {
        return ResponseEntity.ok(adminService.updateManager(id, request));
    }

    @DeleteMapping("/managers/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable Long id) {
        adminService.deleteManager(id);
        return ResponseEntity.ok().build();
    }
}