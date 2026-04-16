package br.com.analysis.consumer;

import br.com.analysis.dtos.ConsumerSensorTest;
import br.com.analysis.metrics.MetricsService;
import br.com.analysis.service.AnalysisService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumer {

    // CIRCUIT BREAKER
    private static final String CIRCUIT_BREAKER_KAFKA_CONSUMER = "circuitbreaker_kafka_consumer";
    // ===============

    private final AnalysisService analysisService;
    private final MetricsService metricsService;

    public KafkaConsumer(
            AnalysisService analysisService,
            MetricsService metricsService) {
        this.analysisService = analysisService;
        this.metricsService = metricsService;
    }

    @KafkaListener(
            topics = "${kafka.topics.sensor-test}",
            groupId = "${kafka.group-id.sensor-test}",
            containerFactory = "kafkaListenerSensorTestFactory")
    @CircuitBreaker(name = CIRCUIT_BREAKER_KAFKA_CONSUMER, fallbackMethod = "consumerSensorTestCircuitBreaker")
    public void consumerSensorTest(ConsumerSensorTest consumer, Acknowledgment ack) {

        var sampleTimer = this.metricsService.startTimer();

        try {
            this.analysisService.consumerSensorTest(consumer);

        } finally {
            this.metricsService.stopConsumerTimer(sampleTimer);
            ack.acknowledge();
        }
    }

    public void consumerSensorTestCircuitBreaker(ConsumerSensorTest consumer, Acknowledgment ack, Exception e) {
        log.warn("Circuit breaker for kafka: {}", e.getMessage());
        this.metricsService.failConsumerEvent();
    }
}
