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

    @Query("SELECT b FROM Booking b JOIN FETCH b.client JOIN FETCH b.workPlace wp JOIN FETCH wp.location WHERE b.client.id = :clientId")
    List<Booking> findByClientIdWithDetails(@Param("clientId") String clientId);
    @Query("SELECT b FROM Booking b JOIN FETCH b.client JOIN FETCH b.workPlace wp JOIN FETCH wp.location WHERE b.startTime BETWEEN :start AND :end")
    List<Booking> findByStartTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b JOIN FETCH b.client JOIN FETCH b.workPlace wp JOIN FETCH wp.location WHERE :currentTime BETWEEN b.startTime AND b.endTime")
    List<Booking> findActiveBookings(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT b FROM Booking b JOIN FETCH b.client JOIN FETCH b.workPlace wp JOIN FETCH wp.location WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") String id);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.bookingServices bs LEFT JOIN FETCH bs.service WHERE b.id = :id")
    Optional<Booking> findByIdWithServices(@Param("id") String id);

    @Query("SELECT b FROM Booking b JOIN FETCH b.client JOIN FETCH b.workPlace wp JOIN FETCH wp.location")
    List<Booking> findAllWithDetails();

    @Query("SELECT b.endTime FROM Booking b WHERE b.workPlace.id = :workplaceId AND b.startTime <= :now AND b.endTime > :now")
    List<LocalDateTime> findCurrentBookingEndTime(@Param("workplaceId") String workplaceId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) FROM Booking b JOIN b.client c WHERE c.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
