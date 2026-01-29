package br.com.device_login.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class LoginMetrics {

    private final String serviceName = "device-login";
    private final MeterRegistry meterRegistry;
    private final Timer successLoginTimer;
    private final Timer failedLoginTimer;

    public LoginMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Timer SEPARADO para sucessos
        this.successLoginTimer = Timer.builder("login_success_duration_seconds")
                .tags("service", this.serviceName)
                .register(meterRegistry);

        // Timer SEPARADO para falhas
        this.failedLoginTimer = Timer.builder("login_failed_duration_seconds")
                .tags("service", this.serviceName)
                .register(meterRegistry);
    }

    public void userNotFound() {
        this.meterRegistry.counter(
                "login_user_not_found_total", "service", this.serviceName).increment();
    }

    public void invalidCredentials() {
        meterRegistry.counter(
                "login_invalid_credentials_total", "service", this.serviceName).increment();
    }

    public void loginSuccess() {
        this.meterRegistry.counter(
                "login_success_total", "service", this.serviceName).increment();
    }

    public void refreshTokenSuccess() {
        this.meterRegistry.counter(
                "refresh_token_success_total", "service", this.serviceName).increment();
    }

    public void refreshTokenInvalid() {
        this.meterRegistry.counter(
                "refresh_token_invalid_total", "service", this.serviceName).increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopSuccessLoginTimer(Timer.Sample sample) {
        sample.stop(successLoginTimer);
    }

    public void stopFailedLoginTimer(Timer.Sample sample) {
        sample.stop(failedLoginTimer);
    }
}
