package br.com.device_login.controller;


import br.com.device_login.dtos.loginDto.RequestLoginDto;
import br.com.device_login.dtos.tokenDto.RequestTokensDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

    @Test
    void shouldReturn200WhenUserLogInWithSuccess() throws Exception {

        var response = new ResponseTokens("access-token", "refresh-token");

        when(this.loginService.login(any(RequestLoginDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "teste@gmail.com",
                  "password": "99218841Pp@"
                }
            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void shouldReturn400WhenFieldEmailIsBlank() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "",
                  "password": "99218841Pp@"
                }
            """))
                .andExpect(status().isBadRequest());
    }



    @Test
    void shouldReturn401WhenUserLogInIsFailed() throws Exception{

        when(this.loginService.login(any()))
                .thenThrow(InvalidCredentialsException.class);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "teste@gmail.com",
                  "password": "99218841Pp@"
                }
            """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturn503WhenMicroserviceUserIsDown() throws Exception{

        when(this.loginService.login(any()))
                .thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "teste@gmail.com",
                  "password": "99218841Pp@"
                }
            """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void shouldReturn200WhenGeneratedNewTokens() throws Exception{

        var response = new ResponseTokens("access-token", "refresh-token");

        when(this.loginService.refreshTokens(any(RequestTokensDto.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/refresh-tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "accessToken": "123",
                            "refreshToken": "321"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void shouldReturn401WhenFailedGeneratedNewTokens() throws Exception{

        when(this.loginService.refreshTokens(any(RequestTokensDto.class)))
                .thenThrow(InvalidCredentialsException.class);

        mockMvc.perform(post("/api/refresh-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "accessToken": "123",
                            "refreshToken": "321"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}