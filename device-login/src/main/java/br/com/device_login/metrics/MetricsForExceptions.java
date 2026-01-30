package br.com.device_login.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsForExceptions {

    private final MeterRegistry meterRegistry;

    public MetricsForExceptions(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordErrors(String httpStatus, String errorType , String description, String path) {
        this.meterRegistry.counter(
                "errors_lookup_total",
                        "output", httpStatus,
                        "error_type", errorType,
                        "description", description,
                        "path", path)
                .increment();
    }
}
