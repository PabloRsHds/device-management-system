package br.com.sensor_test.dtos;

public record UpdateSensor(
        String name,
        String deviceModel,
        String manufacturer
) {
}
