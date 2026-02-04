package br.com.sensor_test.infra.global;

import br.com.sensor_test.dtos.exceptionDto.ExceptionMetricDto;
import br.com.sensor_test.dtos.exceptionDto.ResponseExceptionDto;
import br.com.sensor_test.infra.exceptions.SensorIsEmptyException;
import br.com.sensor_test.infra.exceptions.SensorIsPresentException;
import br.com.sensor_test.infra.exceptions.ServiceUnavailableException;
import br.com.sensor_test.metrics.MetricsForExceptions;
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
    private final MetricsForExceptions metrics;

    public GlobalHandlerException(MetricsForExceptions metrics) {
        this.metrics = metrics;
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ResponseExceptionDto> serviceUnavailable(ServiceUnavailableException ex, HttpServletRequest request) {

        this.metrics.recordErrors(
                new ExceptionMetricDto(
                    HttpStatus.SERVICE_UNAVAILABLE.toString(),
                    "Service unavailable",
                    ex.getMessage(),
                    request.getRequestURI()
                )
        );

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

    @ExceptionHandler(SensorIsPresentException.class)
    public ResponseEntity<ResponseExceptionDto> sensorIsPresent(SensorIsPresentException ex, HttpServletRequest request) {

        this.metrics.recordErrors(
                new ExceptionMetricDto(
                        HttpStatus.NOT_FOUND.toString(),
                        "Sensor is present in the database",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseExceptionDto(
                        Instant.now().toString(),
                        HttpStatus.NOT_FOUND.value(),
                        "NOT FOUND",
                        "SENSOR-TEST",
                        "DATABASE",
                        this.serviceName,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(SensorIsEmptyException.class)
    public ResponseEntity<ResponseExceptionDto> sensorIsEmpty(SensorIsEmptyException ex, HttpServletRequest request) {

        this.metrics.recordErrors(
                new ExceptionMetricDto(
                        HttpStatus.NOT_FOUND.toString(),
                        "Sensor is not present in the database",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseExceptionDto(
                        Instant.now().toString(),
                        HttpStatus.NOT_FOUND.value(),
                        "NOT FOUND",
                        "SENSOR-TEST",
                        "DATABASE",
                        this.serviceName,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }
}
