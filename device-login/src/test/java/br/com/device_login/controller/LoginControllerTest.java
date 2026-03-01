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
import org.springframework.test.web.servlet.ResultActions;

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

    private ResultActions expectDefaultErrorStructure(ResultActions result) throws Exception {
        return result
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.source").exists())
                .andExpect(jsonPath("$.target").exists())
                .andExpect(jsonPath("$.service").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists())

                .andExpect(jsonPath("$.source").value("DEVICE-LOGIN"))
                .andExpect(jsonPath("$.target").value("USER-DEVICE"))
                .andExpect(jsonPath("$.service").value("device-login"));
    }

    // =============================================== TEST LOGIN =====================================================
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
    void shouldReturn401WhenUserLogInIsFailed() throws Exception{

        when(this.loginService.login(any()))
                .thenThrow(new InvalidCredentialsException("User unauthorized"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "teste@gmail.com",
                  "password": "99218841Pp@"
                }
            """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Invalid or expired credentials"))
                .andExpect(jsonPath("$.message").value("User unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }

    @Test
    void shouldReturn503WhenMicroserviceUserIsDown() throws Exception{

        when(this.loginService.login(any()))
                .thenThrow(new ServiceUnavailableException("Service unavailable, try later again"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "teste@gmail.com",
                  "password": "99218841Pp@"
                }
            """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("Service unavailable"))
                .andExpect(jsonPath("$.message").value("Service unavailable, try later again"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }
    // ===============================================================================================================

    // ======================================== VALIDATIONS TEST =====================================================

    // EMAIL VALIDATION
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation incorrect"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }

    @Test
    void shouldReturn400WhenFieldEmailTheSizeIsIncorrectMin() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "A",
                  "password": "99218841Pp@"
                }
            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation incorrect"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }

    @Test
    void shouldReturn400WhenFieldEmailTheSizeIsIncorrectMax() throws Exception{

        var max = "a".repeat(61);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "%s",
                  "password": "99218841Pp@"
                }
            """.formatted(max)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation incorrect"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }

    @Test
    void shouldReturn400WhenFieldEmailThePatternIsIncorrect() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "pablo@gmailcom",
                  "password": "99218841Pp@"
                }
            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation incorrect"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }

    // PASSWORD
    @Test
    void shouldReturn400WhenFieldPasswordTheSizeIsIncorrectMin() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "pablo@gmail.com",
                  "password": "A"
                }
            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation incorrect"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }

    @Test
    void shouldReturn400WhenFieldPasswordTheSizeIsIncorrectMax() throws Exception{

        var password = "a".repeat(31);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "pablo@gmail.com",
                  "password": "%s"
                }
            """.formatted(password)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation incorrect"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }

    @Test
    void shouldReturn400WhenFieldPasswordThePatternIsIncorrect() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "pablo@gmail.com",
                  "password": "123456789"
                }
            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation incorrect"))
                .andExpect(jsonPath("$.path").value("/api/login"));
    }

    // ===================================== REFRESH TOKENS TEST ======================================================
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
                .thenThrow(new InvalidCredentialsException("Invalid or expired refresh token"));

        mockMvc.perform(post("/api/refresh-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "accessToken": "123",
                            "refreshToken": "321"
                        }
                        """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Invalid or expired credentials"))
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"))
                .andExpect(jsonPath("$.path").value("/api/refresh-tokens"));
    }
}