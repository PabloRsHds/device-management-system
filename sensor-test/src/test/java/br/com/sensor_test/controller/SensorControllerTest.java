package br.com.sensor_test.controller;

import br.com.sensor_test.dtos.UpdateSensor;
import br.com.sensor_test.dtos.sensor.ResponseSensorDto;
import br.com.sensor_test.enums.Status;
import br.com.sensor_test.infra.exceptions.SensorIsEmptyException;
import br.com.sensor_test.metrics.MetricsForExceptions;
import br.com.sensor_test.repository.SensorRepository;
import br.com.sensor_test.service.SensorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorController.class)
@AutoConfigureMockMvc(addFilters = false)
class SensorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

    @MockitoBean
    private SensorService sensorService;

    @MockitoBean
    private SensorRepository sensorRepository;


    // =============================================== UPDATE TEST ===================================================
    @Test
    void shouldReturnResponseSensorDtoWhenUpdateSensor() throws Exception{

        var response = new ResponseSensorDto(
                "name",
                "type",
                "deviceModel",
                "manufacturer",
                Status.ACTIVATED
        );

        when(this.sensorService.updateSensor("deviceModel",
                new UpdateSensor("newName", "", "")))
                .thenReturn(response);

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
    void shouldReturnThrowWhenUpdateSensor() throws Exception{

        when(this.sensorService.updateSensor(
                eq("deviceModel"),
                any(UpdateSensor.class)))
                .thenThrow(new SensorIsEmptyException("Sensor not found"));

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

    // =========================================== DELETE TEST =======================================================
    @Test
    void shouldReturnResponseSensorDtoWhenDeleteSensor() throws Exception {

        when(this.sensorService.deleteSensor("deviceModel"))
                .thenReturn(any(ResponseSensorDto.class));

        this.mockMvc.perform(delete("/api/delete-sensor/{deviceModel}", "deviceModel"))
                .andExpect(status().isOk());
    }


}