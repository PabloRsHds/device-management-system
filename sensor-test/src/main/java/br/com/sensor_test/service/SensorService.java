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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SensorService {

    private final SensorRepository sensorRepository;
    private final MetricsService metricsTimer;

    @Autowired
    public SensorService(
            SensorRepository sensorRepository,
            MetricsService metricsTimer) {
        this.sensorRepository = sensorRepository;
        this.metricsTimer = metricsTimer;
    }

    // ========================================== REGISTER ===========================================================

    public void registerSensor(ConsumerDeviceManagement consumer) {

        var sampleTimer = this.metricsTimer.startTimer();

        try {
            this.verifyIfSensorIsEmpty(consumer.deviceModel());
            this.save(consumer);

        } finally {
            this.metricsTimer.stopConsumerTimer(sampleTimer);
        }
    }

    @CircuitBreaker(name = "circuitbreaker-database", fallbackMethod = "circuitbreaker_for_database")
    public void verifyIfSensorIsEmpty(String deviceModel) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(deviceModel);

        if (entity.isPresent()) {
            throw new SensorIsPresentException("This device already cadastred in database");
        }
    }

    @Transactional
    public void save(ConsumerDeviceManagement consumer) {

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

    public void circuitbreaker_for_database(String deviceModel, Exception ex) {
        log.warn("Database service is not available, error:", ex);
        throw new ServiceUnavailableException("Database service is not available");
    }

    // =============================================  UPDATE =========================================================

    public ResponseSensorDto updateSensor(String deviceModel, UpdateSensor request) {

        var sampleTimer = this.metricsTimer.startTimer();

        try {
            var entity = verifyIfSensorIsPresent(deviceModel);
            return this.update(entity, request);

        } finally {
            this.metricsTimer.stopUpdateTimer(sampleTimer);
        }
    }

    // Metodo para verificar se o sensor é presente, se não ele retorna um erro.
    @CircuitBreaker(name = "circuitbreaker-database", fallbackMethod = "circuitbreaker_for_database")
    public Sensor verifyIfSensorIsPresent(String deviceModel) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            throw new SensorIsEmptyException("Sensor not found");
        }

        return entity.get();
    }

    @Transactional
    public ResponseSensorDto update(Sensor entity, UpdateSensor request) {

        if (!request.name().isBlank()) {
            entity.setName(request.name());
        }
        if (!request.deviceModel().isBlank()) {
            entity.setDeviceModel(request.deviceModel());
        }
        if (!request.manufacturer().isBlank()) {
            entity.setManufacturer(request.manufacturer());
        }

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

        var sampleTimer = this.metricsTimer.startTimer();

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
            this.metricsTimer.stopDeleteTimer(sampleTimer);
        }
    }

    @Transactional
    public void delete(Sensor entity) {
        this.sensorRepository.delete(entity);
    }

    // ===============================================================================================================


    // ====================================== PEGA TODOS OS SENSORES =================================================

    @CircuitBreaker(name = "circuitbreaker-all-database", fallbackMethod = "circuitbreaker_for_all_database")
    public List<ResponseSensorDto> findAllSensorsActivated(int page, int size) {

        var sampleTimer = this.metricsTimer.startTimer();

        try {
            return this.sensorRepository
                    .findAllSensors(PageRequest.of(page, size, Sort.Direction.DESC))
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
            this.metricsTimer.stopSensorsTimer(sampleTimer);
        }

    }

    public List<ResponseSensorDto> circuitbreaker_for_all_database(int page, int size, Exception ex) {
        log.warn("Database service is not available, error:", ex);
        throw new ServiceUnavailableException("Database service is not available");
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

        entity.setStatus(Status.ACTIVATED);
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

    // ============================================ PEGO O STATUS ====================================================
    public String getStatus(String deviceModel) {

        var entity = this.verifyIfSensorIsPresent(deviceModel);

        return entity.getStatus().toString();
    }
}
