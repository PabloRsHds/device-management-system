package br.com.device_management.service;

import br.com.device_management.dtos.DeleteDevice;
import br.com.device_management.dtos.DeviceDto;
import br.com.device_management.dtos.UpdateDevice;
import br.com.device_management.enums.Status;
import br.com.device_management.model.Device;
import br.com.device_management.repository.DeviceRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Transactional
    public ResponseEntity<Map<String, String>> registerDevice(DeviceDto request) {

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
        newDevice.setStatus(Status.ACTIVATED);
        newDevice.setLocation(request.location());
        newDevice.setUnit(request.unit());
        newDevice.setMinLimit(request.minLimit());
        newDevice.setMaxLimit(request.maxLimit());
        newDevice.setCreatedAt(LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        log.info("Salvando o dispositivo e enviando uma mensagem ao usuário");
        this.deviceRepository.save(newDevice);

        //Adicionar um kafka aqui, onde eu vou enviar os arquivos para o simulador de dispositivos e lá tbm adicionar um banco de dados,
        //para que eu possa ter um histórico de todos os dispositivos que foram cadastrados.
        //e assim utilizar o status lá, se o dispostivo for active ele rodará no sheduling, e tbm no microserviço de teste de dispositvos
        //Vou adcionar uma função onde eu possa alterar o status, ai no front eu clico no botao e muda o status do dispositivo e assim começa o teste,
        //E tbm adicionarei um retorno de todos os dispositivos que estão ativos e que estão sendo testados, os que estiverem inativos serão ignorados

        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("Message","Your device has been registered successfully!")
        );
    }

    public ResponseEntity<Map<String, String>> updateDevice(String deviceModel,UpdateDevice request) {

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

        return ResponseEntity.ok().body(
                Map.of("Update", "Device updated successfully")
        );
    }

    public ResponseEntity<Void> deleteDevice(String deviceModel) {

        Optional<Device> entity = this.deviceRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        this.deviceRepository.delete(entity.get());

        return ResponseEntity.ok().build();
    }


    public ResponseEntity<List<DeviceDto>> allDevices() {

        return ResponseEntity.ok(
                deviceRepository.findAll().stream()
                        .map(device -> new DeviceDto(
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
}
