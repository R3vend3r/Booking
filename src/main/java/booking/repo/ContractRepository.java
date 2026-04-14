package booking.repo;

import booking.entity.Booking;
import booking.entity.Contract;
import booking.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract,String> {
    Optional<Contract> findByBookingId(String bookingId);
    Optional<Contract> findByContractNumber(String contractNumber);
    List<Contract> findByPaymentStatus(PaymentStatus status);

    @Query("SELECT SUM(c.totalAmount) FROM Contract c WHERE c.paymentStatus = 'PAID'")
    Double calculateTotalIncome();
    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.contract WHERE b.id = :id")
    Optional<Booking> findByIdWithContract(@Param("id") String id);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.contract LEFT JOIN FETCH b.bookingServices bs LEFT JOIN FETCH bs.service WHERE b.id = :id")
    Optional<Booking> findByIdWithContractAndServices(@Param("id") String id);

    @Query("SELECT b FROM Booking b JOIN FETCH b.contract")
    List<Booking> findAllWithContracts();

    @Query("SELECT b FROM Booking b WHERE b.contract IS NULL")
    List<Booking> findBookingsWithoutContract();
}
