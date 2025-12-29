package br.com.device_management.microservice;

import br.com.device_management.enums.Type;
import br.com.device_management.enums.Unit;
import br.com.device_management.model.Device;
import br.com.device_management.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/microservice")
public class DeviceMicroservice {

    private final DeviceRepository deviceRepository;

    @Autowired
    public DeviceMicroservice(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @GetMapping("/verification-for-iot_gateway")
    private Boolean verificationForIotGateway(
            @RequestParam String deviceId,
            @RequestParam String name,
            @RequestParam Type type,
            @RequestParam String description,
            @RequestParam String deviceModel,
            @RequestParam String manufacturer) {

        Optional<Device> device = this.deviceRepository.findById(deviceId);

        if (device.isPresent() &&
            device.get().getName().equals(name) &&
            device.get().getType().equals(type) &&
            device.get().getDescription().equals(description) &&
            device.get().getDeviceModel().equals(deviceModel) &&
            device.get().getManufacturer().equals(manufacturer)) {

            return true;
        }

        return false;
    }

    @GetMapping("/verification-for-device-analysis")
    private Boolean verificationForDeviceAnalysis(@RequestParam String deviceId,
                                                  @RequestParam Unit unit,
                                                  @RequestParam Float minLimit,
                                                  @RequestParam Float maxLimit) {

        Optional<Device> device = this.deviceRepository.findById(deviceId);

        if (device.isPresent() &&
            device.get().getUnit().equals(unit) &&
            device.get().getMinLimit() <= minLimit &&
            device.get().getMaxLimit() >= maxLimit) {

            System.out.println(device.get().getMinLimit());
            System.out.println(device.get().getMaxLimit());
            System.out.println(minLimit);
            System.out.println(maxLimit);

            return true;
        }

        return false;
    }
}
