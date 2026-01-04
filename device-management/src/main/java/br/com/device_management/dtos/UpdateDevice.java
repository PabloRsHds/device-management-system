package br.com.device_management.dtos;

public record UpdateDevice(
        String newName,
        String newDeviceModel,
        String newManufacturer,
        String newLocation,
        String newDescription
) {
}
