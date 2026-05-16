package booking.repo;

import booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String> {
    List<Booking> findByClientId(String clientId);
    List<Booking> findByWorkPlaceId(String workplaceId);
    List<Booking> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE :currentTime BETWEEN b.startTime AND b.endTime")
    List<Booking> findActiveBookings(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.bookingServices bs LEFT JOIN FETCH bs.service WHERE b.id = :id")
    Optional<Booking> findByIdWithServices(@Param("id") String id);

    @Query("SELECT b.endTime FROM Booking b WHERE b.workPlace.id = :workplaceId AND b.startTime <= :now AND b.endTime > :now")
    List<LocalDateTime> findCurrentBookingEndTime(@Param("workplaceId") String workplaceId, @Param("now") LocalDateTime now);
    // нужно ли с договорами делать? Добавлю уже в процессе

    @Query("SELECT b FROM Booking b JOIN FETCH b.client")
    List<Booking> findAllWithClient();
}
