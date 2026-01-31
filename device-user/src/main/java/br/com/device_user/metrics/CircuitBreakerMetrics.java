package br.com.device_user.metrics;

import br.com.device_user.dtos.metricsDto.CircuitBreakerMetricDto;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CircuitBreakerMetrics {

    private final String serviceName = "DEVICE-USER";

    private final MeterRegistry meterRegistry;

    public CircuitBreakerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordCircuitBreakerStates(CircuitBreakerMetricDto request) {

        log.info("EVENTO CAPTURADO! Circuit: {}, {} → {}",
                request.circuitName(),
                request.fromState(),
                request.toState());

        this.meterRegistry.counter("circuit_breaker_total",
                "circuit_name", request.circuitName(),
                "from_state", request.fromState(),
                "to_state",request.toState()).increment();
    }
}
