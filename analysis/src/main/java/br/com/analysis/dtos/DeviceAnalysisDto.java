package br.com.analysis.dtos;

import br.com.analysis.enums.Unit;

public record DeviceAnalysisDto(
        Float minLimit,
        Float maxLimit,
        Unit unit,
        String updatedAt,
        String createdAt,
        Float lastReadingMinLimit,
        Float lastReadingMaxLimit,
        String lastReadingUpdateAt

) {
}
