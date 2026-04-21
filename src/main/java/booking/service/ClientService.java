package booking.service;

import booking.dto.mapper.ClientMapper;
import booking.dto.request.ClientRequest;
import booking.dto.response.ClientResponse;
import booking.entity.Client;
import booking.entity.User;
import booking.exception.ServiceException;
import booking.repo.ClientRepository;
import booking.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;
    private final UserRepository userRepository;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper, UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public ClientResponse getCurrentClient() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ServiceException("Пользователь не найден"));

        Client client = user.getClient();
        if (client == null) {
            throw new ServiceException("Клиент не найден");
        }
        return clientMapper.toResponse(client);
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

        checkClientOwnership(client);

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

    private void checkClientOwnership(Client client) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String login = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            User user = userRepository.findByLogin(login)
                    .orElseThrow(() -> new ServiceException("Пользователь не найден"));

            if (user.getClient() != null && !user.getClient().getId().equals(client.getId())) {
                throw new ServiceException("Вы можете редактировать только свой профиль");
            }
        }
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