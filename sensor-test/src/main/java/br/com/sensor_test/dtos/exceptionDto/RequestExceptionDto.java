package br.com.sensor_test.dtos.exceptionDto;

public record RequestExceptionDto(

        String httpStatus,
        String errorType ,
        String description,
        String path
) {
}
