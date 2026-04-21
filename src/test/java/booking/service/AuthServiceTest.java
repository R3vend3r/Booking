package booking.service;

import booking.dto.auth.AuthResponse;
import booking.dto.auth.LoginRequest;
import booking.dto.auth.RegistrationRequest;
import booking.entity.Client;
import booking.entity.User;
import booking.enums.Role;
import booking.exception.ServiceException;
import booking.repo.ClientRepository;
import booking.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void registrationUser_shouldSaveAndReturnResponseWhenSuccess() {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");
        request.setEmail("john@example.com");
        request.setFullName("John Doe");
        request.setPhone("+79991234567");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        User user = new User();
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setLogin("john_doe");
        savedUser.setRole(Role.ROLE_USER);

        Client client = new Client();

        when(userRepository.findByLogin("john_doe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(clientRepository.findByPhone("+79991234567")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        AuthResponse result = authService.registrationUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("1");
        assertThat(result.getLogin()).isEqualTo("john_doe");
        assertThat(result.getRole()).isEqualTo("ROLE_USER");
        verify(userRepository, times(1)).save(any(User.class));
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void registrationUser_shouldThrowExceptionWhenLoginAlreadyExists() {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");

        when(userRepository.findByLogin("john_doe")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.registrationUser(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Пользователь с таким логином уже существует");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registrationUser_shouldThrowExceptionWhenEmailAlreadyExists() {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setEmail("existing@example.com");

        when(userRepository.findByLogin("john_doe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.registrationUser(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Пользователь с таким email уже существует");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registrationUser_shouldThrowExceptionWhenPhoneAlreadyExists() {
        RegistrationRequest request = new RegistrationRequest();
        request.setLogin("john_doe");
        request.setEmail("john@example.com");
        request.setPhone("+79991234567");

        when(userRepository.findByLogin("john_doe")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(clientRepository.findByPhone("+79991234567")).thenReturn(Optional.of(new Client()));

        assertThatThrownBy(() -> authService.registrationUser(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Клиент с таким номером телефона уже существует");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_shouldReturnResponseWhenSuccess() {
        LoginRequest request = new LoginRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setLogin("john_doe");
        user.setPassword("encodedPassword");
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);

        when(userRepository.findByLogin("john_doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        AuthResponse result = authService.login(request);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("1");
        assertThat(result.getLogin()).isEqualTo("john_doe");
        assertThat(result.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void login_shouldThrowExceptionWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setLogin("john_doe");

        when(userRepository.findByLogin("john_doe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Неверный логин или пароль");
    }

    @Test
    void login_shouldThrowExceptionWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setLogin("john_doe");
        request.setPassword("wrong");

        User user = new User();
        user.setPassword("encodedPassword");

        when(userRepository.findByLogin("john_doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Неверный логин или пароль");
    }

    @Test
    void login_shouldThrowExceptionWhenAccountDisabled() {
        LoginRequest request = new LoginRequest();
        request.setLogin("john_doe");
        request.setPassword("password123");

        User user = new User();
        user.setPassword("encodedPassword");
        user.setEnabled(false);

        when(userRepository.findByLogin("john_doe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Аккаунт заблокирован");
    }
}