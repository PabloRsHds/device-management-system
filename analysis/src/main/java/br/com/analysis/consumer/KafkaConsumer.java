package br.com.analysis.consumer;

import br.com.analysis.dtos.ConsumerSensorTest;
import br.com.analysis.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumer {

    private final AnalysisService analysisService;

    public KafkaConsumer(
            AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @KafkaListener(
            topics = "sensor-test-for-analysis-topic",
            groupId = "sensor-test-for-analysis-groupId",
            containerFactory = "kafkaListenerSensorTestFactory")
    public void consumerIotGateway(ConsumerSensorTest consumer, Acknowledgment ack) {

        try {
            this.analysisService.consumerIotGatewayService(consumer);

        } finally {
            ack.acknowledge();
        }
    }
}
