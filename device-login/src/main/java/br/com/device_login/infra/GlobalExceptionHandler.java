package br.com.device_login.infra;

import br.com.device_login.infra.exceptions.InvalidCredentialsException;
import br.com.device_login.infra.exceptions.ServiceUnavailableException;
import br.com.device_login.metrics.CircuitBreakerMetrics;
import br.com.device_login.metrics.UserServiceMetrics;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.application.name}")
    private String serviceName;
    private final CircuitBreakerMetrics circuitBreakerMetrics;
    private final UserServiceMetrics userServiceMetrics;

    public GlobalExceptionHandler(CircuitBreakerMetrics circuitBreakerMetrics,
                                  UserServiceMetrics userServiceMetrics) {
        this.circuitBreakerMetrics = circuitBreakerMetrics;
        this.userServiceMetrics = userServiceMetrics;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentialsException(InvalidCredentialsException ex,
                                                                                 HttpServletRequest request) {

        this.circuitBreakerMetrics.recordCircuitBreakerResponse(request.getRequestURI(), "401");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", HttpStatus.UNAUTHORIZED.value(),
                        "error","User does not have authorization",
                        "source", "DEVICE-LOGIN",
                        "target", "USER-DEVICE",
                        "service", this.serviceName,
                        "message", ex.getMessage(),
                        "path", request.getRequestURI()
                ));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleServiceUnavailableException(ServiceUnavailableException ex,
                                                                                 HttpServletRequest request) {

        this.userServiceMetrics.recordServiceUnavailable(request.getRequestURI());
        this.circuitBreakerMetrics.recordCircuitBreakerResponse(request.getRequestURI(), "503");

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                Map.of(
                        "timestamp", Instant.now().toString(),
                        "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "error","Service unavailable",
                        "service", this.serviceName,
                        "message", ex.getMessage(),
                        "path", request.getRequestURI()
                ));
    }

    // Pega as mensagens de erro das validações diretamente por causa do
    // @Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                          HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                Map.of("timestamp", Instant.now().toString(),
                        "status", HttpStatus.BAD_REQUEST.value(),
                        "error", "Validation incorrect",
                        "service", this.serviceName,
                        "message", ex.getMessage(),
                        "path", request.getRequestURI())
        );
    }

}
