package br.com.sensor_test.dtos;

import br.com.sensor_test.enums.Status;

public record AllSensorsDto(
        String name,
        String type,
        String deviceModel,
        String manufacturer,
        Status status
) {
}
