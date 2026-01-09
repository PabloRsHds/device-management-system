package br.com.device_login.dtos.tokenDto;

public record ResponseTokens(
        String accessToken,
        String refreshToken
) {
}
