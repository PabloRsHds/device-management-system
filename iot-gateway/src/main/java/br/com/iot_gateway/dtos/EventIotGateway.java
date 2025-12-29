package br.com.iot_gateway.dtos;

import br.com.iot_gateway.enums.Unit;

public record EventIotGateway(
        String deviceId,
        Unit unit,
        Float minLimit,
        Float maxLimit
) {
}
