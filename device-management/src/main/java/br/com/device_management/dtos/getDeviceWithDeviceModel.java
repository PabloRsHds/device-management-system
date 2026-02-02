package br.com.device_management.dtos;

public record getDeviceWithDeviceModel(
        String name,
        String deviceModel,
        String manufacturer,
        String location,
        String description
) {
}
