package br.com.device_login.dtos;

public record RequestLoginDto(
        String email,
        String password
) {
}
