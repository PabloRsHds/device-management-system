package br.com.sensor_test.infra.global;

import br.com.sensor_test.dtos.exceptionDto.ResponseExceptionDto;
import br.com.sensor_test.infra.exceptions.DeviceIsPresentException;
import br.com.sensor_test.infra.exceptions.ServiceUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalHandlerException {

    @Value("${spring.application.name}")
    private String serviceName;

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<?> serviceUnavailable(ServiceUnavailableException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                new ResponseExceptionDto(
                        Instant.now().toString(),
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "SERVICE_UNAVAILABLE",
                        "SENSOR-TEST",
                        "DATABASE",
                        this.serviceName,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(DeviceIsPresentException.class)
    public ResponseEntity<?> deviceIsPresent(DeviceIsPresentException ex, HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ResponseExceptionDto(
                        Instant.now().toString(),
                        HttpStatus.CONFLICT.value(),
                        "CONFLICT",
                        "SENSOR-TEST",
                        "DATABASE",
                        this.serviceName,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }
}
