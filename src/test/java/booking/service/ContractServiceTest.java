package booking.service;

import booking.dto.mapper.ContractMapper;
import booking.dto.request.ContractRequest;
import booking.dto.response.ContractResponse;
import booking.entity.Booking;
import booking.entity.Contract;
import booking.enums.PaymentMethod;
import booking.enums.PaymentStatus;
import booking.exception.ServiceException;
import booking.repo.BookingRepository;
import booking.repo.ContractRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ContractMapper contractMapper;

    @InjectMocks
    private ContractService contractService;

    @Test
    void createContract_shouldCreateSuccessfully() {
        ContractRequest request = new ContractRequest();
        request.setBookingId("B-123");

        Booking booking = mock(Booking.class);
        when(booking.getContract()).thenReturn(null);
        when(booking.getStartTime()).thenReturn(LocalDateTime.now().plusDays(1));
        when(booking.getTotalAmount()).thenReturn(10000L);

        Contract savedContract = new Contract();
        savedContract.setId("CT-123");
        savedContract.setContractNumber("CONTRACT-123");

        ContractResponse response = new ContractResponse();
        response.setId("CT-123");

        when(contractRepository.findByIdWithContractAndServices("B-123")).thenReturn(Optional.of(booking));
        when(contractRepository.save(any(Contract.class))).thenReturn(savedContract);
        when(contractMapper.toResponse(savedContract)).thenReturn(response);

        ContractResponse result = contractService.createContract(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("CT-123");
        verify(contractRepository, times(1)).save(any(Contract.class));
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void createContract_shouldThrowExceptionWhenBookingNotFound() {
        ContractRequest request = new ContractRequest();
        request.setBookingId("B-999");

        when(contractRepository.findByIdWithContractAndServices("B-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.createContract(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Бронирование с ID B-999 не найдено");
    }

    @Test
    void createContract_shouldThrowExceptionWhenContractAlreadyExists() {
        ContractRequest request = new ContractRequest();
        request.setBookingId("B-123");

        Booking booking = new Booking();
        booking.setId("B-123");
        booking.setContract(new Contract());

        when(contractRepository.findByIdWithContractAndServices("B-123")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> contractService.createContract(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Договор для этого бронирования уже существует");
    }

    @Test
    void createContract_shouldThrowExceptionWhenBookingInPast() {
        ContractRequest request = new ContractRequest();
        request.setBookingId("B-123");

        Booking booking = new Booking();
        booking.setId("B-123");
        booking.setStartTime(LocalDateTime.now().minusDays(1));
        booking.setContract(null);

        when(contractRepository.findByIdWithContractAndServices("B-123")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> contractService.createContract(request))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Нельзя создать договор для прошедшего бронирования");
    }

    @Test
    void getContractById_shouldReturnResponseWhenFound() {
        String id = "CT-123";
        Contract contract = new Contract();
        contract.setId(id);
        ContractResponse response = new ContractResponse();
        response.setId(id);

        when(contractRepository.findById(id)).thenReturn(Optional.of(contract));
        when(contractMapper.toResponse(contract)).thenReturn(response);

        ContractResponse result = contractService.getContractById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void getContractById_shouldThrowExceptionWhenNotFound() {
        String id = "CT-999";
        when(contractRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.getContractById(id))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Договор с ID " + id + " не найден");
    }

    @Test
    void getContractByBookingId_shouldReturnResponseWhenFound() {
        String bookingId = "B-123";
        Contract contract = new Contract();
        contract.setId("CT-123");
        ContractResponse response = new ContractResponse();

        when(contractRepository.findByBookingId(bookingId)).thenReturn(Optional.of(contract));
        when(contractMapper.toResponse(contract)).thenReturn(response);

        ContractResponse result = contractService.getContractByBookingId(bookingId);

        assertThat(result).isNotNull();
    }

    @Test
    void getContractByBookingId_shouldThrowExceptionWhenNotFound() {
        String bookingId = "B-999";
        when(contractRepository.findByBookingId(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.getContractByBookingId(bookingId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Договор для бронирования " + bookingId + " не найден");
    }

    @Test
    void getAllContracts_shouldReturnListOfResponses() {
        List<Contract> contracts = Arrays.asList(new Contract(), new Contract());
        List<ContractResponse> responses = Arrays.asList(new ContractResponse(), new ContractResponse());

        when(contractRepository.findAll()).thenReturn(contracts);
        when(contractMapper.toResponse(contracts.get(0))).thenReturn(responses.get(0));
        when(contractMapper.toResponse(contracts.get(1))).thenReturn(responses.get(1));

        List<ContractResponse> result = contractService.getAllContracts();

        assertThat(result).hasSize(2);
    }

    @Test
    void getPendingContracts_shouldReturnListOfPendingContracts() {
        List<Contract> contracts = Arrays.asList(new Contract(), new Contract());
        List<ContractResponse> responses = Arrays.asList(new ContractResponse(), new ContractResponse());

        when(contractRepository.findByPaymentStatus(PaymentStatus.PENDING)).thenReturn(contracts);
        when(contractMapper.toResponse(contracts.get(0))).thenReturn(responses.get(0));
        when(contractMapper.toResponse(contracts.get(1))).thenReturn(responses.get(1));

        List<ContractResponse> result = contractService.getPendingContracts();

        assertThat(result).hasSize(2);
    }

    @Test
    void payContract_shouldPaySuccessfully() {
        String contractId = "CT-123";
        Contract contract = new Contract();
        contract.setId(contractId);
        contract.setPaymentStatus(PaymentStatus.PENDING);

        Contract paidContract = new Contract();
        paidContract.setId(contractId);
        paidContract.setPaymentStatus(PaymentStatus.PAID);

        ContractResponse response = new ContractResponse();
        response.setPaymentStatus(PaymentStatus.PAID);

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(contractRepository.save(contract)).thenReturn(paidContract);
        when(contractMapper.toResponse(paidContract)).thenReturn(response);

        ContractResponse result = contractService.payContract(contractId, PaymentMethod.CARD);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        verify(contractRepository, times(1)).save(contract);
    }

    @Test
    void payContract_shouldThrowExceptionWhenContractNotFound() {
        String contractId = "CT-999";
        when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.payContract(contractId, PaymentMethod.CARD))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Договор с ID " + contractId + " не найден");
    }

    @Test
    void payContract_shouldThrowExceptionWhenAlreadyPaid() {
        String contractId = "CT-123";
        Contract contract = new Contract();
        contract.setPaymentStatus(PaymentStatus.PAID);

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractService.payContract(contractId, PaymentMethod.CARD))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Договор уже оплачен");
    }

    @Test
    void payContract_shouldThrowExceptionWhenCancelled() {
        String contractId = "CT-123";
        Contract contract = new Contract();
        contract.setPaymentStatus(PaymentStatus.CANCELLED);

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractService.payContract(contractId, PaymentMethod.CARD))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Нельзя оплатить отмененный договор");
    }

    @Test
    void cancelContract_shouldCancelSuccessfully() {
        String contractId = "CT-123";
        Contract contract = new Contract();
        contract.setId(contractId);
        contract.setPaymentStatus(PaymentStatus.PENDING);

        Contract cancelledContract = new Contract();
        cancelledContract.setId(contractId);
        cancelledContract.setPaymentStatus(PaymentStatus.CANCELLED);

        ContractResponse response = new ContractResponse();
        response.setPaymentStatus(PaymentStatus.CANCELLED);

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));
        when(contractRepository.save(contract)).thenReturn(cancelledContract);
        when(contractMapper.toResponse(cancelledContract)).thenReturn(response);

        ContractResponse result = contractService.cancelContract(contractId);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.CANCELLED);
    }

    @Test
    void cancelContract_shouldThrowExceptionWhenContractNotFound() {
        String contractId = "CT-999";
        when(contractRepository.findById(contractId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contractService.cancelContract(contractId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Договор с ID " + contractId + " не найден");
    }

    @Test
    void cancelContract_shouldThrowExceptionWhenAlreadyPaid() {
        String contractId = "CT-123";
        Contract contract = new Contract();
        contract.setPaymentStatus(PaymentStatus.PAID);

        when(contractRepository.findById(contractId)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractService.cancelContract(contractId))
                .isInstanceOf(ServiceException.class)
                .hasMessage("Нельзя отменить оплаченный договор");
    }

    @Test
    void getTotalIncome_shouldReturnTotalIncome() {
        Double expectedTotal = 150000.0;
        when(contractRepository.calculateTotalIncome()).thenReturn(expectedTotal);

        Long result = contractService.getTotalIncome();

        assertThat(result).isEqualTo(150000L);
    }

    @Test
    void getTotalIncome_shouldReturnZeroWhenNull() {
        when(contractRepository.calculateTotalIncome()).thenReturn(null);

        Long result = contractService.getTotalIncome();

        assertThat(result).isEqualTo(0L);
    }
}