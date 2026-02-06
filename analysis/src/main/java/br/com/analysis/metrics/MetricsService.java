package br.com.analysis.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private final String serviceName = "analysis";
    private final MeterRegistry meterRegistry;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void analysisSuccess(boolean success) {

        var trueOrFalseString = "";

        if (success) {
            trueOrFalseString = "success";
        } else {
            trueOrFalseString = "failed";
        }

        this.meterRegistry.counter("analysis",
                "output", trueOrFalseString)
                .increment();
    }

    public void failSendEvent() {
        this.meterRegistry.counter("kafka_producer",
                "service", this.serviceName)
                .increment();
    }

    public void failConsumerEvent() {
        this.meterRegistry.counter("kafka_consumer",
                        "service", this.serviceName)
                .increment();
    }
}
