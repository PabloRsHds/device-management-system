package br.com.device_notification.dtos.exception;

public record ResponseExceptionDto(
        String timesTamp,
        int status,
        String error,
        String source,
        String service,
        String message,
        String path
) {
}
