package br.com.analysis.dtos;

public record DeviceDto(
        String deviceId,
        String name,
        String type,
        String description,
        String deviceModel,
        String manufacturer,
        String location,
        String unit,
        Float minLimit,
        Float maxLimit
) {
}
