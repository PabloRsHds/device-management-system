package br.com.device_user.dtos;

import br.com.device_user.enums.Role;

public record ResponseUserForLogin(
        String userId,
        String password,
        Role role
) {
}
