package br.com.device_login.metrics.login;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoginMetrics {

    private static final Logger log = LoggerFactory.getLogger(LoginMetrics.class);
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
        log.info("Metrics: User not found");
        this.meterRegistry.counter(
                "login_user_not_found_total", "service", this.serviceName).increment();
    }

    public void invalidCredentials() {
        log.info("Metrics: Invalid credentials not found");
        meterRegistry.counter(
                "login_invalid_credentials_total", "service", this.serviceName).increment();
    }

    public void loginSuccess() {
        log.info("Metrics: Login success");
        this.meterRegistry.counter(
                "login_success_total", "service", this.serviceName).increment();
    }

    public void refreshTokenSuccess() {
        log.info("Metrics: Tokens generated with success");
        this.meterRegistry.counter(
                "refresh_tokens_success_total", "service", this.serviceName).increment();
    }

    public void failedRefreshTokens() {
        log.info("Metrics: Failed generated tokens");
        this.meterRegistry.counter(
                "refresh_tokens_failed_total", "service", this.serviceName).increment();
    }

    // Métricas de TEMPO
    public Timer.Sample startTimer() {
        log.info("Metrics: Start timer");
        return Timer.start(meterRegistry);
    }

    public void stopSuccessLoginTimer(Timer.Sample sample) {
        log.info("Metrics: Stop timer with login success");
        sample.stop(successLoginTimer);
    }

    public void stopFailedLoginTimer(Timer.Sample sample) {
        log.info("Metrics: Stop timer with failed login");
        sample.stop(failedLoginTimer);
    }

    public void stopSuccessRefreshTokensTimer(Timer.Sample sample) {
        log.info("Metrics: Stop timer with success generated tokens");
        sample.stop(successRefreshTokensTimer);
    }

    public void stopFailedRefreshTokensTimer(Timer.Sample sample) {
        log.info("Metrics: Stop timer with failed generated tokens");
        sample.stop(failedRefreshTokensTimer);
    }
}
