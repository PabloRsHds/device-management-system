package br.com.device_login.dtos.loginDto;


import br.com.device_login.enums.Role;

public record ResponseUserForLogin(

        String userId,
        String password,
        Role role
) {
}
