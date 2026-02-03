package br.com.sensor_test.dtos.sensor;

import br.com.sensor_test.enums.Status;

public record ResponseSensorDto(
        String name,
        String type,
        String deviceModel,
        String manufacturer,
        Status status
) {
}
