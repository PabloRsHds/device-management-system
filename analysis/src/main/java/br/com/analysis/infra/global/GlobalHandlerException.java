package br.com.analysis.infra.global;

import br.com.analysis.dtos.ResponseExceptionDto;
import br.com.analysis.dtos.exception.ExceptionMetricDto;
import br.com.analysis.infra.exceptions.DeviceNotFoundException;
import br.com.analysis.infra.exceptions.ServiceUnavailableException;
import br.com.analysis.metrics.MetricsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalHandlerException {

    private final String serviceName = "analysis";

    private final MetricsService metricsService;

    public GlobalHandlerException(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ResponseExceptionDto> handleDeviceNotFoundException(DeviceNotFoundException ex,
                                                                              HttpServletRequest request) {

        this.metricsService.metricForExceptions(new ExceptionMetricDto(
                HttpStatus.NOT_FOUND.toString(),
                "NOT FOUND",
                ex.getMessage(),
                request.getRequestURI()
        ));

        return ResponseEntity.badRequest().body(new ResponseExceptionDto(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "NOT FOUND",
                "ANALYSIS",
                "DATABASE",
                this.serviceName,
                ex.getMessage(),
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ResponseExceptionDto> handleServiceUnavailableException(ServiceUnavailableException ex,
                                                                              HttpServletRequest request) {

        this.metricsService.metricForExceptions(new ExceptionMetricDto(
                HttpStatus.SERVICE_UNAVAILABLE.toString(),
                "SERVICE UNAVAILABLE",
                ex.getMessage(),
                request.getRequestURI()
        ));

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ResponseExceptionDto(
                Instant.now().toString(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "SERVICE UNAVAILABLE",
                "ANALYSIS",
                "DATABASE",
                this.serviceName,
                ex.getMessage(),
                request.getRequestURI()
        ));
    }
}
