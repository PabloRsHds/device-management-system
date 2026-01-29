package br.com.analysis.controller;

import br.com.analysis.dtos.DeviceAnalysisDto;
import br.com.analysis.dtos.RequestUpdateAnalysis;
import br.com.analysis.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class AnalysisController {

    private final AnalysisService analysisService;

    @Autowired
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/get-device-for-model")
    public ResponseEntity<DeviceAnalysisDto> findDeviceForAnalysis(@RequestParam String deviceModel) {
        return this.analysisService.findDeviceForAnalysis(deviceModel);
    }

    @PatchMapping("/update-analysis/{deviceModel:.+}")
    public ResponseEntity<DeviceAnalysisDto> updateAnalysis(@PathVariable String deviceModel,@RequestBody RequestUpdateAnalysis request) {
        return this.analysisService.updateAnalysis(deviceModel, request);
    }

    @DeleteMapping("/delete-analysis/{deviceModel:.+}")
    public ResponseEntity<DeviceAnalysisDto> deleteAnalysis(@PathVariable String deviceModel) {
        return this.analysisService.deleteAnalysis(deviceModel);
    }
}
