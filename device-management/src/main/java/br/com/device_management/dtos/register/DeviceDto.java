package br.com.device_management.dtos.register;

import br.com.device_management.enums.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeviceDto(

        @NotBlank(message = "The name field cannot be blank")
        @Size(min = 2, max = 30, message = "The name field must have between 2 and 50 characters")
        String name,

        @NotNull(message = "The type field cannot be null")
        Type type,

        @NotBlank(message = "The description field cannot be blank")
        @Size(max = 200, message = "The description field must have a maximum of 200 characters")
        String description,

        @NotBlank(message = "The device model field cannot be blank")
        @Size(min = 2, max = 30, message = "The device model field must have between 1 and 50 characters")
        String deviceModel,

        @NotBlank(message = "The manufacturer field cannot be blank")
        @Size(min = 1, max = 50, message = "The manufacturer field must have between 1 and 50 characters")
        String manufacturer,

        @NotBlank(message = "The location field cannot be blank")
        @Size(max = 100, message = "The location field must have a maximum of 100 characters")
        String location
) {
}
