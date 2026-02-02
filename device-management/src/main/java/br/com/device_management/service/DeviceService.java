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
import java.awt.print.Pageable;
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

            log.info("Novo dispositivo salvo no banco de dados");
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

    @Retry(name = "retry-database", fallbackMethod = "retry_for_database")
    @CircuitBreaker(name = "circuitbreaker-database", fallbackMethod = "circuitbreaker_for_database")
    public void verifyIfDeviceIsPresent(String deviceModel) {

        Optional<Device> device = this.deviceRepository.findByDeviceModel(deviceModel);

        if (device.isPresent()) {
            throw new DeviceIsPresent("This device model is already registered in the database");
        }
    }

    public void retry_for_database(String deviceModel, Exception e) {
        log.error("Erro ao verificar se o dispositivo ja esta cadastrado", e);
        throw new ServiceUnavailable("Database service unavailable");
    }

    public void circuitbreaker_for_database(String deviceModel, Exception e) {
        log.error("Erro ao verificar se o dispositivo ja esta cadastrado", e);
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

    @CircuitBreaker(name = "circuitbreaker-kafka", fallbackMethod = "circuitbreaker_for_kafka")
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

    public void circuitbreaker_for_kafka(String topic, DeviceDto dto, Exception e) {
        log.error("Kafka service unavailable, error: ", e);
    }

    // ================================================================================================================

    // =========================================== UPDATE =============================================================

    public ResponseDeviceDto updateDevice(String deviceModel,UpdateDeviceDto request) {

        var sampleTimer = this.timer.startTimer();

        try {
            log.info("Verificando se o dispositivo não está cadastrado");
            var entity = this.verifyIfDeviceIsEmpty(deviceModel);

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

    @Retry(name = "retry-database", fallbackMethod = "retry_for_database")
    @CircuitBreaker(name = "circuitbreaker-database", fallbackMethod = "circuitbreaker_for_database")
    public Device verifyIfDeviceIsEmpty(String deviceModel) {

        Optional<Device> entity = this.deviceRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            throw new DeviceIsEmpty("This device model is not registered in the database");
        }

        return entity.get();
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

    @Retry(name = "retry-database", fallbackMethod = "retry_for_database")
    @CircuitBreaker(name = "circuitbreaker-database", fallbackMethod = "circuitbreaker_for_database")
    public getDeviceWithDeviceModel getDeviceWithDeviceModel(String deviceModel) {

        var sampleTimer = this.timer.startTimer();

        try {
            var device = this.verifyIfDeviceIsEmpty(deviceModel);

            return new getDeviceWithDeviceModel(
                    device.getName(),
                    device.getDeviceModel(),
                    device.getManufacturer(),
                    device.getLocation(),
                    device.getDescription()
            );

        } finally {
            this.timer.stopGetDeviceTimer(sampleTimer);
        }
    }
    //=================================================================================================================

    // ================================= Retorna todos os dispositivos ================================================

    @Retry(name = "retry-database", fallbackMethod = "retry_for_database")
    @CircuitBreaker(name = "circuitbreaker-database", fallbackMethod = "circuitbreaker_for_database")
    public List<ResponseDeviceDto> getAllDevices(int page, int size) {

        var sampleTimer = this.timer.startTimer();

        try {
            return this.deviceRepository.findAllDevices((Pageable) PageRequest.of(page, size))
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

        } finally {
            this.timer.stopGetDevicesTimer(sampleTimer);
        }
    }

    // ================================================================================================================
}
