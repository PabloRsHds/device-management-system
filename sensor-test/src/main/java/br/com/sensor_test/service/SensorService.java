package br.com.sensor_test.service;

import br.com.sensor_test.dtos.AllSensorsDto;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.model.Sensor;
import br.com.sensor_test.repository.SensorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SensorService {

    private final SensorRepository sensorRepository;

    @Autowired
    public SensorService(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    // Pega todos os sensores cadastrados com o status ativo
    public ResponseEntity<List<AllSensorsDto>> findAllSensorsActivated() {

        List<AllSensorsDto> sensors = this.sensorRepository
                .findAll()
                .stream()
                .filter(device -> Status.ACTIVATED.equals(device.getStatus()))
                .map(device -> new AllSensorsDto(
                        device.getName(),
                        device.getType(),
                        device.getDeviceModel(),
                        device.getManufacturer(),
                        device.getStatus()
                ))
                .toList();

        return ResponseEntity.ok(sensors);
    }

    // Altera o status do sensor
    public void changeStatus(String deviceModel) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(deviceModel);

        if (entity.get().getStatus().equals(Status.ACTIVATED)) {
            entity.get().setStatus(Status.DEACTIVATED);
            this.sensorRepository.save(entity.get());

        } else {
            entity.get().setStatus(Status.ACTIVATED);
            this.sensorRepository.save(entity.get());
        }
    }

    public ResponseEntity<String> getStatus(String deviceModel) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(deviceModel);

        return entity.map(sensor -> ResponseEntity.ok(sensor.getStatus().toString())).orElseThrow();
    }
}
