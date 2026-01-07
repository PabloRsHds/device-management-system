package br.com.device_login.dtos;

public record ResponseTokens(
        String accessToken,
        String refreshToken
) {
}
