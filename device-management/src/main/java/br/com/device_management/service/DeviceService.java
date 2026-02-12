package br.com.device_management.service;

import br.com.device_management.dtos.DeviceManagementEventForSensor;
import br.com.device_management.dtos.ResponseDeviceDto;
import br.com.device_management.dtos.UpdateDeviceDto;
import br.com.device_management.dtos.getDeviceWithDeviceModel;
import br.com.device_management.dtos.register.DeviceDto;
import br.com.device_management.infra.exceptions.DeviceIsEmpty;
import br.com.device_management.infra.exceptions.DeviceIsPresent;
import br.com.device_management.infra.exceptions.ServiceUnavailable;
import br.com.device_management.metrics.timers.TimerMetrics;
import br.com.device_management.model.Device;
import br.com.device_management.repository.DeviceRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final TimerMetrics timer;
    private final KafkaTemplate<String, DeviceManagementEventForSensor> kafkaTemplate;

    @Autowired
    public DeviceService(
            DeviceRepository deviceRepository,
            TimerMetrics timer,
            KafkaTemplate<String, DeviceManagementEventForSensor> kafkaTemplate) {
        this.deviceRepository = deviceRepository;
        this.timer = timer;
        this.kafkaTemplate = kafkaTemplate;
    }


    // ========================================== REGISTER DEVICE ====================================================

    public ResponseDeviceDto registerDevice(DeviceDto request) {

        var sampleTimer = this.timer.startTimer();

        try {
            log.info("Verificando se o dispositivo ja esta cadastrado");
            this.verifyIfDeviceIsPresent(request.deviceModel());

            log.info("Salvando o dispositivo");
            var deviceDto = this.save(request);

            log.info("Enviando evento para o sensor");
            this.sendEvent("device-management-for-sensor-test-topic",deviceDto);

            return new ResponseDeviceDto(
                    deviceDto.name(),
                    deviceDto.type(),
                    deviceDto.description(),
                    deviceDto.deviceModel(),
                    deviceDto.manufacturer(),
                    deviceDto.location(),
                    deviceDto.type().getUnit(),
                    deviceDto.type().getMin(),
                    deviceDto.type().getMax()
            );

        } finally {

            this.timer.stopRegisterTimer(sampleTimer);
        }
    }

    @Retry(name = "retry_device_is_present", fallbackMethod = "verifyIfDeviceIsPresentRetry")
    @CircuitBreaker(name = "circuitbreaker_device_is_present", fallbackMethod = "verifyIfDeviceIsPresentCircuitBreaker")
    public void verifyIfDeviceIsPresent(String deviceModel) {

        Optional<Device> device = this.deviceRepository.findByDeviceModel(deviceModel);

        if (device.isPresent()) {
            log.info("Dispositivo já cadastrado");
            throw new DeviceIsPresent("This device model is already registered in the database");
        }
    }

    public void verifyIfDeviceIsPresentRetry(String deviceModel, Exception e) {
        log.error("Serviço de banco de dados indisponível, com isso não está sendo possível verificar se o dispositivo: {}" +
                "está cadastrado", deviceModel);
    }

    public void verifyIfDeviceIsPresentCircuitBreaker(String deviceModel, Exception e) {
        log.error("Circuit breaker aberto -  Banco de dados indisponível");
        throw new ServiceUnavailable("Database service unavailable, please try again later");
    }

    @Transactional
    public DeviceDto save(DeviceDto dto) {

        var newDevice = new Device();

        newDevice.setName(dto.name());
        newDevice.setType(dto.type());
        newDevice.setDescription(dto.description());
        newDevice.setDeviceModel(dto.deviceModel());
        newDevice.setManufacturer(dto.manufacturer());
        newDevice.setLocation(dto.location());
        newDevice.setUnit(dto.type().getUnit());
        newDevice.setMinLimit(dto.type().getMin());
        newDevice.setMaxLimit(dto.type().getMax());
        newDevice.setCreatedAt(LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        log.info("Salvando o dispositivo e enviando uma mensagem ao usuário");
        this.deviceRepository.save(newDevice);

        return new DeviceDto(
                newDevice.getName(),
                newDevice.getType(),
                newDevice.getDescription(),
                newDevice.getDeviceModel(),
                newDevice.getManufacturer(),
                newDevice.getLocation()
        );
    }

    @CircuitBreaker(name = "circuitbreaker_kafka_send_event", fallbackMethod = "sendEventCircuitBreaker")
    public void sendEvent(String topic, DeviceDto dto) {

        this.kafkaTemplate.send(topic,
                new DeviceManagementEventForSensor(
                        dto.name(),
                        dto.type().toString(),
                        dto.description(),
                        dto.deviceModel(),
                        dto.manufacturer(),
                        dto.type().getUnit().toString(),
                        dto.type().getMin(),
                        dto.type().getMax()
                ));
    }

    public void sendEventCircuitBreaker(String topic, DeviceDto dto, Exception e) {
        log.error("Kafka service unavailable, error: ", e);
    }

    // ================================================================================================================

    // =========================================== UPDATE =============================================================

    public ResponseDeviceDto updateDevice(String deviceModel,UpdateDeviceDto request) {

        var sampleTimer = this.timer.startTimer();

        try {
            log.info("Verificando se o dispositivo não está cadastrado");
            var entity = this.verifyIfDeviceIsEmpty(deviceModel);

            log.info("Salvando as atualizações");
            var deviceDto = this.saveUpdate(entity, request);

            log.debug("Salvo as atualizações e a retorno como um dto");
            return new ResponseDeviceDto(
                    deviceDto.name(),
                    deviceDto.type(),
                    deviceDto.description(),
                    deviceDto.deviceModel(),
                    deviceDto.manufacturer(),
                    deviceDto.location(),
                    deviceDto.type().getUnit(),
                    deviceDto.type().getMin(),
                    deviceDto.type().getMax()
            );

        } finally {
            this.timer.stopUpdateTimer(sampleTimer);
        }
    }

    @Retry(name = "retry_device_is_empty", fallbackMethod = "verifyIfDeviceIsEmptyRetry")
    @CircuitBreaker(name = "circuitbreaker_device_is_empty", fallbackMethod = "verifyIfDeviceIsEmptyCircuitBreaker")
    public Device verifyIfDeviceIsEmpty(String deviceModel) {

        Optional<Device> entity = this.deviceRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            log.error("Dispositivo não cadastrado, dispositivo: {}", deviceModel);
            throw new DeviceIsEmpty("This device model is not registered in the database");
        }

        return entity.get();
    }

    public Device verifyIfDeviceIsEmptyRetry(String deviceModel, Exception ex) {
        log.error("O serviço do banco de dados está fora do ar, com isso o retry retornará um throw: ", ex);
        throw new ServiceUnavailable("Database service unavailable");
    }

    public Device verifyIfDeviceIsEmptyCircuitBreaker(String deviceModel, Exception ex) {
        log.error("Circuit breaker aberto - Banco de dados indisponível");
        throw new ServiceUnavailable("Database service unavailable");
    }

    @Transactional
    public DeviceDto saveUpdate(Device entity, UpdateDeviceDto dto) {

        if (dto.newName() != null ) {
            entity.setName(dto.newName());
        }

        if (dto.newDeviceModel() != null ) {
            entity.setDeviceModel(dto.newDeviceModel());
        }

        if (dto.newManufacturer() != null) {
            entity.setManufacturer(dto.newManufacturer());
        }

        if (dto.newLocation() != null) {
            entity.setLocation(dto.newLocation());
        }

        if (dto.newDescription() != null) {
            entity.setDescription(dto.newDescription());
        }
        this.deviceRepository.save(entity);

        return new DeviceDto(
                entity.getName(),
                entity.getType(),
                entity.getDescription(),
                entity.getDeviceModel(),
                entity.getManufacturer(),
                entity.getLocation()
        );
    }


    //=================================================================================================================

    // ============================================ DELETE ============================================================

    public ResponseDeviceDto deleteDevice(String deviceModel) {

        var sampleTimer = this.timer.startTimer();

        try {
            log.info("Verifico se o device existe no banco de dados");
            var entity = this.verifyIfDeviceIsEmpty(deviceModel);

            var responseDto = new ResponseDeviceDto(
                    entity.getName(),
                    entity.getType(),
                    entity.getDescription(),
                    entity.getDeviceModel(),
                    entity.getManufacturer(),
                    entity.getLocation(),
                    entity.getUnit(),
                    entity.getType().getMin(),
                    entity.getType().getMax()
            );

            this.delete(entity);
            return responseDto;

        } finally {
            this.timer.stopDeleteTimer(sampleTimer);
        }
    }

    @Transactional
    public void delete(Device entity) {
        this.deviceRepository.delete(entity);
    }

    // ================================================================================================================

    // =============================== Retorna o dispositivo com o modelo dele ========================================

    public getDeviceWithDeviceModel getDeviceWithDeviceModel(String deviceModel) {

        var sampleTimer = this.timer.startTimer();

        try {

            var entity = this.verifyIfDeviceIsEmpty(deviceModel);
            return new getDeviceWithDeviceModel(
                    entity.getName(),
                    entity.getDeviceModel(),
                    entity.getManufacturer(),
                    entity.getLocation(),
                    entity.getDescription()
            );

        } finally {
            this.timer.stopGetDeviceTimer(sampleTimer);
        }
    }
    //=================================================================================================================

    // ================================= Retorna todos os dispositivos ================================================

    @Retry(name = "retry_for_all_devices", fallbackMethod = "retry_all_devices")
    @CircuitBreaker(name = "circuitbreaker_for_get_all_devices", fallbackMethod = "circuitbreaker_all_devices")
    public List<ResponseDeviceDto> getAllDevices(int page, int size) {

        var sampleTimer = this.timer.startTimer();

        this.timer.stopGetDevicesTimer(sampleTimer);
        return this.deviceRepository.findAllDevices(PageRequest.of(page, size))
                .stream()
                .map(device -> new ResponseDeviceDto(
                        device.getName(),
                        device.getType(),
                        device.getDescription(),
                        device.getDeviceModel(),
                        device.getManufacturer(),
                        device.getLocation(),
                        device.getUnit(),
                        device.getMinLimit(),
                        device.getMaxLimit()
                ))
                .toList();
    }


    public List<ResponseDeviceDto> retry_all_devices(int page, int size, Exception ex) {
        return List.of();
    }

    public List<ResponseDeviceDto> circuitbreaker_all_devices(int page, int size, Exception ex) {
        return List.of();
    }

    // ================================================================================================================
}
