package booking.repo;

import booking.entity.Contract;
import booking.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract,String> {
    Optional<Contract> findByBookingId(String bookingId);
    Optional<Contract> findByContractNumber(String contractNumber);
    List<Contract> findByPaymentStatus(PaymentStatus status);

    @Query("SELECT SUM(c.totalAmount) FROM Contract c WHERE c.paymentStatus = 'PAID'")
    Double calculateTotalIncome();}
