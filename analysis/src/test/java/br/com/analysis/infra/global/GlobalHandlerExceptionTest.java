package br.com.analysis.infra.global;

import br.com.analysis.controller.AnalysisController;
import br.com.analysis.infra.exceptions.DeviceNotFoundException;
import br.com.analysis.infra.exceptions.ServiceUnavailableException;
import br.com.analysis.metrics.MetricsService;
import br.com.analysis.repository.AnalysisRepository;
import br.com.analysis.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalHandlerExceptionTest {


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

    @Test
    void shouldReturnThrowWhenDeviceNotFoundException() throws Exception{

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


}