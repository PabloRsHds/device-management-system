package br.com.device_login.dtos.exceptionDto;

public record RequestExceptionDto(

        String httpStatus,
        String errorType ,
        String description,
        String path
) {
}
