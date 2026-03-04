package br.com.sensor_test.service;

import br.com.sensor_test.dtos.ConsumerDeviceManagement;
import br.com.sensor_test.dtos.UpdateSensor;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.infra.exceptions.SensorIsEmptyException;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void shouldReturnVoidWhenVerifyIfSensorIsEmpty() {

        when(this.sensorRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.empty());

        this.sensorService.verifyIfSensorIsEmpty("deviceModel");

        verify(this.sensorRepository).findByDeviceModel("deviceModel");
        verifyNoInteractions(this.metricsService);
    }

    @Test
    void shouldReturnThrowWhenVerifyIfSensorIsEmpty() {

        when(this.sensorRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Sensor()));

        assertThrows(SensorIsPresentException.class,
                () -> this.sensorService.verifyIfSensorIsEmpty("deviceModel"));

        verify(this.sensorRepository).findByDeviceModel("deviceModel");
        verifyNoInteractions(this.metricsService);
    }

    @Test
    void shouldReturnVoidWhenSave() {

        this.sensorService.save(new ConsumerDeviceManagement(
                "",
                "",
                "",
                "",
                "",
                "unit",
                0f,
                1f
        ));
    }

    // ===============================================================================================================

    // ============================================= UPDATE SENSOR TEST ==============================================

    @Test
    void shouldReturnResponseSensorDtoWhenUpdateSensor() {

        var sample = mock(Timer.Sample.class);

        when(this.metricsService.startTimer())
                .thenReturn(sample);

        when(this.sensorRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Sensor()));

        var response = this.sensorService.updateSensor("deviceModel",
                new UpdateSensor("name", "", ""));

        assertNotNull(response);
        verify(this.metricsService).stopUpdateTimer(sample);
        verify(this.sensorRepository).findByDeviceModel("deviceModel");
    }

    @Test
    void shouldReturnSensorWhenVerifyIfSensorIsPresent() {

        when(this.sensorRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Sensor()));

        var response = this.sensorService.verifyIfSensorIsPresent("deviceModel");

        assertNotNull(response);
        verify(this.sensorRepository).findByDeviceModel("deviceModel");
        verifyNoInteractions(this.metricsService);
    }
}