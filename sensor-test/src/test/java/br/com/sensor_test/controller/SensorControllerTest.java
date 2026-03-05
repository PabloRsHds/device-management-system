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
import static org.mockito.Mockito.*;
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

        var request = new UpdateSensor("newName", "", "");

        when(this.sensorService.updateSensor("deviceModel",request))
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

        verify(this.sensorService).updateSensor("deviceModel", request);
        verifyNoInteractions(this.sensorRepository);
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

        verify(this.sensorService).updateSensor(eq("deviceModel"), any(UpdateSensor.class));
        verifyNoInteractions(this.sensorRepository);
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

    @Test
    void shouldReturnThrowWhenDeleteSensor() throws Exception {

        when(this.sensorService.deleteSensor("deviceModel"))
                .thenThrow(new SensorIsEmptyException("Sensor is empty"));

        this.mockMvc.perform(delete("/api/delete-sensor/{deviceModel}", "deviceModel"))
                .andExpect(status().isConflict());
    }

    // ================================================================================================================

    // ====================================== FIND ALL SENSORS ACTIVATED TEST =========================================

    @Test
    void shouldReturnListResponseSensorDtoWhenFindAllSensorsActivated() throws Exception {

        when(this.sensorService.getAllSensorsActivated(0, 1))
                .thenReturn(List.of());

        this.mockMvc.perform(get("/api/find-all-sensors-activated")
                        .param("page","0")
                        .param("size", "1"))
                .andExpect(status().isOk());
    }

    // ================================================================================================================

    // ============================================= GET STATUS TEST ==================================================

    @Test
    void shouldReturnStringWhenGetStatus() throws Exception {

        when(this.sensorService.getStatus("deviceModel"))
                .thenReturn(anyString());

        this.mockMvc.perform(get("/api/get-status/{deviceModel}", "deviceModel"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnThrowWhenGetStatus() throws Exception {

        when(this.sensorService.getStatus("deviceModel"))
                .thenThrow(new SensorIsEmptyException("Sensor is empty"));

        this.mockMvc.perform(get("/api/get-status/{deviceModel}", "deviceModel"))
                .andExpect(status().isConflict());
    }

    //================================================================================================================

    // ========================================== CHANGE STATUS TEST =================================================

    @Test
    void shouldReturnResponseSensorDtoWhenChangeStatus() throws Exception{

        when(this.sensorService.changeStatus("deviceModel"))
                .thenReturn(any(ResponseSensorDto.class));

        this.mockMvc.perform(patch("/api/change-status/{deviceModel}", "deviceModel"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnThrowWhenChangeStatus() throws Exception{

        when(this.sensorService.changeStatus("deviceModel"))
                .thenThrow(new SensorIsEmptyException("Sensor is empty"));

        this.mockMvc.perform(patch("/api/change-status/{deviceModel}", "deviceModel"))
                .andExpect(status().isConflict());
    }
}