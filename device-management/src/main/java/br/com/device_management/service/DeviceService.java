package br.com.device_management.service;

import br.com.device_management.dtos.DeviceManagementEventForSensor;
import br.com.device_management.dtos.ResponseDeviceDto;
import br.com.device_management.dtos.UpdateDeviceDto;
import br.com.device_management.dtos.DeviceDetailsDto;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
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

    // Caches
    private static final String CACHE_ALL_DEVICES = "cache_all_devices";
    private static final String CACHE_GET_DEVICE = "cache_get_device";
    // =============================

    // Circuit breaker "circuitbreaker_kafka_send_event"
    private static final String CIRCUIT_BREAKER_DEVICE_PRESENT = "circuitbreaker_device_present";
    private static final String CIRCUIT_BREAKER_GET_DEVICE = "circuitbreaker_get_device";
    private static final String CIRCUIT_BREAKER_ALL_DEVICES = "circuitbreaker_all_devices";
    private static final String CIRCUIT_BREAKER_KAFKA_SEND = "circuitbreaker_kafka_send";
    // =============================

    // Retry
    private static final String RETRY_DEVICE_PRESENT = "retry_device_present";
    private static final String RETRY_GET_DEVICE = "retry_get_device";
    private static final String RETRY_ALL_DEVICES = "retry_all_devices";
    // =============================

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

    @Retry(name = RETRY_DEVICE_PRESENT, fallbackMethod = "verifyIfDeviceIsPresentRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_DEVICE_PRESENT, fallbackMethod = "verifyIfDeviceIsPresentCircuitBreaker")
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

    @CircuitBreaker(name = CIRCUIT_BREAKER_KAFKA_SEND, fallbackMethod = "sendEventCircuitBreaker")
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
            var entity = this.getDeviceOrThrow(deviceModel);

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

    @Cacheable(value = CACHE_GET_DEVICE, key = "#deviceModel")
    @Retry(name = RETRY_GET_DEVICE, fallbackMethod = "getDeviceOrThrowRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_GET_DEVICE, fallbackMethod = "getDeviceOrThrowCircuitBreaker")
    public Device getDeviceOrThrow(String deviceModel) {

        Optional<Device> entity = this.deviceRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            log.error("Dispositivo não cadastrado, dispositivo: {}", deviceModel);
            throw new DeviceIsEmpty("This device model is not registered in the database");
        }

        return entity.get();
    }

    public Device getDeviceOrThrowRetry(String deviceModel, Exception ex) {
        log.error("O serviço do banco de dados está fora do ar, com isso o retry retornará um throw: ", ex);
        throw new ServiceUnavailable("Database service unavailable");
    }

    public Device getDeviceOrThrowCircuitBreaker(String deviceModel, Exception ex) {
        log.error("Circuit breaker aberto - Banco de dados indisponível");
        throw new ServiceUnavailable("Database service unavailable");
    }


    @Caching(evict = {
            @CacheEvict(value = CACHE_GET_DEVICE, key = "#entity.deviceModel"),
            @CacheEvict(value = CACHE_ALL_DEVICES, allEntries = true)
    })
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

        log.info("Atualização de dispositivo, feita com sucesso");
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

        log.info("iniciando o timer");
        var sampleTimer = this.timer.startTimer();

        try {
            log.info("Verifico se o device existe no banco de dados");
            var entity = this.getDeviceOrThrow(deviceModel);

            log.info("Usuário achado no banco de dados, salvando um responseDto com os dados do dispositivo");
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
            log.info("parando o timer");
            this.timer.stopDeleteTimer(sampleTimer);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_GET_DEVICE, key = "#entity.deviceModel"),
            @CacheEvict(value = CACHE_ALL_DEVICES, allEntries = true)
    })
    @Transactional
    public void delete(Device entity) {
        try {
            log.info("Dispositivo apagado com sucesso");
            this.deviceRepository.delete(entity);

        } catch (DataAccessException ex) {
            log.info("Database serviço indisponível");
            throw new ServiceUnavailable("Database is down");
        }
    }

    // ================================================================================================================

    // =============================== Retorna o dispositivo com o modelo dele ========================================

    public DeviceDetailsDto getDeviceWithDeviceModel(String deviceModel) {

        var sampleTimer = this.timer.startTimer();

        try {

            var entity = this.getDeviceOrThrow(deviceModel);
            return new DeviceDetailsDto(
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

    @Cacheable(value = CACHE_ALL_DEVICES, key = "#page + '-' + #size", unless = "#result.isEmpty()")
    @Retry(name = RETRY_ALL_DEVICES, fallbackMethod = "getAllDevicesRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_ALL_DEVICES, fallbackMethod = "getAllDevicesCircuitBreaker")
    public Page<ResponseDeviceDto> getAllDevices(int page, int size) {

        var sampleTimer = this.timer.startTimer();

        try {

            return this.deviceRepository.findAll(PageRequest.of(page, size))
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
                    ));

        } finally {
            this.timer.stopGetDevicesTimer(sampleTimer);
        }
    }

    public List<ResponseDeviceDto> getAllDevicesRetry(int page, int size, Exception ex) {
        log.error("O serviço do banco de dados está fora do ar, com isso o retry retornará um throw: {}", ex.getMessage());
        return List.of();
    }

    public List<ResponseDeviceDto> getAllDevicesCircuitBreaker(int page, int size, Exception ex) {
        log.error("Circuit breaker aberto - Banco de dados indisponível para retornar todos os dispositivos.");
        return List.of();
    }

    // ================================================================================================================
}
