package br.com.sensor_test.consumer;

import br.com.sensor_test.dtos.ConsumerDeviceManagement;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.model.Sensor;
import br.com.sensor_test.repository.SensorRepository;
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

    @Transactional
    @KafkaListener(
            topics = "device-management-for-sensor-test-topic",
            groupId = "device-management-for-sensor-test-groupId",
            containerFactory = "kafkaListenerSensorTestFactory")
    public void consumerIotGateway(ConsumerDeviceManagement consumer, Acknowledgment ack) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(consumer.deviceModel());

        if (entity.isEmpty()) {

            var newEntity = new Sensor();

            newEntity.setName(consumer.name());
            newEntity.setType(consumer.type());
            newEntity.setDescription(consumer.description());
            newEntity.setDeviceModel(consumer.deviceModel());
            newEntity.setManufacturer(consumer.manufacturer());
            newEntity.setUnit(consumer.unit());
            newEntity.setStatus(Status.DEACTIVATED);
            this.sensorRepository.save(newEntity);

            ack.acknowledge();
        }
        ack.acknowledge();
    }
}
