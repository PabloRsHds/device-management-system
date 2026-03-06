package br.com.sensor_test.infra.global;

import br.com.sensor_test.controller.SensorController;
import br.com.sensor_test.dtos.UpdateSensor;
import br.com.sensor_test.infra.exceptions.SensorIsEmptyException;
import br.com.sensor_test.infra.exceptions.ServiceUnavailableException;
import br.com.sensor_test.metrics.MetricsForExceptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalHandlerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SensorController sensorController;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

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

                .andExpect(jsonPath("$.source").value("SENSOR-TEST"))
                .andExpect(jsonPath("$.target").value("DATABASE"))
                .andExpect(jsonPath("$.service").value("sensor-test"));
    }

    @Test
    void shouldReturnThrowWhenServiceUnavailableException() throws Exception{

        when(this.sensorController.updateSensor(eq("deviceModel"), any(UpdateSensor.class)))
                .thenThrow(new ServiceUnavailableException("Service unavailable"));

        this.mockMvc.perform(patch("/api/update-sensor/{deviceModel}", "deviceModel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name" : "name",
                            "deviceModel" : "deviceModel",
                            "manufacturer" : "manufacturer"
                        }
                        """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("SERVICE UNAVAILABLE"))
                .andExpect(jsonPath("$.message").value("Service unavailable"))
                .andExpect(jsonPath("$.path").value("/api/update-sensor/deviceModel"));
    }

    @Test
    void shouldReturnThrowWhenSensorIsEmptyException() throws Exception{

        when(this.sensorController.updateSensor(eq("deviceModel"), any(UpdateSensor.class)))
                .thenThrow(new SensorIsEmptyException("Sensor not found"));

        this.mockMvc.perform(patch("/api/update-sensor/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name" : "name",
                            "deviceModel" : "deviceModel",
                            "manufacturer" : "manufacturer"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("SENSOR NOT FOUND"))
                .andExpect(jsonPath("$.message").value("Sensor not found"))
                .andExpect(jsonPath("$.path").value("/api/update-sensor/deviceModel"));
    }
}