package br.com.device_login.dtos;


import br.com.device_login.enums.Role;

public record ResponseUserForLogin(
        String userId,
        String password,
        Role role
) {
}
