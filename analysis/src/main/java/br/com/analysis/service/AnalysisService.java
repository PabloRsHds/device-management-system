package br.com.analysis.service;

import br.com.analysis.dtos.DeviceAnalysisDto;
import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;

    @Autowired
    public AnalysisService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    public ResponseEntity<DeviceAnalysisDto> findDeviceForAnalysis(String deviceModel) {

        Optional<Analysis> analysis = this.analysisRepository.findByDeviceModel(deviceModel);

        return analysis.map(value -> ResponseEntity.ok(new DeviceAnalysisDto(
                value.getName(),
                value.getType(),
                value.getDescription(),
                value.getDeviceModel(),
                value.getManufacturer(),
                value.getStatus(),
                value.getLocation(),
                value.getUnit(),
                value.getMinLimit(),
                value.getMaxLimit(),
                value.getLastReadingMinLimit(),
                value.getLastReadingMaxLimit(),
                value.getLastReadingUpdateAt(),
                value.getUpdatedAt(),
                value.getCreatedAt()
        ))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<List<DeviceAnalysisDto>> allDevicesAnalysis() {

        return ResponseEntity.ok(
                analysisRepository.findAll().stream()
                        .map(device -> new DeviceAnalysisDto(
                                device.getName(),
                                device.getType(),
                                device.getDescription(),
                                device.getDeviceModel(),
                                device.getManufacturer(),
                                device.getStatus(),
                                device.getLocation(),
                                device.getUnit(),
                                device.getMinLimit(),
                                device.getMaxLimit(),
                                device.getLastReadingMinLimit(),
                                device.getLastReadingMaxLimit(),
                                device.getLastReadingUpdateAt(),
                                device.getUpdatedAt(),
                                device.getCreatedAt()
                        ))
                        .toList()
        );
    }
}
