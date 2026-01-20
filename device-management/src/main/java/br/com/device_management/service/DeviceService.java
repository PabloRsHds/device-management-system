package br.com.device_management.service;

import br.com.device_management.dtos.*;
import br.com.device_management.model.Device;
import br.com.device_management.repository.DeviceRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final KafkaTemplate<String, DeviceManagementEventForSensor> kafkaTemplate;

    @Autowired
    public DeviceService(
            DeviceRepository deviceRepository,
            KafkaTemplate<String, DeviceManagementEventForSensor> kafkaTemplate) {
        this.deviceRepository = deviceRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public ResponseEntity<?> registerDevice(DeviceDto request) {

        Optional<Device> device = this.deviceRepository.findByDeviceModel(request.deviceModel());

        if (device.isPresent()) {
            log.info("Já está cadastrado");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("Message","This device model is already registered in the database")
            );
        }

        log.info("Novo dispositivo salvo no banco de dados");
        var newDevice = new Device();

        newDevice.setName(request.name());
        newDevice.setType(request.type());
        newDevice.setDescription(request.description());
        newDevice.setDeviceModel(request.deviceModel());
        newDevice.setManufacturer(request.manufacturer());
        newDevice.setLocation(request.location());
        newDevice.setUnit(request.type().getUnit());
        newDevice.setMinLimit(request.type().getMin());
        newDevice.setMaxLimit(request.type().getMax());
        newDevice.setCreatedAt(LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        log.info("Salvando o dispositivo e enviando uma mensagem ao usuário");
        this.deviceRepository.save(newDevice);

        this.kafkaTemplate.send("device-management-for-sensor-test-topic",
                new DeviceManagementEventForSensor(
                        request.name(),
                        request.type().toString(),
                        request.description(),
                        request.deviceModel(),
                        request.manufacturer(),
                        request.type().getUnit().toString(),
                        request.type().getMin(),
                        request.type().getMax()
                ));

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new AllDevicesDto(
                        request.name(),
                        request.type(),
                        request.description(),
                        request.deviceModel(),
                        request.manufacturer(),
                        request.location(),
                        request.type().getUnit(),
                        request.type().getMin(),
                        request.type().getMax())
        );
    }

    public ResponseEntity<?> updateDevice(String deviceModel,UpdateDevice request) {

        Optional<Device> entity = this.deviceRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            System.out.println("Não foi");
            return ResponseEntity.notFound().build();
        }

        if (request.newName() != null ) {
            entity.get().setName(request.newName());
        }

        if (request.newDeviceModel() != null ) {
            entity.get().setDeviceModel(request.newDeviceModel());
        }

        if (request.newManufacturer() != null) {
            entity.get().setManufacturer(request.newManufacturer());
        }

        if (request.newLocation() != null) {
            entity.get().setLocation(request.newLocation());
        }

        if (request.newDescription() != null) {
            entity.get().setDescription(request.newDescription());
        }
        this.deviceRepository.save(entity.get());

        return ResponseEntity.status(HttpStatus.OK).body(
                new AllDevicesDto(
                        entity.get().getName(),
                        entity.get().getType(),
                        entity.get().getDescription(),
                        entity.get().getDeviceModel(),
                        entity.get().getManufacturer(),
                        entity.get().getLocation(),
                        entity.get().getType().getUnit(),
                        entity.get().getType().getMin(),
                        entity.get().getType().getMax()
        ));
    }

    public ResponseEntity<?> deleteDevice(String deviceModel) {

        Optional<Device> entity = this.deviceRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var device = entity.get();

        this.deviceRepository.delete(entity.get());

        return ResponseEntity.status(HttpStatus.OK).body(
                new AllDevicesDto(
                        device.getName(),
                        device.getType(),
                        device.getDescription(),
                        device.getDeviceModel(),
                        device.getManufacturer(),
                        device.getLocation(),
                        device.getType().getUnit(),
                        device.getType().getMin(),
                        device.getType().getMax()
                ));
    }


    public ResponseEntity<List<AllDevicesDto>> allDevices() {

        return ResponseEntity.ok(
                deviceRepository.findAll().stream()
                        .sorted(Comparator.comparing(Device::getCreatedAt).reversed())
                        .map(device -> new AllDevicesDto(
                                device.getName(),
                                device.getType(),
                                device.getDescription(),
                                device.getDeviceModel(),
                                device.getManufacturer(),
                                device.getLocation(),
                                device.getUnit(),
                                device.getMinLimit(),
                                device.getMaxLimit()
                        ))

                        .toList()
        );
    }

    public ResponseEntity<FindByDeviceWithDeviceModel> findByDeviceWithDeviceModel(String deviceModel) {

        Optional<Device> entity = this.deviceRepository.findByDeviceModel(deviceModel);

        return entity.map(device -> ResponseEntity.ok(new FindByDeviceWithDeviceModel(
                device.getName(),
                device.getDeviceModel(),
                device.getManufacturer(),
                device.getLocation(),
                device.getDescription()
        ))).orElseGet(() -> ResponseEntity.notFound().build());

    }
}
