package br.com.device_management.controller;

import br.com.device_management.dtos.AllDevicesDto;
import br.com.device_management.dtos.ResponseDeviceDto;
import br.com.device_management.dtos.register.DeviceDto;
import br.com.device_management.dtos.FindByDeviceWithDeviceModel;
import br.com.device_management.dtos.UpdateDeviceDto;
import br.com.device_management.dtos.register.ResponseDeviceDto;
import br.com.device_management.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DeviceController {

    private final DeviceService deviceService;

    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/register-device")
    public ResponseEntity<ResponseDeviceDto> registerDevice(@RequestBody DeviceDto request) {
        var device = this.deviceService.registerDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    @PatchMapping("/update-device/{deviceModel:.+}")
    public ResponseEntity<ResponseDeviceDto> updateDevice(@PathVariable String deviceModel,@RequestBody UpdateDeviceDto request) {
        var device = this.deviceService.updateDevice(deviceModel,request);
        return ResponseEntity.status(HttpStatus.OK).body(device);
    }

    @DeleteMapping("/delete-device/{deviceModel:.+}")
    public ResponseEntity<ResponseDeviceDto> deleteDevice(@PathVariable String deviceModel) {
        var device = this.deviceService.deleteDevice(deviceModel);
        return ResponseEntity.ok().body(device);
    }

    @GetMapping("/all-devices")
    public ResponseEntity<List<AllDevicesDto>> allDevices(){
        return this.deviceService.allDevices();
    }

    @GetMapping("/find-by-device/{deviceModel:.+}")
    public ResponseEntity<FindByDeviceWithDeviceModel> findByDeviceWithDeviceModel(@PathVariable String deviceModel){
        return this.deviceService.findByDeviceWithDeviceModel(deviceModel);
    }
}
