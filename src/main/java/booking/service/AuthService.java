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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, ClientRepository clientRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clientRepository = clientRepository;
    }

    @Transactional
    public AuthResponse registrationUser(RegistrationRequest request){
        if (userRepository.findByLogin(request.getLogin()).isPresent()){
            throw new SecurityException("Пользователь с таким логином уже существует");
        }

        User user = new User();
        user.setLogin(request.getLogin());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        Client client = new Client(
                request.getFullName(),
                request.getPhone(),
                request.getBirthday()
        );

        client.setUser(savedUser);
        clientRepository.save(client);

        return new AuthResponse(
                savedUser.getId().toString(),
                savedUser.getLogin(),
                savedUser.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByLogin(request.getLogin()).orElseThrow(() -> new ServiceException("неверный логин или пароль"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new SecurityException("Неверный логин или пароль");
        }
        if (!user.isEnabled()) {
            throw new ServiceException("Аккаунт заблокирован");
        }
        return new AuthResponse(
                user.getId().toString(),
                user.getLogin(),
                user.getRole().name()
        );
    }
}
