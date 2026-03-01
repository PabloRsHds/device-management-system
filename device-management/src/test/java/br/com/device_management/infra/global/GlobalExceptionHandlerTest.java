package br.com.device_management.infra.global;

import br.com.device_management.controller.DeviceController;
import br.com.device_management.dtos.UpdateDeviceDto;
import br.com.device_management.dtos.register.DeviceDto;
import br.com.device_management.infra.exceptions.DeviceIsEmpty;
import br.com.device_management.infra.exceptions.DeviceIsPresent;
import br.com.device_management.infra.exceptions.ServiceUnavailable;
import br.com.device_management.metrics.excepiton.MetricsForExceptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

    @MockitoBean
    private DeviceController deviceController;

    @Test
    void shouldReturn400MethodArgumentNotValidException() throws Exception{

        this.mockMvc.perform(post("/api/register-device")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "",
                            "type": "TEMPERATURE_SENSOR",
                            "description": "description",
                            "deviceModel": "deviceModel",
                            "manufacturer": "manufacturer",
                            "location": "location"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timesTamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.source").value("DEVICE-MANAGEMENT"))
                .andExpect(jsonPath("$.service").value("device-management"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/register-device"));
    }

    @Test
    void shouldReturn503MethodServiceUnavailable() throws Exception{

        when(this.deviceController.registerDevice(any(DeviceDto.class)))
                .thenThrow(new ServiceUnavailable("Service unavailable, try again later"));

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "name",
                            "type": "TEMPERATURE_SENSOR",
                            "description": "description",
                            "deviceModel": "deviceModel",
                            "manufacturer": "manufacturer",
                            "location": "location"
                        }
                        """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.timesTamp").exists())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service unavailable"))
                .andExpect(jsonPath("$.source").value("DEVICE-MANAGEMENT"))
                .andExpect(jsonPath("$.service").value("device-management"))
                .andExpect(jsonPath("$.message").value("Service unavailable, try again later"))
                .andExpect(jsonPath("$.path").value("/api/register-device"));
    }

    @Test
    void shouldReturn409DeviceIsEmpty() throws Exception{

        when(this.deviceController.updateDevice(eq("deviceModel"),any(UpdateDeviceDto.class)))
                .thenThrow(new DeviceIsEmpty("This device model is not registered in the database"));

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName": "name",
                            "newDeviceModel": "deviceModel",
                            "newManufacturer": "manufacturer",
                            "newLocation": "location",
                            "newDescription": "description"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timesTamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Device not found"))
                .andExpect(jsonPath("$.source").value("DEVICE-MANAGEMENT"))
                .andExpect(jsonPath("$.service").value("device-management"))
                .andExpect(jsonPath("$.message").value("This device model is not registered in the database"))
                .andExpect(jsonPath("$.path").value("/api/update-device/deviceModel"));
    }

    @Test
    void shouldReturn409DeviceIsPresent() throws Exception{

        when(this.deviceController.registerDevice(any(DeviceDto.class)))
                .thenThrow(new DeviceIsPresent("This device model is already registered in the database"));

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "name": "name",
                            "type": "TEMPERATURE_SENSOR",
                            "description": "description",
                            "deviceModel": "deviceModel",
                            "manufacturer": "manufacturer",
                            "location": "location"
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timesTamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Device already cadastred"))
                .andExpect(jsonPath("$.source").value("DEVICE-MANAGEMENT"))
                .andExpect(jsonPath("$.service").value("device-management"))
                .andExpect(jsonPath("$.message").value("This device model is already registered in the database"))
                .andExpect(jsonPath("$.path").value("/api/register-device"));
    }
}