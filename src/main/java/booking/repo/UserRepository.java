package booking.repo;

import booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_USER' AND u.enabled = true")
    List<User> findAllActiveClients();
}