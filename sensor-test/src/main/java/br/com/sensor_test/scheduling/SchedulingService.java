package br.com.sensor_test.scheduling;

import br.com.sensor_test.dtos.SensorForAnalysisEvent;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedulingService {

    private final SensorRepository sensorRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public SchedulingService(SensorRepository sensorRepository,KafkaTemplate<String, Object> kafkaTemplate) {
        this.sensorRepository = sensorRepository;
        this.kafkaTemplate = kafkaTemplate;
    }


    @Transactional
    @Scheduled(cron = "* */5 * * * *")
    public void sensorTestService() {

        sensorRepository.findAll()
                .stream()
                .filter(device -> device.getStatus() == Status.ACTIVATED)
                .forEach(device -> {

                    var minLimit = device.getType()
                                    .randomMinLimit(device.getType().getMin(), device.getType().getMax());

                    var maxLimit = device.getType()
                                    .randomMaxLimit(device.getType().getMin(), device.getType().getMax());

                    kafkaTemplate.send(
                            "sensor-test-for-analysis-topic",
                            new SensorForAnalysisEvent(
                                    device.getName(),
                                    device.getType(),
                                    device.getDescription(),
                                    device.getDeviceModel(),
                                    device.getManufacturer(),
                                    device.getUnit(),
                                    minLimit,
                                    maxLimit
                            )
                    );
                });
    }
}
