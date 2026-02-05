package br.com.analysis.controller;

import br.com.analysis.dtos.ResponseDeviceAnalysisDto;
import br.com.analysis.dtos.RequestUpdateAnalysis;
import br.com.analysis.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;

    @Autowired
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/get-device-for-model")
    public ResponseEntity<ResponseDeviceAnalysisDto> findDeviceForAnalysis(@RequestParam String deviceModel) {
        var response = this.analysisService.findDeviceForAnalysis(deviceModel);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-analysis/{deviceModel:.+}")
    public ResponseEntity<ResponseDeviceAnalysisDto> updateAnalysis(@PathVariable String deviceModel, @RequestBody RequestUpdateAnalysis request) {
        var response = this.analysisService.updateAnalysis(deviceModel, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-analysis/{deviceModel:.+}")
    public ResponseEntity<ResponseDeviceAnalysisDto> deleteAnalysis(@PathVariable String deviceModel) {
        var response = this.analysisService.deleteAnalysis(deviceModel);
        return ResponseEntity.ok(response);
    }
}
