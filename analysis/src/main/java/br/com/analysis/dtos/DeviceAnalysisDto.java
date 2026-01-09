package br.com.analysis.dtos;

public record DeviceAnalysisDto(
        Float minLimit,
        Float maxLimit,
        String unit,
        String updatedAt,
        String createdAt,
        Float lastReadingMinLimit,
        Float lastReadingMaxLimit,
        String lastReadingUpdateAt

) {
}
