package br.com.device_management.service;

import br.com.device_management.dtos.AllDevicesDto;
import br.com.device_management.dtos.DeviceManagementEventForSensor;
import br.com.device_management.dtos.FindByDeviceWithDeviceModel;
import br.com.device_management.dtos.UpdateDevice;
import br.com.device_management.dtos.register.DeviceDto;
import br.com.device_management.infra.exceptions.DeviceIsPresent;
import br.com.device_management.infra.exceptions.ServiceUnavailable;
import br.com.device_management.model.Device;
import br.com.device_management.repository.DeviceRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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


    // ========================================== REGISTER DEVICE ====================================================


    public DeviceDto registerDevice(DeviceDto request) {

        log.info("Verificando se o dispositivo ja esta cadastrado");
        this.verifyIfDeviceIsPresent(request.deviceModel());

        log.info("Novo dispositivo salvo no banco de dados");
        var device = this.save(request);

        log.info("Enviando evento para o sensor");
        this.sendEvent("device-management-for-sensor-test-topic",device);

        return device;
    }

    @Retry(name = "retry-database", fallbackMethod = "retry_for_database")
    @CircuitBreaker(name = "circuitbreaker-database", fallbackMethod = "circuitbreaker_for_database")
    public void verifyIfDeviceIsPresent(String deviceModel) {

        Optional<Device> device = this.deviceRepository.findByDeviceModel(deviceModel);

        if (device.isPresent()) {
            throw new DeviceIsPresent("This device model is already registered in the database");
        }
    }

    public void retry_for_database(String deviceModel, Exception e) {
        log.error("Erro ao verificar se o dispositivo ja esta cadastrado", e);
        throw new ServiceUnavailable("Database service unavailable");
    }

    public void circuitbreaker_for_database(String deviceModel, Exception e) {
        log.error("Erro ao verificar se o dispositivo ja esta cadastrado", e);
        throw new ServiceUnavailable("Database service unavailable, please try again later");
    }

    @Transactional
    public DeviceDto save(DeviceDto dto) {

        var newDevice = new Device();

        newDevice.setName(dto.name());
        newDevice.setType(dto.type());
        newDevice.setDescription(dto.description());
        newDevice.setDeviceModel(dto.deviceModel());
        newDevice.setManufacturer(dto.manufacturer());
        newDevice.setLocation(dto.location());
        newDevice.setUnit(dto.type().getUnit());
        newDevice.setMinLimit(dto.type().getMin());
        newDevice.setMaxLimit(dto.type().getMax());
        newDevice.setCreatedAt(LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        log.info("Salvando o dispositivo e enviando uma mensagem ao usuário");
        this.deviceRepository.save(newDevice);

        return new DeviceDto(
                newDevice.getName(),
                newDevice.getType(),
                newDevice.getDescription(),
                newDevice.getDeviceModel(),
                newDevice.getManufacturer(),
                newDevice.getLocation()
        );
    }

    @CircuitBreaker(name = "circuitbreaker-kafka", fallbackMethod = "circuitbreaker_for_kafka")
    public void sendEvent(String topic, DeviceDto dto) {

        this.kafkaTemplate.send(topic,
                new DeviceManagementEventForSensor(
                        dto.name(),
                        dto.type().toString(),
                        dto.description(),
                        dto.deviceModel(),
                        dto.manufacturer(),
                        dto.type().getUnit().toString(),
                        dto.type().getMin(),
                        dto.type().getMax()
                ));
    }

    public void circuitbreaker_for_kafka(String topic, DeviceDto dto, Exception e) {
        log.error("Kafka service unavailable, error: ", e);
    }

    // ================================================================================================================



    // =========================================== UPDATE =============================================================
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

    //=================================================================================================================

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
