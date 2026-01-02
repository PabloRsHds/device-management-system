package br.com.analysis.dtos;

public record DeviceAnalysis(

        String updatedAt,
        String createdAt,
        Float minLimit,
        Float maxLimit
) {
}
