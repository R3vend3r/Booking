package booking.service;

import booking.dto.mapper.ContractMapper;
import booking.dto.request.ContractRequest;
import booking.dto.response.ContractResponse;
import booking.entity.Booking;
import booking.entity.BookingService;
import booking.entity.Contract;
import booking.enums.PaymentMethod;
import booking.enums.PaymentStatus;
import booking.exception.ServiceException;
import booking.repo.BookingRepository;
import booking.repo.ContractRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final BookingRepository bookingRepository;
    private final ContractMapper contractMapper;

    public ContractService(ContractRepository contractRepository,
                           BookingRepository bookingRepository,
                           ContractMapper contractMapper) {
        this.contractRepository = contractRepository;
        this.bookingRepository = bookingRepository;
        this.contractMapper = contractMapper;
    }

    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        Booking booking = contractRepository.findByIdWithContractAndServices(request.getBookingId())
                .orElseThrow(() -> new ServiceException("Бронирование с ID " + request.getBookingId() + " не найдено", HttpStatus.NOT_FOUND));

        if (booking.getContract() != null) {
            throw new ServiceException("Договор для этого бронирования уже существует");
        }

        if (booking.getStartTime().isBefore(java.time.LocalDateTime.now())) {
            throw new ServiceException("Нельзя создать договор для прошедшего бронирования");
        }

        Long totalAmount = booking.getTotalAmount();

        if (totalAmount == 0) {
            throw new ServiceException("Нельзя создать договор с нулевой суммой");
        }

        Contract contract = new Contract();
        contract.setBooking(booking);
        contract.setTotalAmount(totalAmount);

        Contract saved = contractRepository.save(contract);

        booking.setContract(saved);
        bookingRepository.save(booking);

        return contractMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractById(String id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new ServiceException("Договор с ID " + id + " не найден", HttpStatus.NOT_FOUND));
        return contractMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractByBookingId(String bookingId) {
        Contract contract = contractRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ServiceException("Договор для бронирования " + bookingId + " не найден", HttpStatus.NOT_FOUND));
        return contractMapper.toResponse(contract);
    }

    @Transactional(readOnly = true)
    public ContractResponse getContractWithDetails(String id) {
        Booking booking = contractRepository.findByIdWithContractAndServices(id)
                .orElseThrow(() -> new ServiceException("Договор с ID " + id + " не найден", HttpStatus.NOT_FOUND));

        Contract contract = booking.getContract();
        if (contract == null) {
            throw new ServiceException("Договор не найден", HttpStatus.NOT_FOUND);
        }

        ContractResponse response = contractMapper.toResponse(contract);

        long hours = java.time.Duration.between(booking.getStartTime(), booking.getEndTime()).toHours();
        if (hours < 1) hours = 1;

        Long servicesTotal = booking.getBookingServices().stream()
                .mapToLong(BookingService::getTotalPrice)
                .sum();

        return response;
    }

    @Transactional
    public List<ContractResponse> getAllContracts() {
        cleanupExpiredContracts();
        return contractRepository.findAll().stream()
                .map(contractMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ContractResponse> getAllContractsWithBookings() {
        cleanupExpiredContracts();
        return contractRepository.findAllWithContracts().stream()
                .map(booking -> contractMapper.toResponse(booking.getContract()))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ContractResponse> getPendingContracts() {
        cleanupExpiredContracts();
        return contractRepository.findByPaymentStatus(PaymentStatus.PENDING).stream()
                .map(contractMapper::toResponse)
                .collect(Collectors.toList());
    }

    private void cleanupExpiredContracts() {
        List<Contract> expired = contractRepository.findExpiredPendingContracts(java.time.LocalDateTime.now());
        if (!expired.isEmpty()) {
            contractRepository.deleteAll(expired);
        }
    }

    @Transactional(readOnly = true)
    public List<Booking> getBookingsWithoutContract() {
        return contractRepository.findBookingsWithoutContract();
    }

    @Transactional
    public ContractResponse payContract(String contractId, PaymentMethod method) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ServiceException("Договор с ID " + contractId + " не найден", HttpStatus.NOT_FOUND));

        if (contract.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ServiceException("Договор уже оплачен");
        }

        if (contract.getPaymentStatus() == PaymentStatus.CANCELLED) {
            throw new ServiceException("Нельзя оплатить отмененный договор");
        }

        contract.markAsPaid(method);
        Booking booking = contract.getBooking();
        Contract saved = contractRepository.save(contract);
        return contractMapper.toResponse(saved);
    }

    @Transactional
    public ContractResponse cancelContract(String contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new ServiceException("Договор с ID " + contractId + " не найден", HttpStatus.NOT_FOUND));

        if (contract.getPaymentStatus() == PaymentStatus.PAID) {
            throw new ServiceException("Нельзя отменить оплаченный договор");
        }

        contract.markAsCancelled();
        Contract saved = contractRepository.save(contract);
        return contractMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Long getTotalIncome() {
        Double total = contractRepository.calculateTotalIncome();
        return total != null ? total.longValue() : 0L;
    }
}