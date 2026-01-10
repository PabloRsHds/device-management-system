package br.com.sensor_test.scheduling;

import br.com.sensor_test.dtos.SensorForAnalysisEvent;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

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


    @Transactional
    @Scheduled(fixedDelay = 2 * 60 * 1000)
    public void sensorTestService() {

        sensorRepository.findAll()
                .stream()
                .filter(device -> device.getStatus() == Status.ACTIVATED)
                .forEach(device -> {

                    var minLimit = device.getMinLimit();
                    var maxLimit = device.getMaxLimit();

                    var valueMin = this.randomMinLimit(minLimit);
                    var valueMax = this.randomMaxLimit(maxLimit);


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
                                    maxLimit,
                                    valueMin,
                                    valueMax
                            )
                    );
                });
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
