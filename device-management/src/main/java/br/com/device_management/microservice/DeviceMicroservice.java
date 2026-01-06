package br.com.device_management.microservice;

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

    @GetMapping("/verification-for-device-analysis")
    private Boolean verificationForDeviceAnalysis(@RequestParam String deviceModel,
                                                  @RequestParam Float minLimit,
                                                  @RequestParam Float maxLimit) {

        Optional<Device> entity = this.deviceRepository.findByDeviceModel(deviceModel);

        if (entity.get().getMinLimit() <= minLimit &&
                entity.get().getMaxLimit() >= maxLimit) {

            System.out.println(entity.get().getMinLimit());
            System.out.println(entity.get().getMaxLimit());
            System.out.println(minLimit);
            System.out.println(maxLimit);

            return true;
        }

        return false;
    }
}
