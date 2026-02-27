package br.com.device_management.controller;

import br.com.device_management.dtos.ResponseDeviceDto;
import br.com.device_management.dtos.register.DeviceDto;
import br.com.device_management.enums.Type;
import br.com.device_management.metrics.excepiton.MetricsForExceptions;
import br.com.device_management.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@AutoConfigureMockMvc(addFilters = false)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeviceService deviceService;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

    @Test
    void shouldReturn201WhenRegisterIsSuccess() throws Exception {

        var response = new ResponseDeviceDto(
                "temperaturer100",
                Type.TEMPERATURE_SENSOR,
                "description",
                "deviceModel",
                "manufacturer",
                "location",
                Type.TEMPERATURE_SENSOR.getUnit(),
                30f,
                150f
        );

        when(this.deviceService.registerDevice(any(DeviceDto.class)))
                .thenReturn(response);

        this.mockMvc.perform(post("/api/register-device")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                "name": "temperaturer100",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "deviceModel",
                                "manufacturer": "manufacturer",
                                "location": "location"
                            }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.type").value(response.type().name()))
                .andExpect(jsonPath("$.description").value(response.description()))
                .andExpect(jsonPath("$.deviceModel").value(response.deviceModel()))
                .andExpect(jsonPath("$.manufacturer").value(response.manufacturer()))
                .andExpect(jsonPath("$.location").value(response.location()));

    }

    // NAME VALIDATION
    @Test
    void shouldReturn400WhenTheFieldNameIsBlank() throws Exception {

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
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.source").exists())
                .andExpect(jsonPath("$.service").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void shouldReturn400WhenTheFieldNameTheSizeIsIncorrectMin() throws Exception {

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "A",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "deviceModel",
                                "manufacturer": "manufacturer",
                                "location": "location"
                            }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timesTamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.source").exists())
                .andExpect(jsonPath("$.service").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    void shouldReturn400WhenTheFieldNameTheSizeIsIncorrectMax() throws Exception {

        var name = "a".repeat(31);

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "%s",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "deviceModel",
                                "manufacturer": "manufacturer",
                                "location": "location"
                            }
                        """.formatted(name)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timesTamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.source").exists())
                .andExpect(jsonPath("$.service").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());;
    }

    // TYPE VALIDATION
    @Test
    void shouldReturn400WhenTheFieldTypeIsBlank() throws Exception {

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Temperature",
                                "type": "",
                                "description": "description",
                                "deviceModel": "deviceModel",
                                "manufacturer": "manufacturer",
                                "location": "location"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    // DESCRIPTION VALIDATION
    @Test
    void shouldReturn400WhenTheFieldDescriptionIsBlank() throws Exception {

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Temperature",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "",
                                "deviceModel": "deviceModel",
                                "manufacturer": "manufacturer",
                                "location": "location"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldDescriptionTheSizeIsIncorrectMax() throws Exception {

        var description = "a".repeat(201);

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Temperature",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "%s",
                                "deviceModel": "deviceModel",
                                "manufacturer": "manufacturer",
                                "location": "location"
                            }
                        """.formatted(description)))
                .andExpect(status().isBadRequest());
    }

    // DEVICE MODEL VALIDATION
    @Test
    void shouldReturn400WhenTheFieldDeviceModelIsBlank() throws Exception {

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Temperature",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "",
                                "manufacturer": "manufacturer",
                                "location": "location"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldDeviceModeTheSizeIsIncorrectMin() throws Exception {

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "name",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "a",
                                "manufacturer": "manufacturer",
                                "location": "location"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldDeviceModeTheSizeIsIncorrectMax() throws Exception {

        var deviceModel = "a".repeat(31);

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "name",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "%s",
                                "manufacturer": "manufacturer",
                                "location": "location"
                            }
                        """.formatted(deviceModel)))
                .andExpect(status().isBadRequest());
    }

    //MANUFACTURER
    @Test
    void shouldReturn400WhenTheFieldManufacturerIsBlank() throws Exception {

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Temperature",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "deviceModel",
                                "manufacturer": "",
                                "location": "location"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldManufacturerTheSizeIsIncorrectMin() throws Exception {

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "name",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "deviceModel",
                                "manufacturer": "a",
                                "location": "location"
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldManufacturerTheSizeIsIncorrectMax() throws Exception {

        var deviceModel = "a".repeat(31);

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "name",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "deviceModel",
                                "manufacturer": "%s",
                                "location": "location"
                            }
                        """.formatted(deviceModel)))
                .andExpect(status().isBadRequest());
    }

    // LOCATION VALIDATION
    @Test
    void shouldReturn400WhenTheFieldLocationIsBlank() throws Exception {

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "Temperature",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "deviceModel",
                                "manufacturer": "manufacturer",
                                "location": ""
                            }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldLocationTheSizeIsIncorrectMax() throws Exception {

        var location = "a".repeat(101);

        this.mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "name": "name",
                                "type": "TEMPERATURE_SENSOR",
                                "description": "description",
                                "deviceModel": "deviceModel",
                                "manufacturer": "manufacturer",
                                "location": "%s"
                            }
                        """.formatted(location)))
                .andExpect(status().isBadRequest());
    }
}