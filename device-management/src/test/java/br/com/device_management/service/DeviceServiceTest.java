package br.com.device_management.service;

import br.com.device_management.dtos.DeviceManagementEventForSensor;
import br.com.device_management.dtos.UpdateDeviceDto;
import br.com.device_management.dtos.register.DeviceDto;
import br.com.device_management.enums.Type;
import br.com.device_management.infra.exceptions.DeviceIsEmpty;
import br.com.device_management.infra.exceptions.DeviceIsPresent;
import br.com.device_management.infra.exceptions.ServiceUnavailable;
import br.com.device_management.metrics.timers.TimerMetrics;
import br.com.device_management.model.Device;
import br.com.device_management.repository.DeviceRepository;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @InjectMocks
    private DeviceService deviceService;

    @Mock
    private KafkaTemplate<String, DeviceManagementEventForSensor> kafkaTemplate;

    @Mock
    private TimerMetrics timerMetrics;

    //REGISTER

    @Test
    void shouldReturnEmptyBecauseDeviceIsEmpty() {

        when(this.deviceRepository.findByDeviceModel("model"))
                .thenReturn(Optional.empty());

        this.deviceService.verifyIfDeviceIsPresent("model");

        verify(this.deviceRepository).findByDeviceModel("model");
        verifyNoInteractions(timerMetrics);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void shouldReturnThrowBecauseDeviceIsPresent() {

        var device = mock(Device.class);

        when(this.deviceRepository.findByDeviceModel("model"))
                .thenReturn(Optional.of(device));

        assertThrows(DeviceIsPresent.class,
                () -> this.deviceService.verifyIfDeviceIsPresent("model"));

        verify(this.deviceRepository).findByDeviceModel("model");
        verifyNoInteractions(this.timerMetrics);
        verifyNoInteractions(this.kafkaTemplate);
    }

    @Test
    void shouldReturnThrowBecauseDeviceIsPresentCircuitBreakerOpen() {

        assertThrows(ServiceUnavailable.class,
                () -> this.deviceService.verifyIfDeviceIsPresentCircuitBreaker(
                        "model", new DataAccessException(""){}));

        verifyNoInteractions(this.deviceRepository);
        verifyNoInteractions(this.timerMetrics);
        verifyNoInteractions(this.kafkaTemplate);
    }

    @Test
    void shouldReturnDeviceDtoWhenSaveDevice() {

        var request = mock(DeviceDto.class);

        when(request.type()).thenReturn(Type.ACCELEROMETER);
        var response = this.deviceService.save(request);

        assertNotNull(response);
        verifyNoInteractions(this.kafkaTemplate);
        verifyNoInteractions(this.timerMetrics);
    }

    @Test
    void shouldReturnSuccessWhenSendMessage() {

        var request = mock(DeviceDto.class);
        when(request.type()).thenReturn(Type.ACCELEROMETER);

        this.deviceService.sendEvent("topic", request);

        verify(this.kafkaTemplate).send("topic", new DeviceManagementEventForSensor(
                request.name(),
                request.type().toString(),
                request.description(),
                request.deviceModel(),
                request.manufacturer(),
                request.type().getUnit().toString(),
                request.type().getMin(),
                request.type().getMax()));

        verifyNoInteractions(this.deviceRepository);
        verifyNoInteractions(this.timerMetrics);
    }

    @Test
    void shouldReturnResponseDeviceDtoWhenRegisterDeviceIsSuccess() {

        var sample = mock(Timer.Sample.class);
        var request = new DeviceDto(
                "A",
                Type.TEMPERATURE_SENSOR,
                "B",
                "C",
                "D",
                "E"
        );

        when(this.timerMetrics.startTimer())
                .thenReturn(sample);

        when(this.deviceRepository.findByDeviceModel("C"))
                .thenReturn(Optional.empty());

        when(this.deviceRepository.save(any(Device.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        var response = this.deviceService.registerDevice(request);

        assertNotNull(response);
        verify(this.timerMetrics).stopRegisterTimer(sample);
        verify(this.kafkaTemplate).send(anyString(), any());
    }

    // ===============================================================================================================

    // UPDATE
    @Test
    void shouldReturnDeviceBecauseDeviceIsPresent() {

        when(this.deviceRepository.findByDeviceModel("model"))
                .thenReturn(Optional.of(new Device()));

        var response = this.deviceService.verifyIfDeviceIsEmpty("model");

        assertNotNull(response);
        verify(this.deviceRepository).findByDeviceModel("model");
        verifyNoInteractions(this.kafkaTemplate);
        verifyNoInteractions(this.timerMetrics);
    }

    @Test
    void shouldReturnThrowBecauseDeviceIsEmpty() {

        when(this.deviceRepository.findByDeviceModel("model"))
                .thenReturn(Optional.empty());

        assertThrows(DeviceIsEmpty.class,
                () -> this.deviceService.verifyIfDeviceIsEmpty("model"));

        verify(this.deviceRepository).findByDeviceModel("model");
        verifyNoInteractions(this.timerMetrics);
        verifyNoInteractions(this.kafkaTemplate);
    }

    @Test
    void shouldReturnThrowBecauseDeviceIsEmptyRetry() {

        assertThrows(ServiceUnavailable.class,
                () -> this.deviceService.verifyIfDeviceIsEmptyRetry(
                        "", new DataAccessException("") {}));

        verifyNoInteractions(this.deviceRepository);
        verifyNoInteractions(this.kafkaTemplate);
        verifyNoInteractions(this.timerMetrics);
    }

    @Test
    void shouldReturnThrowBecauseDeviceIsEmptyCircuitBreaker() {

        assertThrows(ServiceUnavailable.class,
                () -> this.deviceService.verifyIfDeviceIsEmptyCircuitBreaker(
                        "", new DataAccessException("") {}));

        verifyNoInteractions(this.deviceRepository);
        verifyNoInteractions(this.kafkaTemplate);
        verifyNoInteractions(this.timerMetrics);
    }

    @Test
    void shouldReturnDeviceDtoWhenUpdateDevice() {

        var response = this.deviceService.saveUpdate(new Device(),
                new UpdateDeviceDto(
                        "",
                        "",
                        "",
                        "",
                        "") );

        assertNotNull(response);
        verifyNoInteractions(this.timerMetrics);
        verifyNoInteractions(this.kafkaTemplate);
    }

    @Test
    void shouldReturnResponseDeviceDtoWhenUpdateDevice() {

        var sample = mock(Timer.Sample.class);
        var device = new Device();
        device.setType(Type.TEMPERATURE_SENSOR);
        var updateDevice = new UpdateDeviceDto(
                "",
                "model",
                "",
                "",
                "");

        when(this.timerMetrics.startTimer())
                .thenReturn(sample);

        when(this.deviceRepository.findByDeviceModel("model"))
                .thenReturn(Optional.of(device));

        when(this.deviceRepository.save(device))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        var response = this.deviceService.updateDevice("model", updateDevice);

        assertNotNull(response);
        verify(this.timerMetrics).stopUpdateTimer(sample);
        verifyNoInteractions(this.kafkaTemplate);
    }

    // DELETE

    @Test
    void shouldReturnSuccessWhenDeleteDevice() {
        this.deviceService.delete(new Device());

        verifyNoInteractions(this.kafkaTemplate);
        verifyNoInteractions(this.timerMetrics);
    }

    @Test
    void shouldReturnThrowWhenDeleteDevice() {

        doThrow(new DataAccessException("Database is down") {})
                .when(this.deviceRepository)
                .delete(any(Device.class));

        assertThrows(ServiceUnavailable.class,
                () -> this.deviceService.delete(new Device()));

        verifyNoInteractions(this.timerMetrics);
        verifyNoInteractions(this.kafkaTemplate);
    }

    @Test
    void shouldReturnResponseDeviceDtoWhenDeleteService() {

        var sample = mock(Timer.Sample.class);
        var device = mock(Device.class);

        when(this.timerMetrics.startTimer())
                .thenReturn(sample);

        when(device.getType()).thenReturn(Type.ACCELEROMETER);
        when(this.deviceRepository.findByDeviceModel("model"))
                .thenReturn(Optional.of(device));

        var response = this.deviceService.deleteDevice("model");

        assertNotNull(response);
        verify(this.timerMetrics).stopDeleteTimer(sample);
        verifyNoInteractions(this.kafkaTemplate);
    }

    // Pego o dispositivo através de seu modelo
    @Test
    void shouldReturnGetDeviceWithDeviceModelWhenGetDeviceWithDeviceModelService() {

        var sample = mock(Timer.Sample.class);

        when(this.timerMetrics.startTimer())
                .thenReturn(sample);

        when(this.deviceRepository.findByDeviceModel("model"))
                .thenReturn(Optional.of(new Device()));
        var response = this.deviceService.getDeviceWithDeviceModel("model");

        assertNotNull(response);
        verify(this.timerMetrics).stopGetDeviceTimer(sample);
        verifyNoInteractions(this.kafkaTemplate);
    }

    // Retorna todos os dispositivos
    @Test
    void shouldReturnListResponseDeviceDtoWhenGetAllDevicesService() {

        var sample = mock(Timer.Sample.class);

        when(this.timerMetrics.startTimer())
                .thenReturn(sample);

        when(this.deviceRepository.findAllDevices(any(Pageable.class)))
                .thenReturn(Page.empty());

        var response = this.deviceService.getAllDevices(0, 1);

        assertNotNull(response);
        verify(this.timerMetrics).stopGetDevicesTimer(sample);
        verifyNoInteractions(this.kafkaTemplate);
    }

    @Test
    void shouldReturnListOfWhenGetAllDevicesRetry() {

        var response = this.deviceService.getAllDevicesRetry(1, 2, new DataAccessException("") {});

        assertNotNull(response);
        verifyNoInteractions(this.kafkaTemplate);
        verifyNoInteractions(this.timerMetrics);
        verifyNoInteractions(this.deviceRepository);
    }

    @Test
    void shouldReturnListOfWhenGetAllDevicesCircuitBreaker() {

        var response = this.deviceService.getAllDevicesCircuitBreaker(1, 2, new DataAccessException("") {});
        assertNotNull(response);
        verifyNoInteractions(this.kafkaTemplate);
        verifyNoInteractions(this.timerMetrics);
        verifyNoInteractions(this.deviceRepository);
    }
}