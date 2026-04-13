package booking.repo;

import booking.entity.BookingService;
import booking.entity.BookingServiceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingServiceRepository extends JpaRepository<BookingService, BookingServiceId> {
    List<BookingService> findByBookingId(String bookingId);
    List<BookingService> findByServiceId(String serviceId);

    @Query("SELECT bs FROM BookingService bs JOIN FETCH bs.service WHERE bs.booking.id = :bookingId")
    List<BookingService> findByBookingIdWithService(@Param("bookingId") String bookingId);

    @Query("SELECT SUM(bs.quantity) FROM BookingService bs WHERE bs.service.id = :serviceId")
    Integer getTotalQuantityForService(@Param("serviceId") String serviceId);
}
