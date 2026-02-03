package br.com.sensor_test.scheduling;

import br.com.sensor_test.dtos.SensorForAnalysisEvent;
import br.com.sensor_test.dtos.sensor.ResponseSensorDto;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.infra.exceptions.ServiceUnavailableException;
import br.com.sensor_test.repository.SensorRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class SchedulingService {

    private final SensorRepository sensorRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final Random random = new Random();
    private static final float TEST_MARGIN = 100f;

    @Autowired
    public SchedulingService(SensorRepository sensorRepository,KafkaTemplate<String, Object> kafkaTemplate) {
        this.sensorRepository = sensorRepository;
        this.kafkaTemplate = kafkaTemplate;
    }


    @Scheduled(fixedDelay = 2 * 60 * 1000)
    @CircuitBreaker(name = "circuitbreaker-all-database", fallbackMethod = "circuitbreaker_for_all_database")
    public void sensorTestService() {

        sensorRepository.findAll()
                .stream()
                .filter(device -> device.getStatus() == Status.ACTIVATED)
                .forEach(device -> {

                    var minLimit = device.getMinLimit();
                    var maxLimit = device.getMaxLimit();

                    var valueMin = this.randomMinLimit(minLimit);
                    var valueMax = this.randomMaxLimit(maxLimit);


                    this.sendEvent(
                            "sensor-test-for-analysis-topic",
                                    new SensorForAnalysisEvent(
                                    device.getName(),
                                    device.getType(),
                                    device.getDescription(),
                                    device.getDeviceModel(),
                                    device.getManufacturer(),
                                    device.getUnit(),
                                    minLimit,
                                    maxLimit,
                                    valueMin,
                                    valueMax
                            ));
                });
    }

    public void circuitbreaker_for_all_database(Exception ex) {
        log.warn("Database service is not available, error:", ex);
    }

    @CircuitBreaker(name = "circuitbreaker-kafka-producer", fallbackMethod = "circuitbreaker_kafka_producer")
    public void sendEvent(String topic, SensorForAnalysisEvent event) {
        kafkaTemplate.send(
                topic,
                event
        );
    }

    public void circuitbreaker_kafka_producer(String topic, SensorForAnalysisEvent event, Exception ex) {

        log.error("Circuit breaker opened or error in consumer: {}", ex.getMessage(), ex);
        throw new ServiceUnavailableException("Service unavailable, message will be retried");
    }

    public float randomMinLimit(float minLimit) {

        float lower = minLimit - TEST_MARGIN;
        float upper = minLimit + TEST_MARGIN;

        return random.nextFloat(lower, upper);
    }

    public float randomMaxLimit(float maxLimit) {

        float lower = maxLimit - TEST_MARGIN;
        float upper = maxLimit + TEST_MARGIN;

        return random.nextFloat(lower, upper);
    }
}
