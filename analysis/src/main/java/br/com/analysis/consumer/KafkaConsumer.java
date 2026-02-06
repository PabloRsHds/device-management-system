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

    private final AnalysisService analysisService;
    private final MetricsService metricsService;

    public KafkaConsumer(
            AnalysisService analysisService,
            MetricsService metricsService) {
        this.analysisService = analysisService;
        this.metricsService = metricsService;
    }

    @KafkaListener(
            topics = "sensor-test-for-analysis-topic",
            groupId = "sensor-test-for-analysis-groupId",
            containerFactory = "kafkaListenerSensorTestFactory")
    @CircuitBreaker(name = "circuitbreaker_kafka_consumer", fallbackMethod = "circuitbreaker_for_kafka_consumer")
    public void consumerIotGateway(ConsumerSensorTest consumer, Acknowledgment ack) {

        var sampleTimer = this.metricsService.startTimer();

        try {
            this.analysisService.consumerIotGatewayService(consumer);

        } finally {
            this.metricsService.stopConsumerTimer(sampleTimer);
            ack.acknowledge();
        }
    }

    public void circuitbreaker_for_kafka_consumer(ConsumerSensorTest consumer, Acknowledgment ack, Exception e) {
        log.warn("Circuit breaker for kafka: {}", e.getMessage());
        this.metricsService.failConsumerEvent();
    }
}
