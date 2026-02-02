package br.com.device_management.dtos;

public record UpdateDeviceDto(
        String newName,
        String newDeviceModel,
        String newManufacturer,
        String newLocation,
        String newDescription
) {
}
