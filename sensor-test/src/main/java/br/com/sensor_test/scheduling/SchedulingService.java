package br.com.sensor_test.scheduling;

import br.com.sensor_test.dtos.SensorForAnalysisEvent;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.infra.exceptions.ServiceUnavailableException;
import br.com.sensor_test.metrics.MetricsService;
import br.com.sensor_test.repository.SensorRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class SchedulingService {

    // Circuit breakers
    private static final String CIRCUIT_BREAKER_SCHEDULING = "circuitbreaker_scheduling";
    private static final String CIRCUIT_BREAKER_KAFKA_PRODUCER = "circuitbreaker_kafka_producer";
    // ================

    private final SensorRepository sensorRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MetricsService metricsService;

    private static final Random random = new Random();
    private static final float TEST_MARGIN = 100f;

    @Autowired
    public SchedulingService(
            SensorRepository sensorRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            MetricsService metricsService) {
        this.sensorRepository = sensorRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.metricsService = metricsService;
    }


    @Scheduled(fixedDelay = 2 * 60 * 1000)
    public void sensorTestService() {
        this.processActiveSensorsForAnalysis();
    }

    @CircuitBreaker(name = CIRCUIT_BREAKER_SCHEDULING, fallbackMethod = "processActiveSensorsForAnalysisCircuitBreaker")
    public void processActiveSensorsForAnalysis() {
        this.sensorRepository
                .findAll()
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

    public void processActiveSensorsForAnalysisCircuitBreaker(Exception ex) {
        log.warn("Database service is not available, error:", ex);
        this.metricsService.metricForScheduling();
    }

    // KAFKA PRODUCER E CIRCUIT BREAKER
    @CircuitBreaker(name = CIRCUIT_BREAKER_KAFKA_PRODUCER, fallbackMethod = "sendEventCircuitBreaker")
    public void sendEvent(String topic, SensorForAnalysisEvent event) {
        kafkaTemplate.send(
                topic,
                event
        );
    }

    public void sendEventCircuitBreaker(String topic, SensorForAnalysisEvent event, Exception ex) {

        log.error("Circuit breaker opened or error in consumer: {}", ex.getMessage(), ex);
        throw new ServiceUnavailableException("Service unavailable, message will be retried");
    }
    // =============================================================================================================

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
