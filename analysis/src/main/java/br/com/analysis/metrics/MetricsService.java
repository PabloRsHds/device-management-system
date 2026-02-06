package br.com.analysis.metrics;

import br.com.analysis.dtos.exception.ExceptionMetricDto;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private final String serviceName = "analysis";
    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void metricForExceptions(ExceptionMetricDto dto) {
        this.meterRegistry.counter(
                        "errors_lookup_total",
                        "output", dto.httpStatus(),
                        "error_type", dto.errorType(),
                        "description", dto.description(),
                        "path", dto.path())
                .increment();
    }

    public void analysisSuccess(boolean success) {

        var trueOrFalseString = "";

        if (success) {
            trueOrFalseString = "success";
        } else {
            trueOrFalseString = "failed";
        }

        this.meterRegistry.counter("analysis_total",
                "output", trueOrFalseString)
                .increment();
    }

    public void failSendEvent() {
        this.meterRegistry.counter("kafka_producer_total",
                "service", this.serviceName)
                .increment();
    }

    public void failConsumerEvent() {
        this.meterRegistry.counter("kafka_consumer_total",
                        "service", this.serviceName)
                .increment();
    }
}
