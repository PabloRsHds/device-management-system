package br.com.device_management.dtos;

import br.com.device_management.enums.Type;

public record DeviceForIotGateway(
        String deviceId,
        String name,
        Type type,
        String description,
        String deviceModel,
        String manufacturer
){
}
