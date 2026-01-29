package br.com.device_login.infra;

import br.com.device_login.dtos.loginDto.ResponseUserForLogin;
import br.com.device_login.microservice.UserClient;
import br.com.device_login.service.metrics.MetricsService;
import org.springframework.stereotype.Component;

@Component
public class DeviceUserFallBack implements UserClient {

    private final MetricsService metricsService;

    public DeviceUserFallBack(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Override
    public ResponseUserForLogin getUserForLoginWithEmail(String email) {

        metricsService.recordCircuitBreakerOpened("DEVICE-USER");

        throw new ServiceUnavailableException("Service unavailable, please try again later");
    }
}
