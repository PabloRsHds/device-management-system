package br.com.analysis.dtos;

public record ConsumerSensorTest(
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
