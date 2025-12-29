package br.com.iot_gateway.dtos;

import br.com.iot_gateway.enums.Type;
import br.com.iot_gateway.enums.Unit;

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
