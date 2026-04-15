package br.com.sensor_test.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private static final String SERVICE_NAME = "sensor-test";

    private final MeterRegistry meterRegistry;
    private final Timer consumerTimer;
    private final Timer updateTimer;
    private final Timer deleteTimer;
    private final Timer sensorsTimer;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.consumerTimer = Timer.builder("consumer_timer")
                .tags("service", SERVICE_NAME)
                .register(meterRegistry);

        this.updateTimer = Timer.builder("update_timer")
                .tags("service", SERVICE_NAME)
                .register(meterRegistry);

        this.deleteTimer = Timer.builder("delete_timer")
                .tags("service", SERVICE_NAME)
                .register(meterRegistry);

        this.sensorsTimer = Timer.builder("sensors_timer")
                .tags("service", SERVICE_NAME)
                .register(meterRegistry);
    }

    public void metricFailedDatabaseInScheduling() {
        this.meterRegistry.counter("scheduling_database_error",
                        "output", "failed_database_scheduling")
                .increment();
    }

    public void metricFailedConsumer() {
        this.meterRegistry.counter("kafka_consumer_error",
                        "output", "failed_kafka_consumer")
                .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopConsumerTimer(Timer.Sample sample) {
        sample.stop(this.consumerTimer);
    }

    public void stopUpdateTimer(Timer.Sample sample) {
        sample.stop(this.updateTimer);
    }

    public void stopDeleteTimer(Timer.Sample sample) {
        sample.stop(this.deleteTimer);
    }

    public void stopSensorsTimer(Timer.Sample sample) {
        sample.stop(this.sensorsTimer);
    }
}
