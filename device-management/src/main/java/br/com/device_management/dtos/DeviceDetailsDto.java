package br.com.device_management.dtos;

public record DeviceDetailsDto(
        String name,
        String deviceModel,
        String manufacturer,
        String location,
        String description
) {
}
