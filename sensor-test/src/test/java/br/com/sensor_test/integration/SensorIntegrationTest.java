package br.com.sensor_test.integration;

import br.com.sensor_test.enums.Status;
import br.com.sensor_test.model.Sensor;
import br.com.sensor_test.repository.SensorRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class SensorIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SensorRepository sensorRepository;


    // ============================================= UPDATE INTEGRATION ==============================================
    @Test
    void shouldReturnSuccessWhenUpdateSensor() throws Exception{

        var sensor = new Sensor();
        sensor.setDeviceModel("deviceModel");
        sensor.setStatus(Status.ACTIVATED);
        this.sensorRepository.save(sensor);

        this.mockMvc.perform(patch("/api/update-sensor/{deviceModel}", "deviceModel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name" : "newName",
                            "deviceModel" : "",
                            "manufacturer" : ""
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnFailedWhenUpdateSensor() throws Exception{

        this.mockMvc.perform(patch("/api/update-sensor/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name" : "newName",
                            "deviceModel" : "",
                            "manufacturer" : ""
                        }
                        """))
                .andExpect(status().isConflict());
    }

    // ===============================================================================================================

    // ============================================= DELETE INTEGRATION ==============================================

    @Test
    void shouldReturnSuccessWhenDeleteSensor() throws Exception{

        var sensor = new Sensor();
        sensor.setDeviceModel("deviceModel");
        sensor.setStatus(Status.ACTIVATED);
        this.sensorRepository.save(sensor);

        this.mockMvc.perform(delete("/api/delete-sensor/{deviceModel}", "deviceModel"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnFailedWhenDeleteSensor() throws Exception{

        this.mockMvc.perform(delete("/api/delete-sensor/{deviceModel}", "deviceModel"))
                .andExpect(status().isConflict());
    }
}
