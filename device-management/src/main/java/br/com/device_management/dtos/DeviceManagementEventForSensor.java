package br.com.device_management.dtos;

public record DeviceManagementEventForSensor(
        String name,
        String type,
        String description,
        String deviceModel,
        String manufacturer,
        String unit,
        Float minLimit,
        Float maxLimit
) {
}
