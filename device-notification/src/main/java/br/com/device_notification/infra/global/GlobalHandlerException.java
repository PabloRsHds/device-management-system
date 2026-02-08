package br.com.device_notification.infra.global;

import br.com.device_notification.dtos.exception.ResponseExceptionDto;
import br.com.device_notification.infra.exceptions.NotificationNotFoundEx;
import br.com.device_notification.infra.exceptions.ServiceUnavailableEx;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalHandlerException {

    private final String serviceName = "device-notification";

    @ExceptionHandler(ServiceUnavailableEx.class)
    public ResponseEntity<ResponseExceptionDto> serviceUnavailable(ServiceUnavailableEx ex,
                                                                   HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ResponseExceptionDto(
                        Instant.now().toString(),
                        HttpStatus.SERVICE_UNAVAILABLE.value(),
                        "The service is unavailable",
                        "DEVICE-NOTIFICATION",
                        this.serviceName,
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(NotificationNotFoundEx.class)
    public ResponseEntity<ResponseExceptionDto> notificationNotFound(NotificationNotFoundEx ex,
                                                                     HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseExceptionDto(
                Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                "Notification not found",
                "DEVICE-NOTIFICATION",
                this.serviceName,
                ex.getMessage(),
                request.getRequestURI()
        ));
    }
}
