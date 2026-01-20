package br.com.sensor_test.controller;

import br.com.sensor_test.dtos.AllSensorsDto;
import br.com.sensor_test.dtos.UpdateSensor;
import br.com.sensor_test.service.SensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(value = "*", methods = {RequestMethod.GET, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.POST, RequestMethod.PUT})
public class SensorController {

    private final SensorService sensorService;

    @Autowired
    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping("/find-all-sensors-activated")
    public ResponseEntity<List<AllSensorsDto>> findAllSensorsActivated() {
        return this.sensorService.findAllSensorsActivated();
    }

    @GetMapping("/get-status/{deviceModel:.+}")
    public ResponseEntity<String> getStatus(@PathVariable String deviceModel) {
        return this.sensorService.getStatus(deviceModel);
    }

    @PatchMapping("/change-status/{deviceModel:.+}")
    public ResponseEntity<?> changeStatus(@PathVariable String deviceModel) {
       return this.sensorService.changeStatus(deviceModel);
    }

    @PatchMapping("/update-sensor/{deviceModel:.+}")
    public ResponseEntity<?> updateSensor(@PathVariable String deviceModel, @RequestBody UpdateSensor request){
        return this.sensorService.updateSensor(deviceModel, request);
    }

    @DeleteMapping("/delete-sensor/{deviceModel:.+}")
    public ResponseEntity<?> deleteSensor(@PathVariable String deviceModel){
        return this.sensorService.deleteSensor(deviceModel);
    }
}
