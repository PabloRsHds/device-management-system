package br.com.sensor_test.controller;

import br.com.sensor_test.dtos.UpdateSensor;
import br.com.sensor_test.dtos.sensor.ResponseSensorDto;
import br.com.sensor_test.service.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SensorController {

    private final SensorService sensorService;

    @Autowired
    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @PatchMapping("/update-sensor/{deviceModel:.+}")
    public ResponseEntity<ResponseSensorDto> updateSensor(@PathVariable String deviceModel, @RequestBody UpdateSensor request){
        var response = this.sensorService.updateSensor(deviceModel, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-sensor/{deviceModel:.+}")
    public ResponseEntity<?> deleteSensor(@PathVariable String deviceModel){
        var response = this.sensorService.deleteSensor(deviceModel);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/find-all-sensors-activated")
    public ResponseEntity<List<ResponseSensorDto>> findAllSensorsActivated(@RequestParam int page, @RequestParam int size) {
        var response = this.sensorService.findAllSensorsActivated(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-status/{deviceModel:.+}")
    public ResponseEntity<String> getStatus(@PathVariable String deviceModel) {
        var response = this.sensorService.getStatus(deviceModel);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/change-status/{deviceModel:.+}")
    public ResponseEntity<ResponseSensorDto> changeStatus(@PathVariable String deviceModel) {
       var response = this.sensorService.changeStatus(deviceModel);
       return ResponseEntity.ok(response);
    }
}
