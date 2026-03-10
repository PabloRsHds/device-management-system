package br.com.analysis.integration;

import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
import br.com.analysis.service.AnalysisService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class AnalysisIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private AnalysisService analysisService;

    // ================================== findDeviceForAnalysis ======================================================

    @Test
    void shouldReturnResponseDeviceAnalysisDtoWhenFindDeviceForAnalysis() throws Exception{

        var entity = new Analysis();
        entity.setName("name");
        entity.setDeviceModel("deviceModel");
        this.analysisRepository.save(entity);

        this.mockMvc.perform(get("/api/get-device-for-model")
                .param("deviceModel", "deviceModel"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnThrowWhenFindDeviceForAnalysis() throws Exception{

        this.mockMvc.perform(get("/api/get-device-for-model")
                        .param("deviceModel", "deviceModel"))
                .andExpect(status().isBadRequest());
    }

    // ===============================================================================================================

    // =========================================== updateAnalysis ====================================================

    @Test
    void shouldReturnResponseDeviceAnalysisDtoWhenUpdateAnalysis() throws Exception {

        var entity = new Analysis();
        entity.setName("name");
        entity.setDeviceModel("deviceModel");
        this.analysisRepository.save(entity);

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
                .andExpect(status().isOk());
    }


}
