package br.com.device_user.dtos.exceptionDto;

public record ResponseExceptionDto(

        String timesTamp,
        int status,
        String error,
        String source,
        String target,
        String service,
        String message,
        String path
) {
}
