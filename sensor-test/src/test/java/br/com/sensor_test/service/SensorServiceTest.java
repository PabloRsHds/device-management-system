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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
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

    // ======================================= REGISTER SENSOR TEST ===================================================

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

    @Test
    void shouldReturnThrowWhenVerifyIfSensorIsPresent() {

        when(this.sensorRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.empty());

        assertThrows(SensorIsEmptyException.class,
                () -> this.sensorService.verifyIfSensorIsPresent("deviceModel"));

        verify(this.sensorRepository).findByDeviceModel("deviceModel");
        verifyNoInteractions(this.metricsService);
    }

    @Test
    void shouldReturnResponseSensorDtoWhenUpdate() {

        var response = this.sensorService
                .update(new Sensor(), new UpdateSensor("name", "", ""));

        assertNotNull(response);
    }

    // ===============================================================================================================

    // ========================================== DELETE SENSOR TEST =================================================

    @Test
    void shouldReturnResponseSensorDtoDeleteSensor() {

        var sample = mock(Timer.Sample.class);

        when(this.metricsService.startTimer())
                .thenReturn(sample);

        when(this.sensorRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Sensor()));

        this.sensorService.deleteSensor("deviceModel");

        verify(this.metricsService).stopDeleteTimer(sample);
        verify(this.metricsService).startTimer();
        verify(this.sensorRepository).findByDeviceModel("deviceModel");
    }

    @Test
    void shouldReturnVoidDelete() {

        this.sensorService.delete(new Sensor());
    }

    // ===============================================================================================================

    // =================================== PEGA TODOS OS SENSORES TEST ===============================================

    @Test
    void shouldReturnListResponseSensorDtoWhenGetAllSensorsActivated() {

        var sample = mock(Timer.Sample.class);

        when(this.metricsService.startTimer())
                .thenReturn(sample);

        when(this.sensorRepository.findAllSensors(PageRequest.of(0, 1)))
                .thenReturn(Page.empty());

        var response = this.sensorService.getAllSensorsActivated(0, 1);

        assertNotNull(response);
        verify(this.metricsService).stopSensorsTimer(sample);
    }

    // ===============================================================================================================

    // ================================= ALTERA O STATUS DO SENSOR TEST ==============================================

    @Test
    void shouldReturnResponseSensorDtoWhenChangeStatus() {

        var sensor = mock(Sensor.class);

        when(sensor.getStatus()).thenReturn(Status.ACTIVATED);
        when(this.sensorRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(sensor));

        this.sensorService.changeStatus("deviceModel");
        verify(this.sensorRepository).findByDeviceModel("deviceModel");
        verifyNoInteractions(this.metricsService);
    }

    @Test
    void shouldReturnThrowWhenChangeStatus() {

        assertThrows(SensorIsEmptyException.class,
                () -> this.sensorService.changeStatus("deviceModel"));

        verifyNoInteractions(this.metricsService);
    }

    @Test
    void shouldReturnResponseSensorDtoWhenChange() {

        var sensor = mock(Sensor.class);

        when(sensor.getStatus()).thenReturn(Status.ACTIVATED);

        var response = this.sensorService.change(sensor);

        assertNotNull(response);
        verifyNoInteractions(this.metricsService);
    }

    // ===============================================================================================================

    // ========================================= PEGO O STATUS TEST ==================================================

    @Test
    void shouldReturnStringWhenGetStatus() {

        var sensor = mock(Sensor.class);

        when(sensor.getStatus()).thenReturn(Status.ACTIVATED);

        when(this.sensorRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(sensor));

        var response = this.sensorService.getStatus("deviceModel");

        assertNotNull(response);
        verify(this.sensorRepository).findByDeviceModel("deviceModel");
        verifyNoInteractions(this.metricsService);
    }

    @Test
    void shouldReturnThrowWhenGetStatus() {

        assertThrows(SensorIsEmptyException.class,
                () -> this.sensorService.getStatus("deviceModel"));

        verifyNoInteractions(this.metricsService);
    }
}