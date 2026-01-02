package br.com.analysis.controller;

import br.com.analysis.dtos.DeviceAnalysis;
import br.com.analysis.dtos.DeviceAnalysisDto;
import br.com.analysis.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class AnalysisController {

    private final AnalysisService analysisService;

    @Autowired
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/get-all-analysis")
    public ResponseEntity<List<DeviceAnalysisDto>> allDevicesAnalysis() {
        return this.analysisService.allDevicesAnalysis();
    }

    @GetMapping("/get-device-for-model")
    public ResponseEntity<DeviceAnalysisDto> findDeviceForAnalysis(@RequestParam String deviceModel) {
        return this.analysisService.findDeviceForAnalysis(deviceModel);
    }

}
