package br.com.analysis.service;

import br.com.analysis.dtos.DeviceAnalysisDto;
import br.com.analysis.dtos.RequestUpdateAnalysis;
import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
                value.getDeviceModel(),
                value.getMinLimit(),
                value.getMaxLimit(),
                value.getUnit(),
                value.getUpdatedAt(),
                value.getCreatedAt(),
                value.getLastReadingMinLimit(),
                value.getLastReadingMaxLimit(),
                value.getLastReadingUpdateAt(),
                value.getAnalysisWorked(),
                value.getAnalysisFailed()

        ))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    public ResponseEntity<DeviceAnalysisDto> updateAnalysis(String deviceModel, RequestUpdateAnalysis request) {

        Optional<Analysis> entity = this.analysisRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!request.name().isBlank()) {
            entity.get().setName(request.name());
        }

        if (!request.deviceModel().isBlank()) {
            entity.get().setDeviceModel(request.deviceModel());
        }

        if (!request.manufacturer().isBlank()) {
            entity.get().setManufacturer(request.manufacturer());
        }

        if (!request.description().isBlank()) {
            entity.get().setDescription(request.description());
        }


        this.analysisRepository.save(entity.get());

        return ResponseEntity.ok(new DeviceAnalysisDto(
                entity.get().getName(),
                entity.get().getDeviceModel(),
                entity.get().getMinLimit(),
                entity.get().getMaxLimit(),
                entity.get().getUnit(),
                entity.get().getUpdatedAt(),
                entity.get().getCreatedAt(),
                entity.get().getLastReadingMinLimit(),
                entity.get().getLastReadingMaxLimit(),
                entity.get().getLastReadingUpdateAt(),
                entity.get().getAnalysisWorked(),
                entity.get().getAnalysisFailed()
        ));
    }

    @Transactional
    public ResponseEntity<DeviceAnalysisDto> deleteAnalysis(String deviceModel) {

        Optional<Analysis> entity = this.analysisRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var analysis = entity.get();
        this.analysisRepository.delete(entity.get());

        return ResponseEntity.ok(new DeviceAnalysisDto(
                analysis.getName(),
                analysis.getDeviceModel(),
                analysis.getMinLimit(),
                analysis.getMaxLimit(),
                analysis.getUnit(),
                analysis.getUpdatedAt(),
                analysis.getCreatedAt(),
                analysis.getLastReadingMinLimit(),
                analysis.getLastReadingMaxLimit(),
                analysis.getLastReadingUpdateAt(),
                analysis.getAnalysisWorked(),
                analysis.getAnalysisFailed()
        ));
    }
}
