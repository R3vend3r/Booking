package booking.repo;

import booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_USER' AND u.enabled = true")
    List<User> findAllActiveClients();

    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_USER' AND u.enabled = true ORDER BY u.id")
    List<User> findAllActiveClientsPaginated(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_USER' AND u.enabled = true " +
            "AND (LOWER(u.login) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))) ORDER BY u.id")
    List<User> searchActiveClientsPaginated(@Param("q") String q, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ROLE_USER' AND u.enabled = true " +
            "AND (LOWER(u.login) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')))")
    long countSearchActiveClients(@Param("q") String q);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ROLE_USER' AND u.enabled = true")
    long countAllActiveClients();

    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_MANAGER' ORDER BY u.id")
    List<User> findAllManagers();
}