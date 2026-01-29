package br.com.device_login.infra;

import br.com.device_login.infra.exceptions.InvalidCredentialsException;
import br.com.device_login.infra.exceptions.ServiceUnavailableException;
import br.com.device_login.metrics.CircuitBreakerMetrics;
import br.com.device_login.microservice.UserClient;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class DeviceUserFallBack implements FallbackFactory<UserClient> {

    private static final Logger log = LoggerFactory.getLogger(DeviceUserFallBack.class);
    private final CircuitBreakerMetrics circuitBreakerMetrics;

    public DeviceUserFallBack(CircuitBreakerMetrics circuitBreakerMetrics) {
        this.circuitBreakerMetrics = circuitBreakerMetrics;
    }

    @Override
    public UserClient create(Throwable cause) {

        if (cause instanceof FeignException.Unauthorized) {
            throw new InvalidCredentialsException("This user does not have authorization");
        }

        circuitBreakerMetrics.recordCircuitBreakerOpened();
        throw new ServiceUnavailableException("Service unavailable, please try again later");
    }
}
