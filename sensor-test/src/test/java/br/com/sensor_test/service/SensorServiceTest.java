package br.com.sensor_test.service;

import br.com.sensor_test.dtos.ConsumerDeviceManagement;
import br.com.sensor_test.infra.exceptions.SensorIsPresentException;
import br.com.sensor_test.metrics.MetricsService;
import br.com.sensor_test.model.Sensor;
import br.com.sensor_test.repository.SensorRepository;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

    @Mock
    private MetricsService metricsService;

    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private SensorService sensorService;

    // =========================================== REGISTER SENSOR ===================================================

    @Test
    void shouldReturnSuccessWhenRegisterSensor() {

        var sample = mock(Timer.Sample.class);
        var request = new ConsumerDeviceManagement(
                "",
                "",
                "",
                "deviceModel",
                "",
                "",
                1f,
                10f);

        when(this.metricsService.startTimer())
                .thenReturn(sample);

        when(this.sensorRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.empty());

        this.sensorService.registerSensor(request);

        verify(this.metricsService).startTimer();
        verify(this.metricsService).stopConsumerTimer(sample);
        verify(this.sensorRepository).findByDeviceModel("deviceModel");
    }


}