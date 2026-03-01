package br.com.device_management.infra.global;

import br.com.device_management.controller.DeviceController;
import br.com.device_management.dtos.register.DeviceDto;
import br.com.device_management.infra.exceptions.ServiceUnavailable;
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
                .andExpect(jsonPath("$.path").value("/api/register-device"));;
    }


}