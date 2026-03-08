package br.com.analysis.service;

import br.com.analysis.repository.AnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {

    @Mock
    private AnalysisRepository analysisRepository;

    @InjectMocks
    private AnalysisService analysisService;


    // ========================================= REGISTER ============================================================

    @Test
    void shouldReturnAnalysisResultWhenAnalysisResultRegister() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.empty());

        this.analysisService.analysisResult(
                "deviceModel",10f, 1f,10f, 1f);

        verify(this.analysisRepository).findByDeviceModel("deviceModel");
    }
}