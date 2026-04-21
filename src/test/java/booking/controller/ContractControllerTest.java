package booking.controller;

import booking.dto.request.ContractRequest;
import booking.dto.response.ContractResponse;
import booking.enums.PaymentMethod;
import booking.enums.PaymentStatus;
import booking.exception.GlobalExceptionHandler;
import booking.exception.ServiceException;
import booking.service.ContractService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContractControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContractService contractService;

    @InjectMocks
    private ContractController contractController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders
                .standaloneSetup(contractController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .defaultRequest(get("/").accept(MediaType.APPLICATION_JSON))
                .build();
    }

    // ==================== CREATE CONTRACT ====================

    @Test
    void createContract_shouldReturnCreatedWhenSuccess() throws Exception {
        ContractRequest request = new ContractRequest();
        request.setBookingId("B-123");
        request.setPaymentMethod(PaymentMethod.CARD);

        ContractResponse response = new ContractResponse(
                "C-123", "CON-001", 15000L, PaymentStatus.PENDING,
                null, PaymentMethod.CARD, "B-123"
        );

        when(contractService.createContract(any(ContractRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("C-123"))
                .andExpect(jsonPath("$.contractNumber").value("CON-001"))
                .andExpect(jsonPath("$.totalAmount").value(15000))
                .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$.bookingId").value("B-123"));

        verify(contractService, times(1)).createContract(any(ContractRequest.class));
    }

    @Test
    void createContract_shouldReturnBadRequestWhenBookingIdIsNull() throws Exception {
        ContractRequest request = new ContractRequest();
        request.setBookingId(null);

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(contractService, never()).createContract(any(ContractRequest.class));
    }

    @Test
    void createContract_shouldReturnNotFoundWhenBookingMissing() throws Exception {
        ContractRequest request = new ContractRequest();
        request.setBookingId("B-999");
        request.setPaymentMethod(PaymentMethod.CARD);

        when(contractService.createContract(any(ContractRequest.class)))
                .thenThrow(new ServiceException("Бронирование с ID B-999 не найдено"));

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(contractService, times(1)).createContract(any(ContractRequest.class));
    }

    @Test
    void createContract_shouldReturnBadRequestWhenContractAlreadyExists() throws Exception {
        ContractRequest request = new ContractRequest();
        request.setBookingId("B-123");
        request.setPaymentMethod(PaymentMethod.CARD);

        when(contractService.createContract(any(ContractRequest.class)))
                .thenThrow(new ServiceException("Договор для этого бронирования уже существует"));

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(contractService, times(1)).createContract(any(ContractRequest.class));
    }

    @Test
    void createContract_shouldReturnBadRequestWhenBookingInPast() throws Exception {
        ContractRequest request = new ContractRequest();
        request.setBookingId("B-123");
        request.setPaymentMethod(PaymentMethod.CARD);

        when(contractService.createContract(any(ContractRequest.class)))
                .thenThrow(new ServiceException("Нельзя создать договор для прошедшего бронирования"));

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(contractService, times(1)).createContract(any(ContractRequest.class));
    }

    @Test
    void createContract_shouldReturnBadRequestWhenTotalAmountZero() throws Exception {
        ContractRequest request = new ContractRequest();
        request.setBookingId("B-123");
        request.setPaymentMethod(PaymentMethod.CARD);

        when(contractService.createContract(any(ContractRequest.class)))
                .thenThrow(new ServiceException("Нельзя создать договор с нулевой суммой"));

        mockMvc.perform(post("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(contractService, times(1)).createContract(any(ContractRequest.class));
    }

    // ==================== GET CONTRACT BY ID ====================

    @Test
    void getContractById_shouldReturnContractWhenFound() throws Exception {
        String contractId = "C-123";
        ContractResponse response = new ContractResponse(
                contractId, "CON-001", 15000L, PaymentStatus.PENDING,
                null, PaymentMethod.CARD, "B-123"
        );

        when(contractService.getContractById(contractId)).thenReturn(response);

        mockMvc.perform(get("/api/contracts/{id}", contractId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("C-123"))
                .andExpect(jsonPath("$.contractNumber").value("CON-001"))
                .andExpect(jsonPath("$.totalAmount").value(15000));

        verify(contractService, times(1)).getContractById(contractId);
    }

    @Test
    void getContractById_shouldReturnNotFoundWhenMissing() throws Exception {
        String contractId = "C-999";
        when(contractService.getContractById(contractId))
                .thenThrow(new ServiceException("Договор с ID C-999 не найден"));

        mockMvc.perform(get("/api/contracts/{id}", contractId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(contractService, times(1)).getContractById(contractId);
    }

    // ==================== GET CONTRACT BY BOOKING ID ====================

    @Test
    void getContractByBookingId_shouldReturnContractWhenFound() throws Exception {
        String bookingId = "B-123";
        ContractResponse response = new ContractResponse(
                "C-123", "CON-001", 15000L, PaymentStatus.PENDING,
                null, PaymentMethod.CARD, bookingId
        );

        when(contractService.getContractByBookingId(bookingId)).thenReturn(response);

        mockMvc.perform(get("/api/contracts/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(bookingId));

        verify(contractService, times(1)).getContractByBookingId(bookingId);
    }

    @Test
    void getContractByBookingId_shouldReturnNotFoundWhenMissing() throws Exception {
        String bookingId = "B-999";
        when(contractService.getContractByBookingId(bookingId))
                .thenThrow(new ServiceException("Договор для бронирования B-999 не найден"));

        mockMvc.perform(get("/api/contracts/booking/{bookingId}", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(contractService, times(1)).getContractByBookingId(bookingId);
    }

    // ==================== GET CONTRACT WITH DETAILS ====================

    @Test
    void getContractWithDetails_shouldReturnContractWithDetailsWhenFound() throws Exception {
        String contractId = "C-123";
        ContractResponse response = new ContractResponse(
                contractId, "CON-001", 15000L, PaymentStatus.PENDING,
                null, PaymentMethod.CARD, "B-123"
        );

        when(contractService.getContractWithDetails(contractId)).thenReturn(response);

        mockMvc.perform(get("/api/contracts/{id}/details", contractId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contractId));

        verify(contractService, times(1)).getContractWithDetails(contractId);
    }

    @Test
    void getContractWithDetails_shouldReturnNotFoundWhenMissing() throws Exception {
        String contractId = "C-999";
        when(contractService.getContractWithDetails(contractId))
                .thenThrow(new ServiceException("Договор с ID C-999 не найден"));

        mockMvc.perform(get("/api/contracts/{id}/details", contractId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(contractService, times(1)).getContractWithDetails(contractId);
    }

    // ==================== GET ALL CONTRACTS ====================

    @Test
    void getAllContracts_shouldReturnListOfContracts() throws Exception {
        List<ContractResponse> contracts = Arrays.asList(
                new ContractResponse("C-123", "CON-001", 15000L, PaymentStatus.PENDING, null, PaymentMethod.CARD, "B-123"),
                new ContractResponse("C-456", "CON-002", 25000L, PaymentStatus.PAID, LocalDateTime.now(), PaymentMethod.CASH, "B-456")
        );

        when(contractService.getAllContracts()).thenReturn(contracts);

        mockMvc.perform(get("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("C-123"))
                .andExpect(jsonPath("$[0].totalAmount").value(15000))
                .andExpect(jsonPath("$[1].id").value("C-456"))
                .andExpect(jsonPath("$[1].totalAmount").value(25000));

        verify(contractService, times(1)).getAllContracts();
    }

    @Test
    void getAllContracts_shouldReturnEmptyListWhenNoContracts() throws Exception {
        when(contractService.getAllContracts()).thenReturn(List.of());

        mockMvc.perform(get("/api/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(contractService, times(1)).getAllContracts();
    }

    // ==================== GET ALL CONTRACTS WITH BOOKINGS ====================

    @Test
    void getAllContractsWithBookings_shouldReturnListOfContracts() throws Exception {
        List<ContractResponse> contracts = Arrays.asList(
                new ContractResponse("C-123", "CON-001", 15000L, PaymentStatus.PENDING, null, PaymentMethod.CARD, "B-123"),
                new ContractResponse("C-456", "CON-002", 25000L, PaymentStatus.PAID, LocalDateTime.now(), PaymentMethod.CASH, "B-456")
        );

        when(contractService.getAllContractsWithBookings()).thenReturn(contracts);

        mockMvc.perform(get("/api/contracts/with-bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("C-123"))
                .andExpect(jsonPath("$[1].id").value("C-456"));

        verify(contractService, times(1)).getAllContractsWithBookings();
    }

    // ==================== GET PENDING CONTRACTS ====================

    @Test
    void getPendingContracts_shouldReturnListOfPendingContracts() throws Exception {
        List<ContractResponse> pendingContracts = Arrays.asList(
                new ContractResponse("C-123", "CON-001", 15000L, PaymentStatus.PENDING, null, null, "B-123"),
                new ContractResponse("C-456", "CON-002", 25000L, PaymentStatus.PENDING, null, null, "B-456")
        );

        when(contractService.getPendingContracts()).thenReturn(pendingContracts);

        mockMvc.perform(get("/api/contracts/pending")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentStatus").value("PENDING"))
                .andExpect(jsonPath("$[1].paymentStatus").value("PENDING"));

        verify(contractService, times(1)).getPendingContracts();
    }

    @Test
    void getPendingContracts_shouldReturnEmptyListWhenNoPending() throws Exception {
        when(contractService.getPendingContracts()).thenReturn(List.of());

        mockMvc.perform(get("/api/contracts/pending")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(contractService, times(1)).getPendingContracts();
    }

    // ==================== PAY CONTRACT ====================

    @Test
    void payContract_shouldPaySuccessfully() throws Exception {
        String contractId = "C-123";
        PaymentMethod method = PaymentMethod.CARD;

        ContractResponse response = new ContractResponse(
                contractId, "CON-001", 15000L, PaymentStatus.PAID,
                LocalDateTime.now(), method, "B-123"
        );

        when(contractService.payContract(eq(contractId), eq(method))).thenReturn(response);

        mockMvc.perform(post("/api/contracts/{contractId}/pay", contractId)
                        .param("method", method.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"));

        verify(contractService, times(1)).payContract(contractId, method);
    }

    @Test
    void payContract_shouldReturnNotFoundWhenContractMissing() throws Exception {
        String contractId = "C-999";
        PaymentMethod method = PaymentMethod.CARD;

        when(contractService.payContract(eq(contractId), eq(method)))
                .thenThrow(new ServiceException("Договор с ID C-999 не найден"));

        mockMvc.perform(post("/api/contracts/{contractId}/pay", contractId)
                        .param("method", method.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(contractService, times(1)).payContract(contractId, method);
    }

    @Test
    void payContract_shouldReturnBadRequestWhenAlreadyPaid() throws Exception {
        String contractId = "C-123";
        PaymentMethod method = PaymentMethod.CARD;

        when(contractService.payContract(eq(contractId), eq(method)))
                .thenThrow(new ServiceException("Договор уже оплачен"));

        mockMvc.perform(post("/api/contracts/{contractId}/pay", contractId)
                        .param("method", method.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(contractService, times(1)).payContract(contractId, method);
    }

    @Test
    void payContract_shouldReturnBadRequestWhenCancelled() throws Exception {
        String contractId = "C-123";
        PaymentMethod method = PaymentMethod.CARD;

        when(contractService.payContract(eq(contractId), eq(method)))
                .thenThrow(new ServiceException("Нельзя оплатить отмененный договор"));

        mockMvc.perform(post("/api/contracts/{contractId}/pay", contractId)
                        .param("method", method.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(contractService, times(1)).payContract(contractId, method);
    }

    // ==================== CANCEL CONTRACT ====================

    @Test
    void cancelContract_shouldCancelSuccessfully() throws Exception {
        String contractId = "C-123";

        ContractResponse response = new ContractResponse(
                contractId, "CON-001", 15000L, PaymentStatus.CANCELLED,
                null, null, "B-123"
        );

        when(contractService.cancelContract(contractId)).thenReturn(response);

        mockMvc.perform(post("/api/contracts/{contractId}/cancel", contractId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("CANCELLED"));

        verify(contractService, times(1)).cancelContract(contractId);
    }

    @Test
    void cancelContract_shouldReturnNotFoundWhenContractMissing() throws Exception {
        String contractId = "C-999";

        when(contractService.cancelContract(contractId))
                .thenThrow(new ServiceException("Договор с ID C-999 не найден"));

        mockMvc.perform(post("/api/contracts/{contractId}/cancel", contractId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(contractService, times(1)).cancelContract(contractId);
    }

    @Test
    void cancelContract_shouldReturnBadRequestWhenAlreadyPaid() throws Exception {
        String contractId = "C-123";

        when(contractService.cancelContract(contractId))
                .thenThrow(new ServiceException("Нельзя отменить оплаченный договор"));

        mockMvc.perform(post("/api/contracts/{contractId}/cancel", contractId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(contractService, times(1)).cancelContract(contractId);
    }

    // ==================== GET TOTAL INCOME ====================

    @Test
    void getTotalIncome_shouldReturnTotalIncome() throws Exception {
        Long totalIncome = 150000L;

        when(contractService.getTotalIncome()).thenReturn(totalIncome);

        mockMvc.perform(get("/api/contracts/total-income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("150000"));

        verify(contractService, times(1)).getTotalIncome();
    }

    @Test
    void getTotalIncome_shouldReturnZeroWhenNoIncome() throws Exception {
        when(contractService.getTotalIncome()).thenReturn(0L);

        mockMvc.perform(get("/api/contracts/total-income")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        verify(contractService, times(1)).getTotalIncome();
    }
}