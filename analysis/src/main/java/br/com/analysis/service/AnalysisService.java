package br.com.analysis.service;

import br.com.analysis.dtos.DeviceAnalysisDto;
import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
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
}
