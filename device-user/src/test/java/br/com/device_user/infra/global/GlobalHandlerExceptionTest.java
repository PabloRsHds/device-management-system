package br.com.device_user.infra.global;

import br.com.device_user.dtos.metricsDto.ExceptionMetricDto;
import br.com.device_user.infra.exceptions.ServiceUnavailableException;
import br.com.device_user.metrics.MetricsForExceptions;
import br.com.device_user.microservice.ServiceForLogin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceForLogin.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalHandlerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceForLogin serviceForLogin;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

    @Test
    void shouldReturn503AndFormattedErrorBodyEmail() throws Exception {

        when(this.serviceForLogin.getUserByEmail(any()))
                .thenThrow(new ServiceUnavailableException("Database down"));

        this.mockMvc.perform(get("/microservice/verify-by-email")
                        .param("email", "teste@gmail.com"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service unavailable"))
                .andExpect(jsonPath("$.service").value("device-user"))
                .andExpect(jsonPath("$.message").value("Database down"))
                .andExpect(jsonPath("$.path")
                        .value("/microservice/verify-by-email"));

        verify(metricsForExceptions).recordErrors(any(ExceptionMetricDto.class));
        verify(this.serviceForLogin).getUserByEmail(any());
    }

    @Test
    void shouldReturn503AndFormattedErrorBodyUserId() throws Exception {

        when(this.serviceForLogin.getUserByUserId(any()))
                .thenThrow(new ServiceUnavailableException("Database down"));

        this.mockMvc.perform(get("/microservice/verify-by-userId")
                        .param("userId", "userId"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service unavailable"))
                .andExpect(jsonPath("$.service").value("device-user"))
                .andExpect(jsonPath("$.message").value("Database down"))
                .andExpect(jsonPath("$.path")
                        .value("/microservice/verify-by-userId"));

        verify(metricsForExceptions).recordErrors(any(ExceptionMetricDto.class));
        verify(this.serviceForLogin).getUserByUserId(any());
    }
}