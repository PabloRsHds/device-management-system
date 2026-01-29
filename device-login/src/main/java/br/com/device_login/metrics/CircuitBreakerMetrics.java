package br.com.device_login.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CircuitBreakerMetrics {

    @Value("${spring.application.name}")
    private String serviceName;

    private final MeterRegistry meterRegistry;

    public CircuitBreakerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordCircuitBreakerOpened() {
        meterRegistry.counter("circuit_breaker_opened_total",
                "service", this.serviceName
        ).increment();
    }

    public void recordCircuitBreakerResponse(String context, String status) {
        meterRegistry.counter("circuit_breaker_responses_total",
                "service", this.serviceName,
                "context", context,
                "status", status
        ).increment();
    }
}
