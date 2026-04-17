package br.com.sensor_test.consumer;

import br.com.sensor_test.dtos.ConsumerDeviceManagement;
import br.com.sensor_test.metrics.MetricsService;
import br.com.sensor_test.service.SensorService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumer {

    private static final String CIRCUIT_BREAKER_KAFKA_CONSUMER = "circuitbreaker_kafka_consumer";

    private final SensorService sensorService;
    private final MetricsService metricsService;

    public KafkaConsumer(SensorService sensorService, MetricsService metricsService ) {
        this.sensorService = sensorService;
        this.metricsService = metricsService;
    }

    @KafkaListener(
            topics = "${kafka.topics.device-management}",
            groupId = "${kafka.group-id.device-management}",
            containerFactory = "kafkaListenerSensorTestFactory")
    @CircuitBreaker(name = CIRCUIT_BREAKER_KAFKA_CONSUMER, fallbackMethod = "consumerIotGatewayCircuitBreaker")
    public void consumerIotGateway(ConsumerDeviceManagement consumer, Acknowledgment ack) {

        try {
            this.sensorService.registerSensor(consumer);

        } finally {
            ack.acknowledge();
        }
    }

    public void consumerIotGatewayCircuitBreaker(ConsumerDeviceManagement consumer, Acknowledgment ack, Exception ex) {
        log.error("Circuit breaker opened or error in consumer: {}", ex.getMessage(), ex);
        this.metricsService.metricFailedConsumer();
    }
}
