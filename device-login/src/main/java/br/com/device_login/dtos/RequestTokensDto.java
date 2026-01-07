package br.com.device_login.dtos;

public record RequestTokensDto(
        String accessToken,
        String refreshToken
) {
}
