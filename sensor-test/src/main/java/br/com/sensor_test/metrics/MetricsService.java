package br.com.sensor_test.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class MetricsService {

    private final String serviceName = "sensor-test";

    private final MeterRegistry meterRegistry;
    private final Timer consumerTimer;
    private final Timer updateTimer;
    private final Timer deleteTimer;
    private final Timer sensorsTimer;

    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.consumerTimer = Timer.builder("consumer_timer")
                .tags("service", serviceName)
                .register(meterRegistry);

        this.updateTimer = Timer.builder("update_timer")
                .tags("service", serviceName)
                .register(meterRegistry);

        this.deleteTimer = Timer.builder("delete_timer")
                .tags("service", serviceName)
                .register(meterRegistry);

        this.sensorsTimer = Timer.builder("sensors_timer")
                .tags("service", serviceName)
                .register(meterRegistry);
    }

    public void metricForScheduling() {
        this.meterRegistry.counter("scheduling_error",
                        "output", "find_all_sensors")
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
