package booking.controller;

import booking.dto.auth.UserResponse;
import booking.exception.GlobalExceptionHandler;
import booking.exception.ServiceException;
import booking.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(adminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== GET ALL ACTIVE CLIENTS ====================

//    @Test
//    void getAllActiveClients_shouldReturnListOfUsers() throws Exception {
//        List<UserResponse> users = Arrays.asList(
//                new UserResponse("1", "john_doe", "john@example.com", "John Doe", "+79991234567", true),
//                new UserResponse("2", "jane_doe", "jane@example.com", "Jane Doe", "+79997654321", true)
//        );
//
//        when(adminService.getAllActiveClients()).thenReturn(users);
//
//        mockMvc.perform(get("/api/admin/users/active")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].id").value("1"))
//                .andExpect(jsonPath("$[0].login").value("john_doe"))
//                .andExpect(jsonPath("$[0].email").value("john@example.com"))
//                .andExpect(jsonPath("$[0].fullName").value("John Doe"))
//                .andExpect(jsonPath("$[0].phone").value("+79991234567"))
//                .andExpect(jsonPath("$[0].enabled").value(true))
//                .andExpect(jsonPath("$[1].id").value("2"))
//                .andExpect(jsonPath("$[1].login").value("jane_doe"));
//
//        verify(adminService, times(1)).getAllActiveClients();
//    }

    @Test
    void getAllActiveClients_shouldReturnEmptyListWhenNoUsers() throws Exception {
        when(adminService.getAllActiveClients()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users/active")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(adminService, times(1)).getAllActiveClients();
    }

    // ==================== DISABLE USER ====================

    @Test
    void disableUser_shouldDisableSuccessfully() throws Exception {
        Long userId = 1L;
        doNothing().when(adminService).disableUser(userId);

        mockMvc.perform(post("/api/admin/users/{userId}/disable", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminService, times(1)).disableUser(userId);
    }

    @Test
    void disableUser_shouldReturnNotFoundWhenUserMissing() throws Exception {
        Long userId = 999L;
        doThrow(new ServiceException("Пользователь не найден"))
                .when(adminService).disableUser(userId);

        mockMvc.perform(post("/api/admin/users/{userId}/disable", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(adminService, times(1)).disableUser(userId);
    }

    // ==================== ENABLE USER ====================

    @Test
    void enableUser_shouldEnableSuccessfully() throws Exception {
        Long userId = 1L;
        doNothing().when(adminService).enableUser(userId);

        mockMvc.perform(post("/api/admin/users/{userId}/enable", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(adminService, times(1)).enableUser(userId);
    }

    @Test
    void enableUser_shouldReturnNotFoundWhenUserMissing() throws Exception {
        Long userId = 999L;
        doThrow(new ServiceException("Пользователь не найден"))
                .when(adminService).enableUser(userId);

        mockMvc.perform(post("/api/admin/users/{userId}/enable", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(adminService, times(1)).enableUser(userId);
    }
}