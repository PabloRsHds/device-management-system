package br.com.sensor_test.dtos;

public record SensorForAnalysisEvent(
        String name,
        String type,
        String description,
        String deviceModel,
        String manufacturer,
        String unit,
        Float minLimit,
        Float maxLimit,
        Float minValue,
        Float maxValue
) {
}
