package br.com.device_management.dtos;

public record FindByDeviceWithDeviceModel(
        String name,
        String deviceModel,
        String manufacturer,
        String location,
        String description
) {
}
