package br.com.device_management.integration;

import br.com.device_management.dtos.DeviceManagementEventForSensor;
import br.com.device_management.enums.Type;
import br.com.device_management.model.Device;
import br.com.device_management.repository.DeviceRepository;
import br.com.device_management.service.DeviceService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class DeviceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private DeviceService deviceService;

    @MockitoBean
    private KafkaTemplate<String, DeviceManagementEventForSensor> kafkaTemplate;

    private ResultActions expectDefaultErrorStructure(ResultActions result) throws Exception {
        return result
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.source").exists())
                .andExpect(jsonPath("$.service").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists())

                .andExpect(jsonPath("$.source").value("DEVICE-MANAGEMENT"))
                .andExpect(jsonPath("$.service").value("device-management"));
    }

    // =========================================== REGISTER DEVICE ====================================================
    @Test
    void shouldReturn200RegisterDeviceSuccessfully() throws Exception {

        mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name" : "name",
                        "type" : "TEMPERATURE_SENSOR",
                        "description" : "description",
                        "deviceModel" : "deviceModel",
                        "manufacturer" : "manufacturer",
                        "location" : "location"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.type").value("TEMPERATURE_SENSOR"))
                .andExpect(jsonPath("$.description").value("description"))
                .andExpect(jsonPath("$.deviceModel").value("deviceModel"))
                .andExpect(jsonPath("$.manufacturer").value("manufacturer"))
                .andExpect(jsonPath("$.location").value("location"));
    }

    @Test
    void shouldReturn409WhenRegisterDeviceFailed() throws Exception {

        var device = new Device();
        device.setName("name");
        device.setType(Type.TEMPERATURE_SENSOR);
        device.setDescription("description");
        device.setDeviceModel("deviceModel");
        device.setManufacturer("manufacturer");
        device.setLocation("location");
        this.deviceRepository.save(device);

        this.deviceRepository.findByDeviceModel("deviceModel");

        mockMvc.perform(post("/api/register-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "name" : "name",
                        "type" : "TEMPERATURE_SENSOR",
                        "description" : "description",
                        "deviceModel" : "deviceModel",
                        "manufacturer" : "manufacturer",
                        "location" : "location"
                    }
                    """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Device already cadastred"))
                .andExpect(jsonPath("$.path").value("/api/register-device"));
    }
    // ================================================================================================================

    // ================================================ UPDATE DEVICE =================================================

    @Test
    void shouldReturn200UpdateDeviceSuccessfully() throws Exception {

        var device = new Device();
        device.setName("name");
        device.setType(Type.TEMPERATURE_SENSOR);
        device.setDescription("description");
        device.setDeviceModel("deviceModel");
        device.setManufacturer("manufacturer");
        device.setLocation("location");
        this.deviceRepository.save(device);

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
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn409UpdateDeviceFailed() throws Exception {

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
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Device not found"))
                .andExpect(jsonPath("$.path").value("/api/update-device/deviceModel"));
    }

    // ===============================================================================================================

    // ============================================== DELETE DEVICE ==================================================

    @Test
    void shouldReturn200DeleteDeviceSuccessfully() throws Exception{

        var device = new Device();
        device.setName("name");
        device.setType(Type.TEMPERATURE_SENSOR);
        device.setDescription("description");
        device.setDeviceModel("deviceModel");
        device.setManufacturer("manufacturer");
        device.setLocation("location");
        this.deviceRepository.save(device);

        this.mockMvc.perform(delete("/api/delete-device/{deviceModel}", "deviceModel"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn409DeleteDeviceFailed() throws Exception{

        this.mockMvc.perform(delete("/api/delete-device/{deviceModel}", "deviceModel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Device not found"))
                .andExpect(jsonPath("$.path").value("/api/delete-device/deviceModel"));
    }

    // ===============================================================================================================

    // =============================================== GET ALL DEVICES ===============================================

    @Test
    void shouldReturn200AllDevices() throws Exception{

        this.mockMvc.perform(get("/api/all-devices")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk());
    }

    // ===============================================================================================================

    // =================================== GET DEVICE WITH DEVICE MODEL ==============================================

    @Test
    void shouldReturn200GetDeviceWithDeviceModelSuccessfully() throws Exception{

        var device = new Device();
        device.setName("name");
        device.setType(Type.TEMPERATURE_SENSOR);
        device.setDescription("description");
        device.setDeviceModel("deviceModel");
        device.setManufacturer("manufacturer");
        device.setLocation("location");
        this.deviceRepository.save(device);

        this.mockMvc.perform(get("/api/find-by-device/{deviceModel}", "deviceModel"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn200GetDeviceWithDeviceModelFailed() throws Exception{

        this.mockMvc.perform(get("/api/find-by-device/{deviceModel}", "deviceModel"))
                .andExpect(status().isConflict());
    }
}
