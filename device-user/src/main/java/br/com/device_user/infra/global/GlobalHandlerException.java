package br.com.device_user.infra.global;

import br.com.device_user.dtos.exceptionDto.ResponseExceptionDto;
import br.com.device_user.dtos.metricsDto.ExceptionMetricDto;
import br.com.device_user.infra.exceptions.ServiceUnavailableException;
import br.com.device_user.metrics.MetricsForExceptions;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalHandlerException {

    private final String serviceName = "device_user";
    private final MetricsForExceptions metricsForExceptions;

    public GlobalHandlerException(MetricsForExceptions metricsForExceptions) {
        this.metricsForExceptions = metricsForExceptions;
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ResponseExceptionDto> serviceUnavailableException(ServiceUnavailableException ex, HttpServletRequest request){

        this.metricsForExceptions.recordErrors(
                new ExceptionMetricDto(HttpStatus.SERVICE_UNAVAILABLE.toString(),
                        "SERVICE_UNAVAILABLE",
                        "Service Unavailable",
                        request.getRequestURI())
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
            new ResponseExceptionDto(
                    Instant.now().toString(),
                    HttpStatus.SERVICE_UNAVAILABLE.value(),
                    "service unavailable",
                    "DEVICE-USER",
                    "DATABASE",
                    this.serviceName,
                    ex.getMessage(),
                    request.getRequestURI()
            ));
    }
}
