package br.com.device_management.infra.global;

import br.com.device_management.dtos.exception.RequestExceptionDto;
import br.com.device_management.dtos.exception.ResponseExceptionDto;
import br.com.device_management.infra.exceptions.DeviceIsEmpty;
import br.com.device_management.infra.exceptions.DeviceIsPresent;
import br.com.device_management.infra.exceptions.ServiceUnavailable;
import br.com.device_management.metrics.excepiton.MetricsForExceptions;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MetricsForExceptions metrics;
    private final String serviceName = "device-management";

    public GlobalExceptionHandler(MetricsForExceptions metrics) {
        this.metrics = metrics;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseExceptionDto> handleValidationExceptions(MethodArgumentNotValidException ex,
                                                                           HttpServletRequest request) {

        this.metrics.recordErrors(new RequestExceptionDto(
                HttpStatus.BAD_REQUEST.toString(),
                "Validation error",
                ex.getMessage(),
                request.getRequestURI())
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseExceptionDto(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                "DEVICE-MANAGEMENT",
                this.serviceName,
                ex.getMessage(),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(ServiceUnavailable.class)
    public ResponseEntity<ResponseExceptionDto> handleServiceUnavailableExceptions(ServiceUnavailable ex,
                                                                                   HttpServletRequest request) {

        this.metrics.recordErrors(new RequestExceptionDto(
                HttpStatus.SERVICE_UNAVAILABLE.toString(),
                "Service unavailable",
                ex.getMessage(),
                request.getRequestURI())
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ResponseExceptionDto(
                Instant.now().toString(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service unavailable",
                "DEVICE-MANAGEMENT",
                this.serviceName,
                ex.getMessage(),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(DeviceIsEmpty.class)
    public ResponseEntity<ResponseExceptionDto> handleDeviceIsEmptyException(DeviceIsEmpty ex,
                                                                               HttpServletRequest request) {

        this.metrics.recordErrors(new RequestExceptionDto(
                HttpStatus.CONFLICT.toString(),
                "Device not found",
                ex.getMessage(),
                request.getRequestURI())
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseExceptionDto(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                "Device not found",
                "DEVICE-MANAGEMENT",
                this.serviceName,
                ex.getMessage(),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(DeviceIsPresent.class)
    public ResponseEntity<ResponseExceptionDto> handleDeviceIsPresentException(DeviceIsPresent ex,
                                                                               HttpServletRequest request) {

        this.metrics.recordErrors(new RequestExceptionDto(
                HttpStatus.CONFLICT.toString(),
                "Device already cadastred",
                ex.getMessage(),
                request.getRequestURI())
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseExceptionDto(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                "Device already cadastred",
                "DEVICE-MANAGEMENT",
                this.serviceName,
                ex.getMessage(),
                request.getRequestURI()
        ));
    }
}

