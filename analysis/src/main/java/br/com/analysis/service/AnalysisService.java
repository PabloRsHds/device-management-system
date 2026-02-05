package br.com.analysis.service;

import br.com.analysis.dtos.ResponseDeviceAnalysisDto;
import br.com.analysis.dtos.RequestUpdateAnalysis;
import br.com.analysis.infra.DeviceNotFoundException;
import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
import jakarta.transaction.Transactional;
import org.bouncycastle.pqc.crypto.util.PQCOtherInfoGenerator;
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


    // ================================================ UPDATE ========================================================

    public ResponseDeviceAnalysisDto updateAnalysis(String deviceModel, RequestUpdateAnalysis request) {

        var entity = this.findDeviceModel(deviceModel);

        return this.update(entity, request);
    }

    @Transactional
    public ResponseDeviceAnalysisDto update(Analysis entity, RequestUpdateAnalysis request) {

        if (!request.name().isBlank()) {
            entity.setName(request.name());
        }

        if (!request.deviceModel().isBlank()) {
            entity.setDeviceModel(request.deviceModel());
        }

        if (!request.manufacturer().isBlank()) {
            entity.setManufacturer(request.manufacturer());
        }

        if (!request.description().isBlank()) {
            entity.setDescription(request.description());
        }

        this.analysisRepository.save(entity);

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
                entity.getAnalysisFailed()
        );
    }

    // ===============================================================================================================

    // ================================================ DELETE =======================================================


    public ResponseDeviceAnalysisDto deleteAnalysis(String deviceModel) {

        var entity = this.findDeviceModel(deviceModel);
        return this.delete(entity);
    }

    @Transactional
    public ResponseDeviceAnalysisDto delete(Analysis entity) {

        var response = new ResponseDeviceAnalysisDto(
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
                entity.getAnalysisFailed()
        );

        this.analysisRepository.delete(entity);
        return response;
    }


    // ===============================================================================================================
}
