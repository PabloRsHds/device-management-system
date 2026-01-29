package br.com.analysis.dtos;

public record DeviceAnalysisDto(
        String name,
        String deviceModel,
        Float minLimit,
        Float maxLimit,
        String unit,
        String updatedAt,
        String createdAt,
        Float lastReadingMinLimit,
        Float lastReadingMaxLimit,
        String lastReadingUpdateAt,
        int analysisWorked,
        int analysisFailed

) {
}
