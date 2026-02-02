package br.com.device_management.dtos.exception;

public record RequestExceptionDto(
        String httpStatus,
        String errorType ,
        String description,
        String path
) {
}
