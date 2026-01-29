package br.com.sensor_test.service;

import br.com.sensor_test.dtos.AllSensorsDto;
import br.com.sensor_test.dtos.UpdateSensor;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.model.Sensor;
import br.com.sensor_test.repository.SensorRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public ResponseEntity<?> updateSensor(String deviceModel, UpdateSensor request) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(deviceModel);

        if (!request.name().isBlank()) {
            entity.get().setName(request.name());
        }
        if (!request.deviceModel().isBlank()) {
            entity.get().setDeviceModel(request.deviceModel());
        }
        if (!request.manufacturer().isBlank()) {
            entity.get().setManufacturer(request.manufacturer());
        }

        this.sensorRepository.save(entity.get());

        return ResponseEntity.ok(new AllSensorsDto(
                entity.get().getName(),
                entity.get().getType(),
                entity.get().getDeviceModel(),
                entity.get().getManufacturer(),
                entity.get().getStatus()
        ));
    }

    public ResponseEntity<?> deleteSensor(String deviceModel) {

        Optional<Sensor> entity = sensorRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Sensor sensor = entity.get();
        sensorRepository.delete(sensor);

        return ResponseEntity.ok(new AllSensorsDto(
                sensor.getName(),
                sensor.getType(),
                sensor.getDeviceModel(),
                sensor.getManufacturer(),
                sensor.getStatus()
        ));
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
    public ResponseEntity<?> changeStatus(String deviceModel) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(deviceModel);

        if (entity.get().getStatus().equals(Status.ACTIVATED)) {
            entity.get().setStatus(Status.DEACTIVATED);
            this.sensorRepository.save(entity.get());

            return ResponseEntity.ok()
                    .body(new AllSensorsDto(
                            entity.get().getName(),
                            entity.get().getType(),
                            entity.get().getDeviceModel(),
                            entity.get().getManufacturer(),
                            entity.get().getStatus()
                    ));
        } else {
            entity.get().setStatus(Status.ACTIVATED);
            this.sensorRepository.save(entity.get());

            return ResponseEntity.ok()
                    .body(new AllSensorsDto(
                            entity.get().getName(),
                            entity.get().getType(),
                            entity.get().getDeviceModel(),
                            entity.get().getManufacturer(),
                            entity.get().getStatus()
                    ));
        }
    }

    public ResponseEntity<String> getStatus(String deviceModel) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(deviceModel);

        return entity.map(sensor -> ResponseEntity.ok(sensor.getStatus().toString())).orElseThrow();
    }
}
