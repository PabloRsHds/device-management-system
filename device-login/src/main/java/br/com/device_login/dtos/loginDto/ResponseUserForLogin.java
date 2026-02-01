package br.com.device_login.dtos.loginDto;

public record ResponseUserForLogin(

        String userId,
        String password,
        String role
) {
}
