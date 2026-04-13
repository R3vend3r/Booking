package booking.repo;

import booking.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, String> {
    @Query("SELECT c FROM Client c JOIN FETCH c.user")
    List<Client> findAllWithUser();

    Optional<Client> findByPhone(String phone);

    @Query("SELECT c FROM Client c WHERE c.user.id = :userId")
    Optional<Client> findByUserId(@Param("userId") Long userId);
}
