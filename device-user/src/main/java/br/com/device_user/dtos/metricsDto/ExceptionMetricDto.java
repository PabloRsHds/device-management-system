package br.com.device_user.dtos.metricsDto;

public record ExceptionMetricDto(
        String httpStatus,
        String errorType ,
        String description,
        String path
) {
}
