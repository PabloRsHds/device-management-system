package br.com.device_management.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateDeviceDto(

        @NotBlank(message = "The name field cannot be blank")
        @Size(min = 2, max = 30, message = "The name field must have between 2 and 30 characters")
        String newName,

        @NotBlank(message = "The device model field cannot be blank")
        @Size(min = 2, max = 30, message = "The device model field must have between 1 and 30 characters")
        String newDeviceModel,

        @NotBlank(message = "The manufacturer field cannot be blank")
        @Size(min = 2, max = 30, message = "The manufacturer field must have between 1 and 30 characters")
        String newManufacturer,

        @NotBlank(message = "The location field cannot be blank")
        @Size(max = 100, message = "The location field must have a maximum of 100 characters")
        String newLocation,

        @NotBlank(message = "The description field cannot be blank")
        @Size(max = 200, message = "The description field must have a maximum of 200 characters")
        String newDescription
) {
}
