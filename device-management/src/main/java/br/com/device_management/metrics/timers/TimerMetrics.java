package br.com.device_management.metrics.timers;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class TimerMetrics {

    private final String service = "device-management";

    private final MeterRegistry meterRegistry;
    private final Timer registerTimer;
    private final Timer updateTimer;
    private final Timer deleteTimer;
    private final Timer getDeviceTimer;
    private final Timer getDevicesTimer;

    public TimerMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.registerTimer = Timer.builder("register-timer")
                .tags("service",service)
                .register(meterRegistry);

        this.updateTimer = Timer.builder("update-timer")
                .tags("service",service)
                .register(meterRegistry);

        this.deleteTimer = Timer.builder("delete-timer")
                .tags("service",service)
                .register(meterRegistry);

        this.getDeviceTimer = Timer.builder("get-device-timer-timer")
                .tags("service",service)
                .register(meterRegistry);

        this.getDevicesTimer = Timer.builder("get-devices-timer-timer")
                .tags("service",service)
                .register(meterRegistry);
    }

    public Timer.Sample startTimer(){
        return Timer.start(meterRegistry);
    }

    public void stopRegisterTimer(Timer.Sample sample) {
        sample.stop(registerTimer);
    }

    public void stopUpdateTimer(Timer.Sample sample) {
        sample.stop(updateTimer);
    }

    public void stopDeleteTimer(Timer.Sample sample) {
        sample.stop(deleteTimer);
    }

    public void stopGetDeviceTimer(Timer.Sample sample) {
        sample.stop(getDeviceTimer);
    }

    public void stopGetDevicesTimer(Timer.Sample sample) {
        sample.stop(getDevicesTimer);
    }

}
