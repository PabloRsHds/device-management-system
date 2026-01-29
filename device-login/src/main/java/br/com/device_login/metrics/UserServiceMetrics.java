package br.com.device_login.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserServiceMetrics {

    @Value("${spring.application.name}")
    private String serviceName;
    private final MeterRegistry meterRegistry;

    @Autowired
    public UserServiceMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordServiceUnavailable(String path) {
        meterRegistry.counter("service_unavailable_total",
                "service", this.serviceName,
                "path", path
        ).increment();
    }
}
