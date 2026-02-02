package br.com.device_management.metrics.excepiton;

import br.com.device_management.dtos.exception.RequestExceptionDto;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsForExceptions {

    private final MeterRegistry meterRegistry;

    public MetricsForExceptions(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordErrors(RequestExceptionDto request) {
        this.meterRegistry.counter(
                        "errors_lookup_total",
                        "output", request.httpStatus(),
                        "error_type", request.errorType(),
                        "description", request.description(),
                        "path", request.path())
                .increment();
    }
}
