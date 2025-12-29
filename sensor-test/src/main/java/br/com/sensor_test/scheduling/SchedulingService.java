package br.com.sensor_test.scheduling;

import br.com.sensor_test.dtos.DeviceDto;
import br.com.sensor_test.enums.Type;
import br.com.sensor_test.enums.Unit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class SchedulingService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public SchedulingService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }


    @Transactional
    @Scheduled(cron = "* */5 * * * *")
    private void SensorTestService() {

        var random = new Random();

        var minLimit = random.nextFloat(-20f, 30);
        var maxLimit = random.nextFloat(30, 70);

        this.kafkaTemplate.send("sensor-test-topic", new DeviceDto(
                "a63cebbf-4e2d-4f99-ae8c-88fcc8bb8e76",
                "Sensor de Temperatura",
                Type.TEMPERATURE_SENSOR,
                "Sensor de temperatura mODD532",
                "mODD532",
                "Imbra",
                "Quarto de visitas",
                Unit.CELSIUS,
                minLimit,
                maxLimit
        ));

    }
}
