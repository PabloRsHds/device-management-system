package br.com.sensor_test.dtos;

public record ConsumerDeviceManagement(
        String name,
        String type,
        String description,
        String deviceModel,
        String manufacturer,
        String unit,
        Float minLimit,
        Float maxLimit) {
}
