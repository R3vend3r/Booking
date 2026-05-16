package booking.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, Object>> handleServiceException(ServiceException e) {
        logger.error("ServiceException перехвачена: {}", e.getMessage(), e);
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        HttpStatus status;
        String message = e.getMessage();

        if (message != null && (message.contains("не найден") || message.contains("не найдено") || message.contains("не существует"))) {
            status = HttpStatus.NOT_FOUND;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", e.getMessage());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(BookingException.class)
    public ResponseEntity<Map<String, Object>> handleBookingException(BookingException e) {
        logger.error("BookingException перехвачена: {}", e.getMessage(), e);
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());

        HttpStatus status;
        String message = e.getMessage();

        if (message != null && (message.contains("не найден") || message.contains("не найдено") || message.contains("не существует"))) {
            status = HttpStatus.NOT_FOUND;
        } else {
            status = HttpStatus.BAD_REQUEST;
        }

        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", e.getMessage());
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException e) {
        logger.error("Validation error: {}", e.getMessage());
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        response.put("errors", errors);

        String firstError = errors.values().stream().findFirst().orElse("Ошибка валидации");
        response.put("message", firstError);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        logger.error("Необработанное исключение: {}", e.getMessage(), e);
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "Произошла внутренняя ошибка сервера");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}