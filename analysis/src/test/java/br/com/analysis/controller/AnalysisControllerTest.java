package br.com.analysis.controller;

import br.com.analysis.dtos.RequestUpdateAnalysis;
import br.com.analysis.dtos.ResponseDeviceAnalysisDto;
import br.com.analysis.infra.exceptions.DeviceNotFoundException;
import br.com.analysis.metrics.MetricsService;
import br.com.analysis.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private ResultActions expectDefaultErrorStructure(ResultActions result) throws Exception {
        return result
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.source").exists())
                .andExpect(jsonPath("$.target").exists())
                .andExpect(jsonPath("$.service").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists())

                .andExpect(jsonPath("$.source").value("ANALYSIS"))
                .andExpect(jsonPath("$.target").value("DATABASE"))
                .andExpect(jsonPath("$.service").value("analysis"));
    }

    // ===================================== findDeviceForAnalysis ===================================================
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Device not found"))
                .andExpect(jsonPath("$.path").value("/api/get-device-for-model"));
    }
    // ===============================================================================================================

    // ============================================ updateAnalysis ===================================================

    @Test
    void shouldReturnResponseDeviceAnalysisDtoWhenUpdateAnalysis() throws Exception{

        var response = new ResponseDeviceAnalysisDto(
                "newName",
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

        when(this.analysisService.updateAnalysis("deviceModel", new RequestUpdateAnalysis(
                "newName",
                "",
                "",
                ""
        ))).thenReturn(response);

        this.mockMvc.perform(patch("/api/update-analysis/{deviceModel}", "deviceModel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name" : "newName",
                            "deviceModel" : "",
                            "manufacturer" : "",
                            "description" : ""
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("newName"))
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
    void shouldReturnThrowWhenUpdateAnalysis() throws Exception{

        when(this.analysisService.updateAnalysis("deviceModel", new RequestUpdateAnalysis(
                "newName",
                "",
                "",
                ""
        ))).thenThrow(new DeviceNotFoundException("Device not found"));

        this.mockMvc.perform(patch("/api/update-analysis/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name" : "newName",
                            "deviceModel" : "",
                            "manufacturer" : "",
                            "description" : ""
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Device not found"))
                .andExpect(jsonPath("$.path").value("/api/update-analysis/deviceModel"));
    }

    // ===============================================================================================================

    // ============================================ deleteAnalysis ===================================================

    @Test
    void shouldReturnResponseDeviceAnalysisDtoWhenDeleteAnalysis() throws Exception{

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

        when(this.analysisService.deleteAnalysis("deviceModel"))
                .thenReturn(response);

        this.mockMvc.perform(delete("/api/delete-analysis/{deviceModel}", "deviceModel"))
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
}