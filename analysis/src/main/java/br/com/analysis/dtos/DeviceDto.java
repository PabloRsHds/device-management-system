package br.com.analysis.dtos;

import br.com.analysis.enums.Type;
import br.com.analysis.enums.Unit;

public record DeviceDto(
        String deviceId,
        String name,
        Type type,
        String description,
        String deviceModel,
        String manufacturer,
        String location,
        Unit unit,
        Float minLimit,
        Float maxLimit
) {
}
