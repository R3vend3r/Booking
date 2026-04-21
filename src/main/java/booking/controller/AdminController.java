package booking.controller;

import booking.dto.auth.UserResponse;
import booking.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users/active")
    public ResponseEntity<List<UserResponse>> getAllActiveClients() {
        return ResponseEntity.ok(adminService.getAllActiveClients());
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
}