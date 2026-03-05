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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @Test
    void shouldReturnThrowWhenServiceUnavailableException() throws Exception{

        when(this.sensorController.updateSensor(eq("deviceModel"), any(UpdateSensor.class)))
                .thenThrow(new ServiceUnavailableException("Service unavailable."));

        this.mockMvc.perform(patch("/api/update-sensor/{deviceModel}", "deviceModel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name" : "name",
                            "deviceModel" : "deviceModel",
                            "manufacturer" : "manufacturer"
                        }
                        """))
                .andExpect(status().isServiceUnavailable());
    }
}