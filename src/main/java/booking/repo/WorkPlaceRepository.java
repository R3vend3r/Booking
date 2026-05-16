package booking.repo;

import booking.entity.WorkPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkPlaceRepository extends JpaRepository<WorkPlace, String> {
    List<WorkPlace> findByLocationId(String locationId);
    List<WorkPlace> findByLocationIdAndAvailable(String locationId, boolean available);

    @Query("SELECT wp FROM WorkPlace wp WHERE wp.location.id = :locationId AND wp.id NOT IN " +
            "(SELECT b.workPlace.id FROM Booking b WHERE b.workPlace.location.id = :locationId " +
            "AND b.startTime < :endTime AND b.endTime > :startTime AND b.endTime > :currentTime)")
    List<WorkPlace> findAvailableByLocationIdAndTimeRange(@Param("locationId") String locationId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime,
                                                          @Param("currentTime") LocalDateTime currentTime);

    @Query(value = "SELECT * FROM workplaces WHERE id IN (SELECT workplace_id FROM bookings ORDER BY created_at DESC LIMIT :limit)", nativeQuery = true)
    List<WorkPlace> findRecentlyBookedWorkplaces(@Param("limit") int limit);
}
