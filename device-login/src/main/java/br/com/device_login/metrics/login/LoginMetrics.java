package br.com.device_login.metrics.login;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class LoginMetrics {

    private final String serviceName = "device-login";
    private final MeterRegistry meterRegistry;
    private final Timer successLoginTimer;
    private final Timer failedLoginTimer;
    private final Timer successRefreshTokensTimer;
    private final Timer failedRefreshTokensTimer;

    public LoginMetrics(MeterRegistry meterRegistry) {

        this.meterRegistry = meterRegistry;

        // Timer SEPARADO para sucessos do login
        this.successLoginTimer = Timer.builder("login_success_duration_seconds")
                .tags("service", this.serviceName)
                .register(meterRegistry);

        // Timer SEPARADO para falhas do login
        this.failedLoginTimer = Timer.builder("login_failed_duration_seconds")
                .tags("service", this.serviceName)
                .register(meterRegistry);

        // Timer SEPARADO para sucessos do refresh token
        this.successRefreshTokensTimer = Timer.builder("refresh_tokens_success_duration_seconds")
                .tags("service", this.serviceName)
                .register(meterRegistry);

        this.failedRefreshTokensTimer = Timer.builder("refresh_tokens_failed_duration_seconds")
                .tags("service", this.serviceName)
                .register(meterRegistry);
    }

    //MÉTRICAS DE CONTAGEM
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
                "refresh_tokens_success_total", "service", this.serviceName).increment();
    }

    public void failedRefreshTokens() {
        this.meterRegistry.counter(
                "refresh_tokens_failed_total", "service", this.serviceName).increment();
    }

    // Métricas de TEMPO
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopSuccessLoginTimer(Timer.Sample sample) {
        sample.stop(successLoginTimer);
    }

    public void stopFailedLoginTimer(Timer.Sample sample) {
        sample.stop(failedLoginTimer);
    }

    public void stopSuccessRefreshTokensTimer(Timer.Sample sample) {
        sample.stop(successRefreshTokensTimer);
    }

    public void stopFailedRefreshTokensTimer(Timer.Sample sample) {
        sample.stop(failedRefreshTokensTimer);
    }
}
