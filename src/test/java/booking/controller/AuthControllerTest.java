package booking.controller;

import booking.dto.auth.AuthResponse;
import booking.dto.auth.LoginRequest;
import booking.dto.auth.RegistrationRequest;
import booking.exception.GlobalExceptionHandler;
import booking.exception.ServiceException;
import booking.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    void registration_shouldReturnAuthResponseWhenSuccess() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setBirthday(LocalDate.of(1990, 1, 1));
        request.setPhone("+79991234567");

        AuthResponse response = new AuthResponse("1", "john_doe", "ROLE_USER");

        when(authService.registrationUser(any(RegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.login").value("john_doe"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        verify(authService, times(1)).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenLoginIsBlank() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setEmail("john@example.com");

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenLoginTooShort() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("jo");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setEmail("john@example.com");

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenLoginTooLong() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("a".repeat(51));
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setEmail("john@example.com");

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("");
        request.setFullName("John Doe");
        request.setEmail("john@example.com");

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenPasswordTooShort() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("pass");
        request.setFullName("John Doe");
        request.setEmail("john@example.com");

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenFullNameIsBlank() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");
        request.setFullName("");
        request.setEmail("john@example.com");

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setEmail("");

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenEmailInvalid() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenBirthdayIsFuture() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenPhoneInvalid() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setPhone("invalid-phone");

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenLoginAlreadyExists() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setEmail("john@example.com");

        when(authService.registrationUser(any(RegistrationRequest.class)))
                .thenThrow(new ServiceException("Пользователь с таким логином уже существует"));

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).registrationUser(any(RegistrationRequest.class));
    }

    @Test
    void registration_shouldReturnBadRequestWhenEmailAlreadyExists() throws Exception {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");
        request.setFullName("John Doe");
        request.setEmail("existing@example.com");

        when(authService.registrationUser(any(RegistrationRequest.class)))
                .thenThrow(new ServiceException("Пользователь с таким email уже существует"));

        mockMvc.perform(post("/api/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).registrationUser(any(RegistrationRequest.class));
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void login_shouldReturnAuthResponseWhenSuccess() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");

        AuthResponse response = new AuthResponse("1", "john_doe", "ROLE_USER");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.login").value("john_doe"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void login_shouldReturnBadRequestWhenLoginIsBlank() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin("");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void login_shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin("john_doe");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void login_shouldReturnBadRequestWhenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin("john_doe");
        request.setPassword("wrong");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ServiceException("Неверный логин или пароль"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    void login_shouldReturnBadRequestWhenAccountDisabled() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ServiceException("Аккаунт заблокирован"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }
}