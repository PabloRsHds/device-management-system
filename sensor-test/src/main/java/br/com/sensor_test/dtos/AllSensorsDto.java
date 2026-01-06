package br.com.sensor_test.dtos;

import br.com.sensor_test.enums.Status;
import br.com.sensor_test.enums.Type;

public record AllSensorsDto(
        String name,
        Type type,
        String deviceModel,
        String manufacturer,
        Status status
) {
}
