package br.com.sensor_test.dtos.exceptionDto;

public record ExceptionMetricDto(
        String httpStatus,
        String errorType ,
        String description,
        String path
) {
}
