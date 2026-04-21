package booking.service;

import booking.dto.mapper.ClientMapper;
import booking.dto.request.ClientRequest;
import booking.dto.response.ClientResponse;
import booking.entity.Client;
import booking.entity.Booking;
import booking.exception.ServiceException;
import booking.repo.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    @Test
    void createClient_shouldSaveAndReturnResponseWhenSuccess() {
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Иванов");
        request.setPhone("+79991234567");
        request.setBirthday(LocalDate.of(1990, 1, 1));

        Client client = new Client();
        client.setFullName("Иван Иванов");

        Client savedClient = new Client();
        savedClient.setId("CL-123");
        savedClient.setFullName("Иван Иванов");

        ClientResponse response = new ClientResponse("CL-123", "Иван Иванов", "+79991234567", LocalDate.of(1990, 1, 1));

        when(clientRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(clientMapper.toEntity(request)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(savedClient);
        when(clientMapper.toResponse(savedClient)).thenReturn(response);

        ClientResponse result = clientService.createClient(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("CL-123");
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void createClient_shouldThrowExceptionWhenPhoneAlreadyExists() {
        ClientRequest request = new ClientRequest();
        request.setPhone("+79991234567");

        Client existingClient = new Client();
        existingClient.setPhone("+79991234567");

        when(clientRepository.findByPhone(request.getPhone())).thenReturn(Optional.of(existingClient));

        assertThatThrownBy(() -> clientService.createClient(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Клиент с телефоном +79991234567 уже существует");
    }

    @Test
    void getClientById_shouldReturnResponseWhenFound() {
        String id = "CL-123";
        Client client = new Client();
        client.setId(id);
        client.setFullName("Иван Иванов");

        ClientResponse response = new ClientResponse(id, "Иван Иванов", "+79991234567", LocalDate.of(1990, 1, 1));

        when(clientRepository.findById(id)).thenReturn(Optional.of(client));
        when(clientMapper.toResponse(client)).thenReturn(response);

        ClientResponse result = clientService.getClientById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void getClientById_shouldThrowExceptionWhenNotFound() {
        String id = "CL-999";
        when(clientRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientById(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Клиент с ID " + id + " не найден");
    }

    @Test
    void getClientByPhone_shouldReturnResponseWhenFound() {
        String phone = "+79991234567";
        Client client = new Client();
        client.setPhone(phone);
        ClientResponse response = new ClientResponse("CL-123", "Иван Иванов", phone, LocalDate.of(1990, 1, 1));

        when(clientRepository.findByPhone(phone)).thenReturn(Optional.of(client));
        when(clientMapper.toResponse(client)).thenReturn(response);

        ClientResponse result = clientService.getClientByPhone(phone);

        assertThat(result).isNotNull();
        assertThat(result.getPhone()).isEqualTo(phone);
    }

    @Test
    void getClientByPhone_shouldThrowExceptionWhenNotFound() {
        String phone = "+79999999999";
        when(clientRepository.findByPhone(phone)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientByPhone(phone))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Клиент с телефоном " + phone + " не найден");
    }

    @Test
    void getAllClients_shouldReturnListOfResponses() {
        Client client1 = new Client();
        Client client2 = new Client();
        List<Client> clients = Arrays.asList(client1, client2);

        ClientResponse response1 = new ClientResponse();
        ClientResponse response2 = new ClientResponse();
        List<ClientResponse> expectedResponses = Arrays.asList(response1, response2);

        when(clientRepository.findAll()).thenReturn(clients);
        when(clientMapper.toResponse(client1)).thenReturn(response1);
        when(clientMapper.toResponse(client2)).thenReturn(response2);

        List<ClientResponse> result = clientService.getAllClients();

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedResponses);
    }

    @Test
    void updateClient_shouldUpdateSuccessfully() {
        String id = "CL-123";
        ClientRequest request = new ClientRequest();
        request.setFullName("Иван Петров");
        request.setPhone("+79991112233");

        Client existingClient = new Client();
        existingClient.setId(id);
        existingClient.setFullName("Иван Иванов");
        existingClient.setPhone("+79991234567");

        Client updatedClient = new Client();
        updatedClient.setId(id);
        updatedClient.setFullName("Иван Петров");

        ClientResponse response = new ClientResponse(id, "Иван Петров", "+79991112233", null);

        when(clientRepository.findById(id)).thenReturn(Optional.of(existingClient));
        when(clientRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(clientRepository.save(existingClient)).thenReturn(updatedClient);
        when(clientMapper.toResponse(updatedClient)).thenReturn(response);

        ClientResponse result = clientService.updateClient(id, request);

        assertThat(result.getFullName()).isEqualTo("Иван Петров");
    }

    @Test
    void updateClient_shouldThrowExceptionWhenPhoneAlreadyExists() {
        String id = "CL-123";
        ClientRequest request = new ClientRequest();
        request.setPhone("+79991112233");

        Client existingClient = new Client();
        existingClient.setId(id);
        existingClient.setPhone("+79991234567");

        Client otherClient = new Client();
        otherClient.setId("CL-456");
        otherClient.setPhone("+79991112233");

        when(clientRepository.findById(id)).thenReturn(Optional.of(existingClient));
        when(clientRepository.findByPhone(request.getPhone())).thenReturn(Optional.of(otherClient));

        assertThatThrownBy(() -> clientService.updateClient(id, request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Клиент с телефоном +79991112233 уже существует");
    }

    @Test
    void deleteClient_shouldDeleteSuccessfully() {
        String id = "CL-123";
        Client client = new Client();
        client.setId(id);
        client.setBookings(List.of());

        when(clientRepository.findById(id)).thenReturn(Optional.of(client));

        clientService.deleteClient(id);

        verify(clientRepository, times(1)).delete(client);
    }

    @Test
    void deleteClient_shouldThrowExceptionWhenHasBookings() {
        String id = "CL-123";
        Client client = new Client();
        client.setId(id);
        Booking booking = new Booking();
        client.setBookings(List.of(booking));

        when(clientRepository.findById(id)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> clientService.deleteClient(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Нельзя удалить клиента, у которого есть бронирования");
    }
}