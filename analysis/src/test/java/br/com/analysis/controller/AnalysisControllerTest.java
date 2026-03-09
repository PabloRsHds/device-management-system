package br.com.analysis.controller;

import br.com.analysis.dtos.ResponseDeviceAnalysisDto;
import br.com.analysis.infra.exceptions.DeviceNotFoundException;
import br.com.analysis.metrics.MetricsService;
import br.com.analysis.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricsService metricsService;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void shouldReturnResponseDeviceAnalysisDtoWhenFindDeviceForAnalysis() throws Exception {

        var response = new ResponseDeviceAnalysisDto(
                "name",
                "deviceModel",
                0f,
                100f,
                "unit",
                "update",
                "created",
                10f,
                50f,
                "AA",
                1,
                2
        );

        when(this.analysisService.getDeviceForAnalysis("deviceModel"))
                .thenReturn(response);

        this.mockMvc.perform(get("/api/get-device-for-model")
                        .param("deviceModel", "deviceModel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.deviceModel").value("deviceModel"))
                .andExpect(jsonPath("$.minLimit").value(0f))
                .andExpect(jsonPath("$.maxLimit").value(100f))
                .andExpect(jsonPath("$.unit").value("unit"))
                .andExpect(jsonPath("$.updatedAt").value("update"))
                .andExpect(jsonPath("$.createdAt").value("created"))
                .andExpect(jsonPath("$.lastReadingMinLimit").value(10f))
                .andExpect(jsonPath("$.lastReadingMaxLimit").value(50f))
                .andExpect(jsonPath("$.lastReadingUpdateAt").value("AA"))
                .andExpect(jsonPath("$.analysisWorked").value(1))
                .andExpect(jsonPath("$.analysisFailed").value(2));
    }

    @Test
    void shouldReturnThrowWhenFindDeviceForAnalysis() throws Exception{

        when(this.analysisService.getDeviceForAnalysis("deviceModel"))
                .thenThrow(new DeviceNotFoundException("Device not found"));

        this.mockMvc.perform(get("/api/get-device-for-model")
                        .param("deviceModel", "deviceModel"))
                .andExpect(status().isBadRequest());
    }

}