package br.com.analysis.infra.global;

import br.com.analysis.dtos.ResponseExceptionDto;
import br.com.analysis.infra.exceptions.DeviceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalHandlerException {

    private final String serviceName = "analysis";

    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ResponseExceptionDto> handleDeviceNotFoundException(DeviceNotFoundException ex,
                                                                              HttpServletRequest request) {

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
}
