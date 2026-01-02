package br.com.analysis.dtos;

import br.com.analysis.enums.Status;
import br.com.analysis.enums.Type;
import br.com.analysis.enums.Unit;

public record DeviceAnalysisDto(
        String name,
        Type type,
        String description,
        String deviceModel,
        String manufacturer,
        Status status,
        String location,
        Unit unit,
        Float minLimit,
        Float maxLimit,
        Float lastReadingMinLimit,
        Float lastReadingMaxLimit,
        String lastReadingUpdateAt,
        String updatedAt,
        String createdAt

) {
}
