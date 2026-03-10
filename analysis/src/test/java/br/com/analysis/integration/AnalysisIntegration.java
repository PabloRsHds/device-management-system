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
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    // ================================== findDeviceForAnalysis ======================================================

    @Test
    void shouldReturnResponseDeviceAnalysisDtoWhenFindDeviceForAnalysis() throws Exception{

        var entity = new Analysis();
        entity.setName("name");
        entity.setDeviceModel("deviceModel");
        entity.setMinLimit(0f);
        entity.setMaxLimit(100f);
        entity.setUnit("unit");
        entity.setUpdatedAt("update");
        entity.setCreatedAt("created");
        entity.setLastReadingMinLimit(10f);
        entity.setLastReadingMaxLimit(50f);
        entity.setLastReadingUpdateAt("AA");
        entity.setAnalysisWorked(1);
        entity.setAnalysisFailed(2);
        this.analysisRepository.save(entity);

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

        this.mockMvc.perform(get("/api/get-device-for-model")
                        .param("deviceModel", "deviceModel"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Device not found for analysis"))
                .andExpect(jsonPath("$.path").value("/api/get-device-for-model"));
    }

    // ===============================================================================================================

    // =========================================== updateAnalysis ====================================================

    @Test
    void shouldReturnResponseDeviceAnalysisDtoWhenUpdateAnalysis() throws Exception {

        var entity = new Analysis();
        entity.setName("name");
        entity.setDeviceModel("deviceModel");
        entity.setMinLimit(0f);
        entity.setMaxLimit(100f);
        entity.setUnit("unit");
        entity.setUpdatedAt("update");
        entity.setCreatedAt("created");
        entity.setLastReadingMinLimit(10f);
        entity.setLastReadingMaxLimit(50f);
        entity.setLastReadingUpdateAt("AA");
        entity.setAnalysisWorked(1);
        entity.setAnalysisFailed(2);
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
    void shouldReturnThrowWhenUpdateAnalysis() throws Exception {

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
                .andExpect(jsonPath("$.message").value("Device not found for analysis"))
                .andExpect(jsonPath("$.path").value("/api/get-device-for-model"));
    }

    // ===============================================================================================================

    // ========================================== deleteAnalysis =====================================================

    @Test
    void shouldReturnResponseDeviceAnalysisDtoWhenDeleteAnalysis() throws Exception {

        var entity = new Analysis();
        entity.setName("name");
        entity.setDeviceModel("deviceModel");
        this.analysisRepository.save(entity);

        this.mockMvc.perform(delete("/api/delete-analysis/{deviceModel}", "deviceModel"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnThrowWhenDeleteAnalysis() throws Exception {

        this.mockMvc.perform(delete("/api/delete-analysis/{deviceModel}", "deviceModel"))
                .andExpect(status().isBadRequest());
    }
}
