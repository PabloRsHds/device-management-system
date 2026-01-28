package br.com.device_login.infra;

import br.com.device_login.dtos.loginDto.ResponseUserForLogin;
import br.com.device_login.microservice.UserClient;
import org.springframework.stereotype.Component;

@Component
public class DeviceUserFallBack implements UserClient {

    @Override
    public ResponseUserForLogin getUserForLoginWithEmail(String email) {

        throw new ServiceUnavailableException("Service unavailable, please try again later");
    }
}
