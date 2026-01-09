package br.com.device_login.dtos.loginDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RequestLoginDto(

        @NotBlank(message = "The email field cannot be blank")
        @Size(min = 11, max = 60, message = "The E-mail must have at least 11 characters, and a maximum of 60 characters.")
        @Email(message = "@ is required")
        @Pattern(
                regexp = "^[a-zA-Z0-9._%+-]+@gmail\\.com$",
                message = "Invalid email format. Exemple lara@gmail.com"
        )
        String email,

        @NotBlank(message = "The password field cannot be blank")
        @Size(min = 8, max = 30, message = "The password must be between 8 and 30 characters")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$#!%&])\\S{8,}$",
                message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 symbol (no spaces)"
        )
        String password
) {
}
