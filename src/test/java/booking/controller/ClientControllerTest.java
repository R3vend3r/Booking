package booking.controller;

import booking.dto.request.ClientRequest;
import booking.dto.response.ClientResponse;
import booking.exception.GlobalExceptionHandler;
import booking.exception.ServiceException;
import booking.service.ClientService;
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
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(clientController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
                .build();
    }

    // ==================== CREATE CLIENT ====================

    @Test
    void createClient_shouldReturnCreatedWhenSuccess() throws Exception {
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Иванов");
        request.setPhone("+79991234567");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        ClientResponse response = new ClientResponse("CL-123", "Иван Иванов", "+79991234567", LocalDate.of(1990, 1, 1));

        when(clientService.createClient(any(ClientRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("CL-123"))
                .andExpect(jsonPath("$.fullName").value("Иван Иванов"))
                .andExpect(jsonPath("$.phone").value("+79991234567"))
                .andExpect(jsonPath("$.birthday").exists());

        verify(clientService, times(1)).createClient(any(ClientRequest.class));
    }

    @Test
    void createClient_shouldReturnBadRequestWhenFullNameIsBlank() throws Exception {
        ClientRequest request = new ClientRequest();
        request.setFullName("");
        request.setPhone("+79991234567");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any(ClientRequest.class));
    }

    @Test
    void createClient_shouldReturnBadRequestWhenFullNameIsNull() throws Exception {
        ClientRequest request = new ClientRequest();
        request.setFullName(null);
        request.setPhone("+79991234567");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any(ClientRequest.class));
    }

    @Test
    void createClient_shouldReturnBadRequestWhenPhoneIsBlank() throws Exception {
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Иванов");
        request.setPhone("");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any(ClientRequest.class));
    }

    @Test
    void createClient_shouldReturnBadRequestWhenPhoneIsNull() throws Exception {
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Иванов");
        request.setPhone(null);
        request.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any(ClientRequest.class));
    }

    @Test
    void createClient_shouldReturnBadRequestWhenPhoneHasInvalidFormat() throws Exception {
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Иванов");
        request.setPhone("invalid-phone");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any(ClientRequest.class));
    }

    @Test
    void createClient_shouldReturnBadRequestWhenBirthdayIsFuture() throws Exception {
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Иванов");
        request.setPhone("+79991234567");
        request.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).createClient(any(ClientRequest.class));
    }

    @Test
    void createClient_shouldReturnBadRequestWhenPhoneAlreadyExists() throws Exception {
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Иванов");
        request.setPhone("+79991234567");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        when(clientService.createClient(any(ClientRequest.class)))
                .thenThrow(new ServiceException("Клиент с таким номером телефона уже существует"));

        mockMvc.perform(post("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientService, times(1)).createClient(any(ClientRequest.class));
    }

    // ==================== GET CLIENT BY ID ====================

    @Test
    void getClientById_shouldReturnClientWhenFound() throws Exception {
        String clientId = "CL-123";
        ClientResponse response = new ClientResponse(clientId, "Иван Иванов", "+79991234567", LocalDate.of(1990, 1, 1));

        when(clientService.getClientById(clientId)).thenReturn(response);

        mockMvc.perform(get("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CL-123"))
                .andExpect(jsonPath("$.fullName").value("Иван Иванов"))
                .andExpect(jsonPath("$.phone").value("+79991234567"));

        verify(clientService, times(1)).getClientById(clientId);
    }

    @Test
    void getClientById_shouldReturnNotFoundWhenMissing() throws Exception {
        String clientId = "CL-999";
        when(clientService.getClientById(clientId))
                .thenThrow(new ServiceException("Клиент с ID CL-999 не найден"));

        mockMvc.perform(get("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).getClientById(clientId);
    }

    // ==================== GET CLIENT BY PHONE ====================

    @Test
    void getClientByPhone_shouldReturnClientWhenFound() throws Exception {
        String phone = "+79991234567";
        ClientResponse response = new ClientResponse("CL-123", "Иван Иванов", phone, LocalDate.of(1990, 1, 1));

        when(clientService.getClientByPhone(phone)).thenReturn(response);

        mockMvc.perform(get("/api/clients/phone/{phone}", phone)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CL-123"))
                .andExpect(jsonPath("$.fullName").value("Иван Иванов"))
                .andExpect(jsonPath("$.phone").value(phone));

        verify(clientService, times(1)).getClientByPhone(phone);
    }

    @Test
    void getClientByPhone_shouldReturnNotFoundWhenMissing() throws Exception {
        String phone = "+79999999999";
        when(clientService.getClientByPhone(phone))
                .thenThrow(new ServiceException("Клиент с номером телефона " + phone + " не найден"));

        mockMvc.perform(get("/api/clients/phone/{phone}", phone)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).getClientByPhone(phone);
    }

    // ==================== GET ALL CLIENTS ====================

    @Test
    void getAllClients_shouldReturnListOfClients() throws Exception {
        List<ClientResponse> clients = Arrays.asList(
                new ClientResponse("CL-123", "Иван Иванов", "+79991234567", LocalDate.of(1990, 1, 1)),
                new ClientResponse("CL-456", "Петр Петров", "+79997654321", LocalDate.of(1985, 5, 15))
        );

        when(clientService.getAllClients()).thenReturn(clients);

        mockMvc.perform(get("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("CL-123"))
                .andExpect(jsonPath("$[0].fullName").value("Иван Иванов"))
                .andExpect(jsonPath("$[1].id").value("CL-456"))
                .andExpect(jsonPath("$[1].fullName").value("Петр Петров"));

        verify(clientService, times(1)).getAllClients();
    }

    @Test
    void getAllClients_shouldReturnEmptyListWhenNoClients() throws Exception {
        when(clientService.getAllClients()).thenReturn(List.of());

        mockMvc.perform(get("/api/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(clientService, times(1)).getAllClients();
    }

    // ==================== UPDATE CLIENT ====================

    @Test
    void updateClient_shouldUpdateSuccessfully() throws Exception {
        String clientId = "CL-123";
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Петров");
        request.setPhone("+79991112233");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        ClientResponse response = new ClientResponse(clientId, "Иван Петров", "+79991112233", LocalDate.of(1990, 1, 1));

        when(clientService.updateClient(eq(clientId), any(ClientRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CL-123"))
                .andExpect(jsonPath("$.fullName").value("Иван Петров"))
                .andExpect(jsonPath("$.phone").value("+79991112233"));

        verify(clientService, times(1)).updateClient(eq(clientId), any(ClientRequest.class));
    }

    @Test
    void updateClient_shouldReturnNotFoundWhenClientMissing() throws Exception {
        String clientId = "CL-999";
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Иванов");
        request.setPhone("+79991234567");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        when(clientService.updateClient(eq(clientId), any(ClientRequest.class)))
                .thenThrow(new ServiceException("Клиент с ID CL-999 не найден"));

        mockMvc.perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).updateClient(eq(clientId), any(ClientRequest.class));
    }

    @Test
    void updateClient_shouldReturnBadRequestWhenPhoneAlreadyExists() throws Exception {
        String clientId = "CL-123";
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Иванов");
        request.setPhone("+79999999999");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        when(clientService.updateClient(eq(clientId), any(ClientRequest.class)))
                .thenThrow(new ServiceException("Клиент с таким номером телефона уже существует"));

        mockMvc.perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientService, times(1)).updateClient(eq(clientId), any(ClientRequest.class));
    }

    @Test
    void updateClient_shouldReturnBadRequestWhenFullNameIsBlank() throws Exception {
        String clientId = "CL-123";
        ClientRequest request = new ClientRequest();
        request.setFullName("");
        request.setPhone("+79991234567");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).updateClient(anyString(), any(ClientRequest.class));
    }

    @Test
    void updateClient_shouldReturnBadRequestWhenPhoneHasInvalidFormat() throws Exception {
        String clientId = "CL-123";
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Иванов");
        request.setPhone("invalid");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(put("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(clientService, never()).updateClient(anyString(), any(ClientRequest.class));
    }

    // ==================== DELETE CLIENT ====================

    @Test
    void deleteClient_shouldDeleteSuccessfully() throws Exception {
        String clientId = "CL-123";
        doNothing().when(clientService).deleteClient(clientId);

        mockMvc.perform(delete("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(clientService, times(1)).deleteClient(clientId);
    }

    @Test
    void deleteClient_shouldReturnNotFoundWhenClientMissing() throws Exception {
        String clientId = "CL-999";
        doThrow(new ServiceException("Клиент с ID CL-999 не найден"))
                .when(clientService).deleteClient(clientId);

        mockMvc.perform(delete("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(clientService, times(1)).deleteClient(clientId);
    }

    @Test
    void deleteClient_shouldReturnBadRequestWhenClientHasBookings() throws Exception {
        String clientId = "CL-123";
        doThrow(new ServiceException("Нельзя удалить клиента с активными бронированиями"))
                .when(clientService).deleteClient(clientId);

        mockMvc.perform(delete("/api/clients/{id}", clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(clientService, times(1)).deleteClient(clientId);
    }
}