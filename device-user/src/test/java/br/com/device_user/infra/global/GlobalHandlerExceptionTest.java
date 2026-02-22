package br.com.device_user.infra.global;

import br.com.device_user.dtos.metricsDto.ExceptionMetricDto;
import br.com.device_user.infra.exceptions.ServiceUnavailableException;
import br.com.device_user.metrics.MetricsForExceptions;
import br.com.device_user.service.user_service.UserService;
import br.com.device_user.microservice.ServiceForLogin;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceForLogin.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalHandlerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

    @Test
    void shouldReturn503AndFormattedErrorBody() throws Exception {

        when(this.userService.getResponseUserWithEmailOrUserId(any(), any()))
                .thenThrow(new ServiceUnavailableException("Database down"));

        this.mockMvc.perform(get("/microservice/verify-if-email-already-cadastred")
                        .param("email", "teste@gmail.com")
                        .param("userId", "123"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("service unavailable"))
                .andExpect(jsonPath("$.service").value("device_user"))
                .andExpect(jsonPath("$.message").value("Database down"))
                .andExpect(jsonPath("$.path")
                        .value("/microservice/verify-if-email-already-cadastred"));

        verify(metricsForExceptions).recordErrors(any(ExceptionMetricDto.class));
        verify(this.userService).getResponseUserWithEmailOrUserId(any(), any());
    }
}