package br.com.device_user.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class UserMetrics {

    private final String serviceName = "DEVICE-USER";
    private final MeterRegistry meterRegistry;
    private final Timer userResponseFailedTimer;
    private final Timer userResponseSuccessTimer;

    public UserMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.userResponseSuccessTimer = Timer.builder("user_response_success_timer_duration_seconds")
                .tags("service", this.serviceName)
                .register(meterRegistry);

        this.userResponseFailedTimer = Timer.builder("user_response_failed_timer_duration_seconds")
                .tags("service", this.serviceName)
                .register(meterRegistry);
    }


    // Métricas de contagem
    public void recordUserFound(){
        this.meterRegistry.counter(
                "user_lookup_total",
                        "service",this.serviceName,
                        "outcome","found")
                .increment();
    }

    public void recordUserNotFound(){
        this.meterRegistry.counter("user_lookup_total",
                        "service", this.serviceName,
                        "outcome", "not_found")
                .increment();
    }

    // Métricas de tempo

    // Inicio o timer
    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopUserResponseFailedTimer(Timer.Sample sample) {
        sample.stop(userResponseFailedTimer);
    }

    public void stopUserResponseSuccessTimer(Timer.Sample sample) {
        sample.stop(userResponseSuccessTimer);
    }
}
