package br.com.analysis.dtos.exception;

public record ExceptionMetricDto(
        String httpStatus,
        String errorType ,
        String description,
        String path
) {
}
