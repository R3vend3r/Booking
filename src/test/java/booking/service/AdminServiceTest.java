package booking.service;

import booking.dto.auth.UserResponse;
import booking.entity.Client;
import booking.entity.User;
import booking.enums.Role;
import booking.exception.ServiceException;
import booking.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getAllActiveClients_shouldReturnListOfUserResponses() {
        User user1 = createUser(1L, "john_doe", "john@example.com", true);
        User user2 = createUser(2L, "jane_doe", "jane@example.com", true);

        when(userRepository.findAllActiveClients()).thenReturn(Arrays.asList(user1, user2));

        List<UserResponse> result = adminService.getAllActiveClients();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLogin()).isEqualTo("john_doe");
        assertThat(result.get(1).getLogin()).isEqualTo("jane_doe");
        verify(userRepository, times(1)).findAllActiveClients();
    }

    @Test
    void getAllActiveClients_shouldReturnEmptyListWhenNoUsers() {
        when(userRepository.findAllActiveClients()).thenReturn(List.of());

        List<UserResponse> result = adminService.getAllActiveClients();

        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findAllActiveClients();
    }

    @Test
    void disableUser_shouldDisableUserWhenExists() {
        Long userId = 1L;
        User user = createUser(userId, "john_doe", "john@example.com", true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminService.disableUser(userId);

        assertThat(user.isEnabled()).isFalse();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void disableUser_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.disableUser(userId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Пользователь не найден");
        verify(userRepository, never()).save(any());
    }

    @Test
    void enableUser_shouldEnableUserWhenExists() {
        Long userId = 1L;
        User user = createUser(userId, "john_doe", "john@example.com", false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        adminService.enableUser(userId);

        assertThat(user.isEnabled()).isTrue();
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void enableUser_shouldThrowExceptionWhenUserNotFound() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.enableUser(userId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Пользователь не найден");
        verify(userRepository, never()).save(any());
    }

    private User createUser(Long id, String login, String email, boolean enabled) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        user.setEmail(email);
        user.setEnabled(enabled);
        user.setRole(Role.ROLE_USER);

        Client client = new Client();
        client.setFullName("Test User");
        client.setPhone("+79991234567");
        user.setClient(client);

        return user;
    }
}