package booking.repo;

import booking.entity.Booking;
import booking.entity.Contract;
import booking.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract,String> {
    Optional<Contract> findByBookingId(String bookingId);
    Optional<Contract> findByContractNumber(String contractNumber);
    List<Contract> findByPaymentStatus(PaymentStatus status);

    @Query("SELECT SUM(c.totalAmount) FROM Contract c WHERE c.paymentStatus = 'PAID'")
    Double calculateTotalIncome();

    @Query("SELECT AVG(c.totalAmount) FROM Contract c WHERE c.paymentStatus = 'PAID'")
    Double calculateAverageCheck();

    @Query("SELECT COUNT(c) FROM Contract c WHERE c.paymentStatus = 'PAID'")
    Long countPaidContracts();

    @Query(value = "SELECT TO_CHAR(c.payment_date, 'YYYY-MM-DD') AS period, SUM(c.total_amount) AS sum " +
            "FROM contracts c WHERE c.payment_status = 'PAID' " +
            "GROUP BY TO_CHAR(c.payment_date, 'YYYY-MM-DD') ORDER BY period DESC", nativeQuery = true)
    List<Object[]> findRevenueByDayNative();

    @Query(value = "SELECT TO_CHAR(DATE_TRUNC('week', c.payment_date), 'YYYY-MM-DD') AS period, SUM(c.total_amount) AS sum " +
            "FROM contracts c WHERE c.payment_status = 'PAID' " +
            "GROUP BY DATE_TRUNC('week', c.payment_date) ORDER BY period DESC", nativeQuery = true)
    List<Object[]> findRevenueByWeekNative();

    @Query(value = "SELECT TO_CHAR(DATE_TRUNC('month', c.payment_date), 'YYYY-MM-DD') AS period, SUM(c.total_amount) AS sum " +
            "FROM contracts c WHERE c.payment_status = 'PAID' " +
            "GROUP BY DATE_TRUNC('month', c.payment_date) ORDER BY period DESC", nativeQuery = true)
    List<Object[]> findRevenueByMonthNative();

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.contract WHERE b.id = :id")
    Optional<Booking> findByIdWithContract(@Param("id") String id);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.contract LEFT JOIN FETCH b.bookingServices bs LEFT JOIN FETCH bs.service WHERE b.id = :id")
    Optional<Booking> findByIdWithContractAndServices(@Param("id") String id);

    @Query("SELECT b FROM Booking b JOIN FETCH b.contract")
    List<Booking> findAllWithContracts();

    @Query("SELECT b FROM Booking b WHERE b.contract IS NULL")
    List<Booking> findBookingsWithoutContract();

    @Query("SELECT c FROM Contract c WHERE c.paymentStatus = 'PENDING' AND c.booking.endTime < :now")
    List<Contract> findExpiredPendingContracts(@Param("now") LocalDateTime now);
}