package booking.controller;

import booking.dto.auth.AuthResponse;
import booking.dto.auth.ChangePasswordRequest;
import booking.dto.auth.LoginRequest;
import booking.dto.auth.RegistrationRequest;
import booking.dto.auth.UpdateProfileRequest;
import booking.dto.auth.UserProfileResponse;
import booking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registration")
    public AuthResponse registration(@Valid @RequestBody RegistrationRequest request) {
        return authService.registrationUser(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/change-password")
    public void changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.getOldPassword(), request.getNewPassword());
    }

    @GetMapping("/profile")
    public UserProfileResponse getProfile() {
        return authService.getProfile();
    }

    @PutMapping("/profile")
    public void updateProfile(@RequestBody UpdateProfileRequest request) {
        authService.updateProfile(request);
    }
}