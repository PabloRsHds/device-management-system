package br.com.device_login.infra.global_exceptions;

import br.com.device_login.controller.LoginController;
import br.com.device_login.dtos.loginDto.RequestLoginDto;
import br.com.device_login.dtos.tokenDto.ResponseTokens;
import br.com.device_login.infra.exceptions.InvalidCredentialsException;
import br.com.device_login.infra.exceptions.ServiceUnavailableException;
import br.com.device_login.metrics.exception.MetricsForExceptions;
import br.com.device_login.service.LoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginController loginController;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

    @Test
    void shouldReturn401InvalidCredentialsException() throws Exception {

        when(this.loginController.login(any(RequestLoginDto.class)))
                .thenThrow(new InvalidCredentialsException("Email or Password is incorrect"));

        this.mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "email":"teste@gmail.com",
                            "password":"99218841Pp@"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timesTamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Invalid or expired credentials"))
                .andExpect(jsonPath("$.source").value("DEVICE-LOGIN"))
                .andExpect(jsonPath("$.target").value("USER-DEVICE"))
                .andExpect(jsonPath("$.service").value("device-login"))
                .andExpect(jsonPath("$.message").value("Email or Password is incorrect"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }

    @Test
    void shouldReturn503ServiceUnavailableException() throws Exception {

        when(this.loginController.login(any(RequestLoginDto.class)))
                .thenThrow(new ServiceUnavailableException("Service unavailable, try later again"));

        this.mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "email":"teste@gmail.com",
                            "password":"99218841Pp@"
                        }
                        """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.timesTamp").exists())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service unavailable"))
                .andExpect(jsonPath("$.source").value("DEVICE-LOGIN"))
                .andExpect(jsonPath("$.target").value("USER-DEVICE"))
                .andExpect(jsonPath("$.service").value("device-login"))
                .andExpect(jsonPath("$.message").value("Service unavailable, try later again"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }
}