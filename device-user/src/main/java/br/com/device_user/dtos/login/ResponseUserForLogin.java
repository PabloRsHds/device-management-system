package br.com.device_user.dtos.login;

public record ResponseUserForLogin(
        String userId,
        String password,
        String role
) {
}
