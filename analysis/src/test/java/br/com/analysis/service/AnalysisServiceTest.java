package br.com.analysis.service;

import br.com.analysis.metrics.MetricsService;
import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private AnalysisRepository analysisRepository;

    @InjectMocks
    private AnalysisService analysisService;

    @Mock
    private MetricsService metricsService;


    // ========================================= REGISTER ============================================================

    @Test
    void shouldReturnAnalysisResultWhenAnalysisResultRegister() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.empty());

        this.analysisService.analysisResult(
                "deviceModel",10f, 1f,10f, 1f);

        verify(this.analysisRepository).findByDeviceModel("deviceModel");
        verifyNoInteractions(this.metricsService);
    }

    @Test
    void shouldReturnAnalysisResultWhenAnalysisResultFailed() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Analysis()));

        this.analysisService.analysisResult(
                "deviceModel",10f, 1f,10f, 1f);

        verify(this.analysisRepository).findByDeviceModel("deviceModel");
        verify(this.metricsService).analysisSuccess(false);
    }

    @Test
    void shouldReturnAnalysisResultWhenAnalysisResultSuccess() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Analysis()));

        this.analysisService.analysisResult(
                "deviceModel",10f, 0f,90f, 100f);

        verify(this.analysisRepository).findByDeviceModel("deviceModel");
        verifyNoInteractions(this.metricsService);
    }
}