package br.com.device_management.integration;

import br.com.device_management.dtos.DeviceManagementEventForSensor;
import br.com.device_management.dtos.UpdateDeviceDto;
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
    void shouldReturnThrowWhenRegisterDeviceIsPresent() throws Exception {

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
                .andExpect(status().isConflict());
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
    void shouldReturn400UpdateDeviceFailed() throws Exception {

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
                .andExpect(status().isConflict());
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
}
