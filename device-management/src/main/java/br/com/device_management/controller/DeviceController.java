package br.com.device_management.controller;

import br.com.device_management.dtos.DeleteDevice;
import br.com.device_management.dtos.DeviceDto;
import br.com.device_management.dtos.UpdateDevice;
import br.com.device_management.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DeviceController {

    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/register-device")
    private ResponseEntity<Map<String, String>> registerDevice(@RequestBody DeviceDto request) {
        return this.deviceService.registerDevice(request);
    }

    @PatchMapping("/update-device/{deviceModel:.+}")
    private ResponseEntity<Map<String, String>> updateDevice(@PathVariable String deviceModel,@RequestBody UpdateDevice request) {
        return this.deviceService.updateDevice(deviceModel,request);
    }

    @DeleteMapping("/delete-device/{deviceModel:.+}")
    private ResponseEntity<Void> deleteDevice(@PathVariable String deviceModel) {
        return this.deviceService.deleteDevice(deviceModel);
    }

    @GetMapping("/all-devices")
    public ResponseEntity<List<DeviceDto>> allDevices(){
        return this.deviceService.allDevices();
    }
}
