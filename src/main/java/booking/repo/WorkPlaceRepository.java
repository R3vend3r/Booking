package booking.repo;

import booking.entity.WorkPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkPlaceRepository extends JpaRepository<WorkPlace, String> {
    List<WorkPlace> findByLocationId(String locationId);
    List<WorkPlace> findByLocationIdAndAvailable(String locationId, boolean available);

}
