package br.com.device_login.infra.global_exceptions;

import br.com.device_login.dtos.exceptionDto.RequestExceptionDto;
import br.com.device_login.dtos.exceptionDto.ResponseExceptionDto;
import br.com.device_login.infra.exceptions.InvalidCredentialsException;
import br.com.device_login.infra.exceptions.ServiceUnavailableException;
import br.com.device_login.metrics.exception.MetricsForExceptions;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.application.name}")
    private String serviceName;
    private final MetricsForExceptions metricsForExceptions;

    public GlobalExceptionHandler(MetricsForExceptions metricsForExceptions) {
        this.metricsForExceptions = metricsForExceptions;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ResponseExceptionDto> handleInvalidCredentialsException(InvalidCredentialsException ex,
                                                                                  HttpServletRequest request) {

        this.metricsForExceptions.recordErrors(new RequestExceptionDto(
                HttpStatus.UNAUTHORIZED.toString(),
                "invalid_credentials",
                ex.getMessage(),
                request.getRequestURI())
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ResponseExceptionDto(
                        Instant.now().toString(),
                        HttpStatus.UNAUTHORIZED.value(),
                        "Invalid or expired credentials",
                        "DEVICE-LOGIN",
                        "USER-DEVICE",
                        this.serviceName,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ResponseExceptionDto> handleServiceUnavailableException(ServiceUnavailableException ex,
                                                                                 HttpServletRequest request) {

        this.metricsForExceptions.recordErrors(new RequestExceptionDto(
                HttpStatus.SERVICE_UNAVAILABLE.toString(),
                "service_unavailable",
                ex.getMessage(),
                request.getRequestURI())
        );


        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new ResponseExceptionDto(
                        Instant.now().toString(),
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "Service unavailable",
                        "DEVICE-LOGIN",
                        "USER-DEVICE",
                        this.serviceName,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    // Pega as mensagens de erro das validações diretamente por causa do
    // @Validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseExceptionDto> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                          HttpServletRequest request) {

        this.metricsForExceptions.recordErrors(new RequestExceptionDto(
                        HttpStatus.BAD_REQUEST.toString(),
                        "validation_error",
                        ex.getMessage(),
                        request.getRequestURI())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ResponseExceptionDto(
                        Instant.now().toString(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation incorrect",
                        "DEVICE-LOGIN",
                        "USER-DEVICE",
                        this.serviceName,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

}
