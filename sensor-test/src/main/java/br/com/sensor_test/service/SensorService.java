package br.com.sensor_test.service;

import br.com.sensor_test.dtos.ConsumerDeviceManagement;
import br.com.sensor_test.dtos.UpdateSensor;
import br.com.sensor_test.dtos.sensor.ResponseSensorDto;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.infra.exceptions.SensorIsEmptyException;
import br.com.sensor_test.infra.exceptions.SensorIsPresentException;
import br.com.sensor_test.infra.exceptions.ServiceUnavailableException;
import br.com.sensor_test.metrics.MetricsService;
import br.com.sensor_test.model.Sensor;
import br.com.sensor_test.repository.SensorRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SensorService {

    private final SensorRepository sensorRepository;
    private final MetricsService metricsService;

    @Autowired
    public SensorService(
            SensorRepository sensorRepository,
            MetricsService metricsService) {
        this.sensorRepository = sensorRepository;
        this.metricsService = metricsService;
    }

    // ========================================== REGISTER ===========================================================

    public void registerSensor(ConsumerDeviceManagement consumer) {

        log.info("Iniciando o timer do consumer");
        var sampleTimer = this.metricsService.startTimer();

        try {
            this.verifyIfSensorIsEmpty(consumer.deviceModel());
            this.save(consumer);

        } finally {
            log.info("Parando o timer do consumer");
            this.metricsService.stopConsumerTimer(sampleTimer);
        }
    }

    @Retry(name = "retry_sensor_is_empty", fallbackMethod = "verifyIfSensorIsEmptyRetry")
    @CircuitBreaker(name = "circuitbreaker_sensor_is_empty", fallbackMethod = "verifyIfSensorIsEmptyCircuitBreaker")
    public void verifyIfSensorIsEmpty(String deviceModel) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(deviceModel);

        if (entity.isPresent()) {
            log.info("Sensor já cadastrado. STATUS: NEGATIVO");
            throw new SensorIsPresentException("This device already cadastred in database");
        }
        log.info("Sensor não existe no banco de dados. STATUS: OK");
    }

    public void verifyIfSensorIsEmptyRetry(String deviceModel, Exception ex) {
        log.warn("Serviço de banco de dados indisponível, com isso não está sendo possível verificar o dispositivo");
    }

    public void verifyIfSensorIsEmptyCircuitBreaker(String deviceModel, Exception ex) {
        log.warn("Serviço de banco de dados indisponível, error:", ex);
        throw new ServiceUnavailableException("Database service is not available");
    }

    @Transactional
    public void save(ConsumerDeviceManagement consumer) {

        log.info("Salvando novo sensor no banco de dados");
        var newEntity = new Sensor();

        newEntity.setName(consumer.name());
        newEntity.setType(consumer.type());
        newEntity.setDescription(consumer.description());
        newEntity.setDeviceModel(consumer.deviceModel());
        newEntity.setManufacturer(consumer.manufacturer());
        newEntity.setUnit(consumer.unit());
        newEntity.setMinLimit(consumer.minLimit());
        newEntity.setMaxLimit(consumer.maxLimit());
        newEntity.setStatus(Status.DEACTIVATED);
        this.sensorRepository.save(newEntity);
    }

    // =============================================  UPDATE =========================================================

    public ResponseSensorDto updateSensor(String deviceModel, UpdateSensor request) {

        var sampleTimer = this.metricsService.startTimer();

        try {
            var entity = verifyIfSensorIsPresent(deviceModel);
            return this.update(entity, request);

        } finally {
            this.metricsService.stopUpdateTimer(sampleTimer);
        }
    }

    // Metodo para verificar se o sensor é presente, se não ele retorna um erro.
    @Retry(name = "retry_sensor_is_present", fallbackMethod = "verifyIfSensorIsPresentRetry")
    @CircuitBreaker(name = "circuitbreaker-sensor_is_present", fallbackMethod = "verifyIfSensorIsPresentCircuitBreaker")
    public Sensor verifyIfSensorIsPresent(String deviceModel) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            log.info("Sensor não presente no banco de dados");
            throw new SensorIsEmptyException("Sensor not found");
        }

        log.info("Sensor presente no banco de dados");
        return entity.get();
    }

    public Sensor verifyIfSensorIsPresentRetry(String deviceModel, Exception ex) {
        log.warn("Serviço de banco de dados indisponível, não foi possível fazer a verificação do dispositivo: {}"
                ,deviceModel);
        throw new ServiceUnavailableException("Database service is not available");
    }

    public Sensor verifyIfSensorIsPresentCircuitBreaker(String deviceModel, Exception ex) {
        log.warn("Circuit breaker aberto - Banco de dados está fora do ar");
        throw new ServiceUnavailableException("Database service is not available");
    }

    @Transactional
    public ResponseSensorDto update(Sensor entity, UpdateSensor request) {

        if (!request.name().isBlank()) {
            log.info("alterando o nome");
            entity.setName(request.name());
        }
        if (!request.deviceModel().isBlank()) {
            log.info("alterando o deviceModel");
            entity.setDeviceModel(request.deviceModel());
        }
        if (!request.manufacturer().isBlank()) {
            log.info("alterando o manufacturer");
            entity.setManufacturer(request.manufacturer());
        }

        log.info("Salvando alterações");
        this.sensorRepository.save(entity);

        return new ResponseSensorDto(
                entity.getName(),
                entity.getType(),
                entity.getDeviceModel(),
                entity.getManufacturer(),
                entity.getStatus()
        );
    }

    // ===============================================================================================================

    // ========================================== DELETE =============================================================

    public ResponseSensorDto deleteSensor(String deviceModel) {

        var sampleTimer = this.metricsService.startTimer();

        try {
            var entity = this.verifyIfSensorIsPresent(deviceModel);
            var response = new ResponseSensorDto(
                    entity.getName(),
                    entity.getType(),
                    entity.getDeviceModel(),
                    entity.getManufacturer(),
                    entity.getStatus()
            );
            this.delete(entity);
            return response;

        } finally {
            this.metricsService.stopDeleteTimer(sampleTimer);
        }
    }

    @Transactional
    public void delete(Sensor entity) {

        log.info("Sensor deletado com sucesso");
        this.sensorRepository.delete(entity);
    }

    // ===============================================================================================================


    // ====================================== PEGA TODOS OS SENSORES =================================================

    @Retry(name = "retry_get_all_sensors", fallbackMethod = "getAllSensorsActivatedRetry")
    @CircuitBreaker(name = "circuitbreaker_get_all_sensors", fallbackMethod = "getAllSensorsActivatedCircuitBreaker")
    public List<ResponseSensorDto> getAllSensorsActivated(int page, int size) {

        log.info("Iniciando o timer do get all sensors");
        var sampleTimer = this.metricsService.startTimer();

        try {
            log.info("Procurando registros no banco de dados, e transformando eles em um dto");
            return this.sensorRepository
                    .findAllSensors(PageRequest.of(page, size))
                    .stream()
                    .filter(device -> Status.ACTIVATED.equals(device.getStatus()))
                    .map(device -> new ResponseSensorDto(
                            device.getName(),
                            device.getType(),
                            device.getDeviceModel(),
                            device.getManufacturer(),
                            device.getStatus()
                    ))
                    .toList();

        } finally {
            log.info("Parando o timer do get all sensors");
            this.metricsService.stopSensorsTimer(sampleTimer);
        }
    }

    public List<ResponseSensorDto> getAllSensorsActivatedRetry(int page, int size, Exception ex) {
        return List.of();
    }

    public List<ResponseSensorDto> getAllSensorsActivatedCircuitBreaker(int page, int size, Exception ex) {
        return List.of();
    }

    // ===============================================================================================================

    // ===================================== ALTERA O STATUS DO SENSOR ===============================================

    public ResponseSensorDto changeStatus(String deviceModel) {

        var entity = this.verifyIfSensorIsPresent(deviceModel);
        return this.change(entity);
    }

    @Transactional
    public ResponseSensorDto change(Sensor entity) {

        if (entity.getStatus().equals(Status.ACTIVATED)) {

            log.info("Sensor foi desativado");
            entity.setStatus(Status.DEACTIVATED);
            this.sensorRepository.save(entity);

            return new ResponseSensorDto(
                        entity.getName(),
                        entity.getType(),
                        entity.getDeviceModel(),
                        entity.getManufacturer(),
                        entity.getStatus()
                );
        }

        log.info("Sensor foi ativado");
        entity.setStatus(Status.ACTIVATED);
        this.sensorRepository.save(entity);

        log.info("Retornando o dto ResponseSensorDto, com os dados atualizados");
        return new ResponseSensorDto(
                        entity.getName(),
                        entity.getType(),
                        entity.getDeviceModel(),
                        entity.getManufacturer(),
                        entity.getStatus()
                );
    }
    // ===============================================================================================================

    // ============================================ PEGO O STATUS ====================================================
    public String getStatus(String deviceModel) {

        var entity = this.verifyIfSensorIsPresent(deviceModel);

        log.info("Retornando o status do sensor");
        return entity.getStatus().toString();
    }
}
