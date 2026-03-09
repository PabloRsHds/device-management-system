package br.com.analysis.service;

import br.com.analysis.dtos.AnalysisEventForNotification;
import br.com.analysis.dtos.ConsumerSensorTest;
import br.com.analysis.dtos.RequestUpdateAnalysis;
import br.com.analysis.enums.AnalysisResult;
import br.com.analysis.infra.exceptions.DeviceNotFoundException;
import br.com.analysis.infra.exceptions.ServiceUnavailableException;
import br.com.analysis.metrics.MetricsService;
import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

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

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;


    // ========================================= REGISTER ============================================================

    @Test
    void shouldReturnAnalysisResultWhenAnalysisResultRegister() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.empty());

        this.analysisService.analysisResult(
                "deviceModel",10f, 1f,10f, 1f);

        verify(this.analysisRepository).findByDeviceModel("deviceModel");
        verifyNoInteractions(this.metricsService);
        verifyNoInteractions(this.kafkaTemplate);
    }

    @Test
    void shouldReturnAnalysisResultWhenAnalysisResultFailed() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Analysis()));

        this.analysisService.analysisResult(
                "deviceModel",10f, 1f,10f, 1f);

        verify(this.analysisRepository).findByDeviceModel("deviceModel");
        verify(this.metricsService).analysisSuccess(false);
        verifyNoInteractions(this.kafkaTemplate);
    }

    @Test
    void shouldReturnAnalysisResultWhenAnalysisResultSuccess() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Analysis()));

        this.analysisService.analysisResult(
                "deviceModel",10f, 0f,90f, 100f);

        verify(this.analysisRepository).findByDeviceModel("deviceModel");
        verifyNoInteractions(this.metricsService);
        verifyNoInteractions(this.kafkaTemplate);
    }

    @Test
    void shouldReturnVoidWhenAnalysisSuccess() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Analysis()));

        this.analysisService.analysisSuccess("deviceModel", 10f, 90f);

        this.kafkaTemplate.send("message",
                new AnalysisEventForNotification("deviceModel", false));

        verify(this.metricsService).analysisSuccess(true);
    }

    @Test
    void shouldReturnVoidWhenRegister() {

        var consumer = mock(ConsumerSensorTest.class);

        this.analysisService.register(consumer);

        this.kafkaTemplate.send("message",
                new AnalysisEventForNotification("deviceModel", true));
    }

    @Test
    void shouldReturnThrowWhenSendEventKafkaRetry() {

        var exception = mock(Exception.class);

        assertThrows(ServiceUnavailableException.class,
                () -> this.analysisService.sendEventKafkaRetry(
                        "topic",
                        new AnalysisEventForNotification("deviceModel", true),
                        exception));
    }

    @Test
    void shouldReturnThrowWhenSendEventKafkaCircuitBreaker() {

        var exception = mock(Exception.class);

        assertThrows(ServiceUnavailableException.class,
                () -> this.analysisService.sendEventKafkaCircuitBreaker(
                        "topic",
                        new AnalysisEventForNotification("deviceModel", true),
                        exception));
    }

    //================================================================================================================

    // ========================================== FIND DEVICE FOR ANALYSIS ===========================================

    @Test
    void shouldReturnResponseDeviceAnalysisDtoWhenGetDeviceForAnalysis() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Analysis()));

        var response = this.analysisService.getDeviceForAnalysis("deviceModel");

        assertNotNull(response);
    }

    @Test
    void shouldReturnAnalysisWhenGetDeviceWithModel(){

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Analysis()));

        this.analysisService.getDeviceWithModel("deviceModel");
    }

    @Test
    void shouldReturnThrowWhenGetDeviceWithModel(){

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class,
                () -> this.analysisService.getDeviceWithModel("deviceModel"));
    }

    @Test
    void shouldReturnAnalysisWhenGetDeviceWithModelRetry() {

        var exception = mock(Exception.class);

        assertThrows(ServiceUnavailableException.class,
                () -> this.analysisService.getDeviceWithModelRetry("deviceModel", exception));
    }

    @Test
    void shouldReturnAnalysisWhenGetDeviceWithModelCircuitBreaker() {

        var exception = mock(Exception.class);

        assertThrows(ServiceUnavailableException.class,
                () -> this.analysisService.getDeviceWithModelCircuitBreaker("deviceModel", exception));
    }

    // ===============================================================================================================

    // ================================================ UPDATE ========================================================

    @Test
    void shouldReturnThrowDeviceAnalysisDtoWhenUpdateAnalysis() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.empty());

        assertThrows(DeviceNotFoundException.class,
                () -> this.analysisService.updateAnalysis("deviceModel", new RequestUpdateAnalysis(
                        "newName",
                        "",
                        "",
                        "")));
    }

    @Test
    void shouldReturnResponseDeviceAnalysisDtoWhenUpdateAnalysis() {

        when(this.analysisRepository.findByDeviceModel("deviceModel"))
                .thenReturn(Optional.of(new Analysis()));

        this.analysisService.updateAnalysis("deviceModel", new RequestUpdateAnalysis(
                "newName",
                "",
                "",
                ""
        ));
    }


}