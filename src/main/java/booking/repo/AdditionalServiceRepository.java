package booking.repo;

import booking.entity.AdditionalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdditionalServiceRepository extends JpaRepository<AdditionalService, String> {
    Optional<AdditionalService> findByName(String name);
}
