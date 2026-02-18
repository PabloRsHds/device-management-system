package br.com.device_user.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
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
    public void recordUserIsPresent(String trueOrFalse){

        log.info("Usuário está presente: {}", trueOrFalse);

        this.meterRegistry.counter(
                "user_lookup_total",
                        "service",this.serviceName,
                        "present", trueOrFalse)
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
