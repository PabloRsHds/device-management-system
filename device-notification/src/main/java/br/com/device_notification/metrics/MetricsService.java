package br.com.device_notification.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void circuitbreaker(String name) {
        this.meterRegistry.counter(
                "circuitbreaker-count",
                        "name", name)
                .increment();
    }
}
