package br.com.analysis.service;

import br.com.analysis.dtos.ResponseDeviceAnalysisDto;
import br.com.analysis.dtos.RequestUpdateAnalysis;
import br.com.analysis.infra.DeviceNotFoundException;
import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;

    public AnalysisService(AnalysisRepository analysisRepository) {
        this.analysisRepository = analysisRepository;
    }

    // ====================================== FIND DEVICE FOR ANALYSIS ==============================================
    public ResponseDeviceAnalysisDto findDeviceForAnalysis(String deviceModel) {

        var entity = this.findDeviceModel(deviceModel);

        return new ResponseDeviceAnalysisDto(
                entity.getName(),
                entity.getDeviceModel(),
                entity.getMinLimit(),
                entity.getMaxLimit(),
                entity.getUnit(),
                entity.getUpdatedAt(),
                entity.getCreatedAt(),
                entity.getLastReadingMinLimit(),
                entity.getLastReadingMaxLimit(),
                entity.getLastReadingUpdateAt(),
                entity.getAnalysisWorked(),
                entity.getAnalysisFailed());
    }

    public Analysis findDeviceModel(String deviceModel) {

        Optional<Analysis> entity = this.analysisRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            throw new DeviceNotFoundException("Device not found for analysis");
        }

        return entity.get();
    }
    // ===============================================================================================================

    public ResponseEntity<ResponseDeviceAnalysisDto> updateAnalysis(String deviceModel, RequestUpdateAnalysis request) {

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

        return ResponseEntity.ok(new ResponseDeviceAnalysisDto(
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
    public ResponseEntity<ResponseDeviceAnalysisDto> deleteAnalysis(String deviceModel) {

        Optional<Analysis> entity = this.analysisRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var analysis = entity.get();
        this.analysisRepository.delete(entity.get());

        return ResponseEntity.ok(new ResponseDeviceAnalysisDto(
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
