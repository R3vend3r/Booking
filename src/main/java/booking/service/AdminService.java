package booking.service;

import booking.dto.auth.UserResponse;
import booking.entity.User;
import booking.exception.ServiceException;
import booking.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllActiveClients() {
        return userRepository.findAllActiveClients().stream()
                .map(user -> new UserResponse(
                        user.getId().toString(),
                        user.getLogin(),
                        user.getEmail(),
                        user.getClient().getFullName(),
                        user.getClient().getPhone(),
                        user.isEnabled()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("Пользователь не найден"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException("Пользователь не найден"));
        user.setEnabled(true);
        userRepository.save(user);
    }
}