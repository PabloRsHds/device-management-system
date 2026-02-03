package br.com.sensor_test.consumer;

import br.com.sensor_test.dtos.ConsumerDeviceManagement;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.infra.exceptions.SensorIsPresentException;
import br.com.sensor_test.infra.exceptions.ServiceUnavailableException;
import br.com.sensor_test.model.Sensor;
import br.com.sensor_test.repository.SensorRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class KafkaConsumer {

    private final SensorRepository sensorRepository;

    @Autowired
    public KafkaConsumer(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    @KafkaListener(
            topics = "device-management-for-sensor-test-topic",
            groupId = "device-management-for-sensor-test-groupId",
            containerFactory = "kafkaListenerSensorTestFactory")
    @CircuitBreaker(name = "circuitbreaker-kafka", fallbackMethod = "circuitbreaker_for_kafka")
    public void consumerIotGateway(ConsumerDeviceManagement consumer, Acknowledgment ack) {

        try {
            this.verifyIfDeviceIsPresent(consumer.deviceModel());
            this.save(consumer);

        } finally {
            ack.acknowledge();
        }
    }

    @CircuitBreaker(name = "circuitbreaker-database", fallbackMethod = "circuitbreaker_for_database")
    public void verifyIfDeviceIsPresent(String deviceModel) {

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

    public void circuitbreaker_for_kafka(ConsumerDeviceManagement consumer, Acknowledgment ack, Exception ex) {
        log.error("Circuit breaker opened or error in consumer: {}", ex.getMessage(), ex);

        throw new ServiceUnavailableException("Service unavailable, message will be retried");
    }
}
