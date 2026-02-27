package br.com.device_management.controller;

import br.com.device_management.dtos.ResponseDeviceDto;
import br.com.device_management.dtos.UpdateDeviceDto;
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
import org.springframework.test.web.servlet.ResultActions;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    private ResultActions expectDefaultErrorStructure(ResultActions result) throws Exception {
        return result
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.source").exists())
                .andExpect(jsonPath("$.service").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    // ===================================== REGISTER ================================================================

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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
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

    // ================================================================================================================

    // ================================================== UPDATE ======================================================

    @Test
    void shouldReturn200WhenUpdateDevice() throws Exception{

        var response = new ResponseDeviceDto(
                "name",
                Type.TEMPERATURE_SENSOR,
                "description",
                "deviceModel",
                "manufacturer",
                "location",
                Type.TEMPERATURE_SENSOR.getUnit(),
                Type.TEMPERATURE_SENSOR.getMin(),
                Type.TEMPERATURE_SENSOR.getMax()
        );

        when(this.deviceService.updateDevice(eq("deviceModel"), any(UpdateDeviceDto.class)))
                .thenReturn(response);

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "location",
                            "newDescription" : "description"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.type").value(response.type().name()))
                .andExpect(jsonPath("$.description").value(response.description()))
                .andExpect(jsonPath("$.deviceModel").value(response.deviceModel()))
                .andExpect(jsonPath("$.manufacturer").value(response.manufacturer()))
                .andExpect(jsonPath("$.location").value(response.location()))
                .andExpect(jsonPath("$.unit").value(response.unit().name()))
                .andExpect(jsonPath("$.minLimit").value(response.type().getMin()))
                .andExpect(jsonPath("$.maxLimit").value(response.type().getMax()));
    }

    // NEW NAME VALIDATION
    @Test
    void shouldReturn400BecauseTheFieldNewNameIsBlank() throws Exception{

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "newName" : "",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "location",
                            "newDescription" : "description"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldNewNameTheSizeIsIncorrectMin() throws Exception{

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "a",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "location",
                            "newDescription" : "description"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldNewNameTheSizeIsIncorrectMax() throws Exception{

        var newName = "a".repeat(31);

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "%s",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "location",
                            "newDescription" : "description"
                        }
                        """.formatted(newName)))
                .andExpect(status().isBadRequest());
    }

    // NEW DEVICE MODEL VALIDATION

    @Test
    void shouldReturn400BecauseTheFieldNewDeviceModelIsBlank() throws Exception {

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "location",
                            "newDescription" : "description"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldNewDeviceModelTheSizeIsIncorrectMin() throws Exception{

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "a",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "location",
                            "newDescription" : "description"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldNewDeviceModelTheSizeIsIncorrectMax() throws Exception{

        var newDeviceModel = "a".repeat(31);

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "%s",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "location",
                            "newDescription" : "description"
                        }
                        """.formatted(newDeviceModel)))
                .andExpect(status().isBadRequest());
    }

    // NEW MANUFACTURER VALIDATION

    @Test
    void shouldReturn400BecauseTheFieldNewManufacturerIsBlank() throws Exception {

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "",
                            "newLocation" : "location",
                            "newDescription" : "description"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldNewManufacturerTheSizeIsIncorrectMin() throws Exception{

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "a",
                            "newLocation" : "location",
                            "newDescription" : "description"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldNewManufacturerTheSizeIsIncorrectMax() throws Exception{

        var newManufacturer = "a".repeat(31);

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "%s",
                            "newLocation" : "location",
                            "newDescription" : "description"
                        }
                        """.formatted(newManufacturer)))
                .andExpect(status().isBadRequest());
    }

    // NEW LOCATION VALIDATION

    @Test
    void shouldReturn400BecauseTheFieldNewLocationIsBlank() throws Exception {

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "",
                            "newDescription" : "description"
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldNewLocationTheSizeIsIncorrectMax() throws Exception{

        var newLocation = "a".repeat(101);

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "%s",
                            "newDescription" : "description"
                        }
                        """.formatted(newLocation)))
                .andExpect(status().isBadRequest());
    }

    // NEW DESCRIPTION VALIDATION

    @Test
    void shouldReturn400BecauseTheFieldNewDescriptionIsBlank() throws Exception {

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "location",
                            "newDescription" : ""
                        }
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTheFieldNewDescriptionTheSizeIsIncorrectMax() throws Exception{

        var newDescription = "a".repeat(201);

        this.mockMvc.perform(patch("/api/update-device/{deviceModel}", "deviceModel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "newName" : "name",
                            "newDeviceModel" : "deviceModel",
                            "newManufacturer" : "manufacturer",
                            "newLocation" : "%s",
                            "newDescription" : "description"
                        }
                        """.formatted(newDescription)))
                .andExpect(status().isBadRequest());
    }
}