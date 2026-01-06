package br.com.analysis.dtos;

import br.com.analysis.enums.Type;
import br.com.analysis.enums.Unit;

public record ConsumerSensorTest(
        String name,
        Type type,
        String description,
        String deviceModel,
        String manufacturer,
        Unit unit,
        Float minLimit,
        Float maxLimit
) {
}
