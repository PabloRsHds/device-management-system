package br.com.sensor_test.consumer;

import br.com.sensor_test.dtos.ConsumerDeviceManagement;
import br.com.sensor_test.infra.exceptions.ServiceUnavailableException;
import br.com.sensor_test.service.SensorService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumer {

    private final SensorService sensorService;

    public KafkaConsumer(SensorService sensorService ) {
        this.sensorService = sensorService;
    }

    @KafkaListener(
            topics = "device-management-for-sensor-test-topic",
            groupId = "device-management-for-sensor-test-groupId",
            containerFactory = "kafkaListenerSensorTestFactory")
    @CircuitBreaker(name = "circuitbreaker-kafka-consumer", fallbackMethod = "circuitbreaker_kafka_consumer")
    public void consumerIotGateway(ConsumerDeviceManagement consumer, Acknowledgment ack) {

        try {
            this.sensorService.registerSensor(consumer);

        } finally {
            ack.acknowledge();
        }
    }

    public void circuitbreaker_kafka_consumer(ConsumerDeviceManagement consumer, Acknowledgment ack, Exception ex) {
        log.error("Circuit breaker opened or error in consumer: {}", ex.getMessage(), ex);

        throw new ServiceUnavailableException("Service unavailable, message will be retried");
    }
}
