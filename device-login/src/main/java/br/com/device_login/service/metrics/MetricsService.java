package br.com.device_login.service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordCircuitBreakerOpened(String serviceName) {
        meterRegistry.counter("circuit_breaker.opened",
                "service", serviceName
        ).increment();
    }

    public void recordServiceUnavailable(String serviceName, String path) {
        meterRegistry.counter("service.unavailable.responses",
                "service", serviceName,
                "path", path
        ).increment();
    }

    // ⭐ NOVO: Método para respostas do Circuit Breaker
    public void recordCircuitBreakerResponse(String serviceName, String context, String status) {
        meterRegistry.counter("circuit_breaker.responses",
                "service", serviceName,
                "context", context,
                "status", status
        ).increment();
    }

}