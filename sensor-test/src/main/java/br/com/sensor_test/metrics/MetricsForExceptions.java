package br.com.sensor_test.metrics;

import br.com.sensor_test.dtos.exceptionDto.ExceptionMetricDto;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsForExceptions {

    private final MeterRegistry meterRegistry;

    public MetricsForExceptions(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordErrors(ExceptionMetricDto request) {
        this.meterRegistry.counter(
                "errors_lookup_total",
                        "output", request.httpStatus(),
                        "error_type", request.errorType(),
                        "description", request.description(),
                        "path", request.path())
                .increment();
    }
}
