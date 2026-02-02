package br.com.device_management.dtos;

import br.com.device_management.enums.Type;
import br.com.device_management.enums.Unit;

public record ResponseDeviceDto(
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
