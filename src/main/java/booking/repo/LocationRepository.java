package booking.repo;

import booking.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, String> {
    Optional<Location> findByBranchName(String name);
    List<Location> findByCity(String city);
    Optional<Location> findByBranchNameAndAddressAndCity(String name, String address, String city);

    @Query("SELECT DISTINCT l FROM Location l LEFT JOIN FETCH l.workplaces WHERE l.city = :city")
    List<Location> findByCityWithWorkplaces(@Param("city") String city);

    @Query("SELECT l FROM Location l LEFT JOIN FETCH l.workplaces WHERE l.id = :id")
    Optional<Location> findByIdWithWorkplaces(@Param("id") String id);

    @Query(value = "SELECT * FROM locations WHERE id IN (SELECT DISTINCT location_id FROM workplaces WHERE available = true)", nativeQuery = true)
    List<Location> findLocationsWithAvailableWorkplaces();
}
