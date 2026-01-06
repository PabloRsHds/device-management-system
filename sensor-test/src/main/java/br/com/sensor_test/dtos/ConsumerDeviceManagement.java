package br.com.sensor_test.dtos;

import br.com.sensor_test.enums.Type;
import br.com.sensor_test.enums.Unit;

public record ConsumerDeviceManagement(
        String name,
        Type type,
        String description,
        String deviceModel,
        String manufacturer,
        Unit unit) {
}
