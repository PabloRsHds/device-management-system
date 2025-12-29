package br.com.iot_gateway.consumer;

import br.com.iot_gateway.dtos.DeviceDto;
import br.com.iot_gateway.dtos.EventIotGateway;
import br.com.iot_gateway.microservice.DeviceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final DeviceClient deviceClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaConsumer(DeviceClient deviceClient, KafkaTemplate<String, Object> kafkaTemplate) {
        this.deviceClient = deviceClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(
            topics = "sensor-test-topic",
            groupId = "sensor-test-groupId",
            containerFactory = "kafkaListenerSensorTestFactory")
    private void consumerSensorTestEvent(DeviceDto event, Acknowledgment ack) {

        log.info("Verificando se as requisições brutas estão de acordo");
        var device = this.deviceClient.verificationForIotGateway(
                event.deviceId(),
                event.name(),
                event.type(),
                event.description(),
                event.deviceModel(),
                event.manufacturer());


        if (device == false) {
            log.warn("As informações não bateram");
            ack.acknowledge();
            return;
        }

        log.info("As informações bateram, agora envio para a análise");
        this.kafkaTemplate.send("iot-gateway-topic", new DeviceDto(
                event.deviceId(),
                event.name(),
                event.type(),
                event.description(),
                event.deviceModel(),
                event.manufacturer(),
                event.location(),
                event.unit(),
                event.minLimit(),
                event.maxLimit()
        ));
    }
}
