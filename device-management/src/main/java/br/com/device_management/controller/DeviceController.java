package br.com.device_management.controller;

import br.com.device_management.dtos.ResponseDeviceDto;
import br.com.device_management.dtos.register.DeviceDto;
import br.com.device_management.dtos.getDeviceWithDeviceModel;
import br.com.device_management.dtos.UpdateDeviceDto;
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
        var response = this.deviceService.registerDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/update-device/{deviceModel:.+}")
    public ResponseEntity<ResponseDeviceDto> updateDevice(@PathVariable String deviceModel,@RequestBody UpdateDeviceDto request) {
        var response = this.deviceService.updateDevice(deviceModel,request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/delete-device/{deviceModel:.+}")
    public ResponseEntity<ResponseDeviceDto> deleteDevice(@PathVariable String deviceModel) {
        var response = this.deviceService.deleteDevice(deviceModel);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/all-devices")
    public ResponseEntity<List<ResponseDeviceDto>> allDevices(@RequestParam int page,
                                                              @RequestParam int size){
        var response = this.deviceService.getAllDevices(page, size);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/find-by-device/{deviceModel:.+}")
    public ResponseEntity<getDeviceWithDeviceModel> getDeviceWithDeviceModel(@PathVariable String deviceModel){
        var response = this.deviceService.getDeviceWithDeviceModel(deviceModel);
        return ResponseEntity.ok().body(response);
    }
}
