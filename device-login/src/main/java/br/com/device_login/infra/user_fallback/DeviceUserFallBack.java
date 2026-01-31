package br.com.device_login.infra.user_fallback;

import br.com.device_login.infra.exceptions.InvalidCredentialsException;
import br.com.device_login.infra.exceptions.ServiceUnavailableException;
import br.com.device_login.microservice.UserClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class DeviceUserFallBack implements FallbackFactory<UserClient> {

    private static final Logger log = LoggerFactory.getLogger(DeviceUserFallBack.class);

    @Override
    public UserClient create(Throwable cause) {

        if (cause instanceof FeignException.Unauthorized) {

            log.error("Invalid credentials");
            throw new InvalidCredentialsException("This user does not have authorization");
        }

        log.error("Service unavailable, please try again later");
        throw new ServiceUnavailableException("Service unavailable, please try again later");
    }
}
