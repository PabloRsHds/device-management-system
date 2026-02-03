package br.com.sensor_test.service;

import br.com.sensor_test.dtos.AllSensorsDto;
import br.com.sensor_test.dtos.UpdateSensor;
import br.com.sensor_test.dtos.sensor.ResponseSensorDto;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.infra.exceptions.SensorIsEmptyException;
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

    // =============================================  UPDATE =========================================================

    public ResponseSensorDto updateSensor(String deviceModel, UpdateSensor request) {

        var entity = verifyIfSensorIsPresent(deviceModel);
        return this.update(entity, request);
    }

    // Metodo para verificar se o sensor é presente, se não ele retorna um erro.
    public Sensor verifyIfSensorIsPresent(String deviceModel) {

        Optional<Sensor> entity = this.sensorRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            throw new SensorIsEmptyException("Sensor not found");
        }

        return entity.get();
    }

    @Transactional
    public ResponseSensorDto update(Sensor entity, UpdateSensor request) {

        if (!request.name().isBlank()) {
            entity.setName(request.name());
        }
        if (!request.deviceModel().isBlank()) {
            entity.setDeviceModel(request.deviceModel());
        }
        if (!request.manufacturer().isBlank()) {
            entity.setManufacturer(request.manufacturer());
        }

        this.sensorRepository.save(entity);

        return new ResponseSensorDto(
                entity.getName(),
                entity.getType(),
                entity.getDeviceModel(),
                entity.getManufacturer(),
                entity.getStatus()
        );
    }

    // ===============================================================================================================

    // ========================================== DELETE =============================================================

    public ResponseSensorDto deleteSensor(String deviceModel) {

        var entity = this.verifyIfSensorIsPresent(deviceModel);
        var response = new ResponseSensorDto(
                entity.getName(),
                entity.getType(),
                entity.getDeviceModel(),
                entity.getManufacturer(),
                entity.getStatus()
        );
        this.delete(entity);

        return response;
    }

    @Transactional
    public void delete(Sensor entity) {
        this.sensorRepository.delete(entity);
    }

    // ===============================================================================================================


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
