package br.com.device_management.infra.global;

import br.com.device_management.controller.DeviceController;
import br.com.device_management.metrics.excepiton.MetricsForExceptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

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
                .andExpect(status().isBadRequest());
    }
}