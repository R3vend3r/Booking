package booking.service;

import booking.dto.mapper.ClientMapper;
import booking.dto.request.ClientRequest;
import booking.dto.response.ClientResponse;
import booking.entity.Client;
import booking.exception.ServiceException;
import booking.repo.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    @Transactional
    public ClientResponse createClient(ClientRequest request) {
        if (clientRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new ServiceException("Клиент с телефоном " + request.getPhone() + " уже существует");
        }

        Client client = clientMapper.toEntity(request);
        Client saved = clientRepository.save(client);
        return clientMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientById(String id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Клиент с ID " + id + " не найден"));
        return clientMapper.toResponse(client);
    }

    @Transactional(readOnly = true)
    public ClientResponse getClientByPhone(String phone) {
        Client client = clientRepository.findByPhone(phone)
                .orElseThrow(() -> new ServiceException("Клиент с телефоном " + phone + " не найден"));
        return clientMapper.toResponse(client);
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClientResponse updateClient(String id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Клиент с ID " + id + " не найден"));

        if (!client.getPhone().equals(request.getPhone())) {
            if (clientRepository.findByPhone(request.getPhone()).isPresent()) {
                throw new ServiceException("Клиент с телефоном " + request.getPhone() + " уже существует");
            }
        }

        client.setFullName(request.getFullName());
        client.setPhone(request.getPhone());
        client.setBirthday(request.getBirthday());

        Client updated = clientRepository.save(client);
        return clientMapper.toResponse(updated);
    }

    @Transactional
    public void deleteClient(String id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Клиент с ID " + id + " не найден"));

        if (client.getBookings() != null && !client.getBookings().isEmpty()) {
            throw new ServiceException("Нельзя удалить клиента, у которого есть бронирования");
        }

        clientRepository.delete(client);
    }
}