package booking.service;

import booking.dto.auth.CreateManagerRequest;
import booking.dto.auth.ManagerResponse;
import booking.dto.auth.UpdateManagerRequest;
import booking.dto.auth.UserResponse;
import booking.dto.response.ClientResponse;
import booking.dto.response.RevenueEntry;
import booking.dto.response.StatsSummary;
import booking.dto.response.TopServiceEntry;
import booking.entity.User;
import booking.enums.Role;
import booking.exception.ServiceException;
import booking.repo.BookingRepository;
import booking.repo.BookingServiceRepository;
import booking.repo.ClientRepository;
import booking.repo.ContractRepository;
import booking.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final BookingRepository bookingRepository;
    private final ContractRepository contractRepository;
    private final BookingServiceRepository bookingServiceRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, ClientRepository clientRepository,
                        BookingRepository bookingRepository, ContractRepository contractRepository,
                        BookingServiceRepository bookingServiceRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.bookingRepository = bookingRepository;
        this.contractRepository = contractRepository;
        this.bookingServiceRepository = bookingServiceRepository;
        this.passwordEncoder = passwordEncoder;
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
                        user.isEnabled(),
                        bookingRepository.countByUserId(user.getId())
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getActiveClientsPaginated(int limit, int offset) {
        List<User> userList = userRepository.findAllActiveClientsPaginated(PageRequest.of(offset / limit, limit));
        return buildUserPageResponse(userList, userRepository.countAllActiveClients());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> searchActiveClientsPaginated(String q, int limit, int offset) {
        List<User> userList = userRepository.searchActiveClientsPaginated(q, PageRequest.of(offset / limit, limit));
        return buildUserPageResponse(userList, userRepository.countSearchActiveClients(q));
    }

    private Map<String, Object> buildUserPageResponse(List<User> userList, long total) {
        List<UserResponse> users = userList.stream()
                .map(user -> new UserResponse(
                        user.getId().toString(),
                        user.getLogin(),
                        user.getEmail(),
                        user.getClient().getFullName(),
                        user.getClient().getPhone(),
                        user.isEnabled(),
                        bookingRepository.countByUserId(user.getId())
                ))
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("total", total);
        return result;
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getClientsWithBookings(int limit, int offset) {
        return clientRepository.findClientsWithBookingsPaginated(limit, offset).stream()
                .map(c -> new ClientResponse(c.getId(), c.getFullName(), c.getPhone(), c.getBirthday()))
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

    @Transactional(readOnly = true)
    public List<RevenueEntry> getRevenueByDay() {
        return contractRepository.findRevenueByDayNative().stream()
                .map(row -> new RevenueEntry((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RevenueEntry> getRevenueByWeek() {
        return contractRepository.findRevenueByWeekNative().stream()
                .map(row -> new RevenueEntry((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RevenueEntry> getRevenueByMonth() {
        return contractRepository.findRevenueByMonthNative().stream()
                .map(row -> new RevenueEntry((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TopServiceEntry> getTopServices() {
        return bookingServiceRepository.findTopServicesNative().stream()
                .map(row -> new TopServiceEntry((String) row[0], ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StatsSummary getStatsSummary() {
        Double totalRevenue = contractRepository.calculateTotalIncome();
        Double avgCheck = contractRepository.calculateAverageCheck();
        Long paidCount = contractRepository.countPaidContracts();
        return new StatsSummary(
                avgCheck != null ? avgCheck : 0.0,
                totalRevenue != null ? totalRevenue : 0.0,
                paidCount != null ? paidCount : 0L
        );
    }

    @Transactional(readOnly = true)
    public List<ManagerResponse> getManagers() {
        return userRepository.findAllManagers().stream()
                .map(u -> new ManagerResponse(u.getId(), u.getLogin(), u.getEmail(), u.isEnabled(), u.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional
    public ManagerResponse createManager(CreateManagerRequest request) {
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new ServiceException("Менеджер с таким логином уже существует");
        }
        if (request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ServiceException("Менеджер с таким email уже существует");
        }
        User user = new User();
        user.setLogin(request.getLogin());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_MANAGER);
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        return new ManagerResponse(user.getId(), user.getLogin(), user.getEmail(), user.isEnabled(), user.getCreatedAt());
    }

    @Transactional
    public ManagerResponse updateManager(Long id, UpdateManagerRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Менеджер не найден"));
        if (request.getLogin() != null) {
            if (!user.getLogin().equals(request.getLogin()) && userRepository.findByLogin(request.getLogin()).isPresent()) {
                throw new ServiceException("Менеджер с таким логином уже существует");
            }
            user.setLogin(request.getLogin());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        user = userRepository.save(user);
        return new ManagerResponse(user.getId(), user.getLogin(), user.getEmail(), user.isEnabled(), user.getCreatedAt());
    }

    @Transactional
    public void deleteManager(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Менеджер не найден"));
        userRepository.delete(user);
    }
}