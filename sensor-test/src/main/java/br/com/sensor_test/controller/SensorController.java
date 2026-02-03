package br.com.sensor_test.controller;

import br.com.sensor_test.dtos.AllSensorsDto;
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
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/delete-sensor/{deviceModel:.+}")
    public ResponseEntity<?> deleteSensor(@PathVariable String deviceModel){
        var response = this.sensorService.deleteSensor(deviceModel);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/find-all-sensors-activated")
    public ResponseEntity<List<ResponseSensorDto>> findAllSensorsActivated(@RequestParam int page, @RequestParam int size) {
        var response = this.sensorService.findAllSensorsActivated(page, size);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/get-status/{deviceModel:.+}")
    public ResponseEntity<String> getStatus(@PathVariable String deviceModel) {
        return this.sensorService.getStatus(deviceModel);
    }

    @PatchMapping("/change-status/{deviceModel:.+}")
    public ResponseEntity<?> changeStatus(@PathVariable String deviceModel) {
       return this.sensorService.changeStatus(deviceModel);
    }
}
