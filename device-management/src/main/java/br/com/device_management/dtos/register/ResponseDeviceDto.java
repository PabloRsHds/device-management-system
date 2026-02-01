package br.com.device_management.dtos.register;

import br.com.device_management.enums.Type;

public record ResponseDeviceDto(
        String name,
        Type type,
        String description,
        String deviceModel,
        String manufacturer,
        String location
) {
}
