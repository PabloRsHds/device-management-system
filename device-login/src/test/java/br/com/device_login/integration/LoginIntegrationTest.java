package br.com.device_login.integration;

import br.com.device_login.dtos.loginDto.ResponseUserForLogin;
import br.com.device_login.dtos.tokenDto.RequestTokensDto;
import br.com.device_login.microservice.UserClient;
import br.com.device_login.service.LoginService;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient;

    @Autowired
    private LoginService loginService;

    @Test
    void shouldLoginSuccessfully() throws Exception {

        var encodePassword = new BCryptPasswordEncoder().encode("99218841Pp@");

        when(this.userClient.getResponseUserWithEmailOrUserId("teste@gmail.com",null))
                .thenReturn(new ResponseUserForLogin("123", encodePassword, "USER"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "teste@gmail.com",
                  "password": "99218841Pp@"
                }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void shouldLoginFailed() throws Exception {

        when(this.userClient.getResponseUserWithEmailOrUserId("teste@gmail.com", null))
                .thenReturn(null);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "teste@gmail.com",
                  "password": "99218841Pp@"
                }
                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldLoginFailedBecausePasswordIsIncorrect() throws Exception {

        var encodePassword = new BCryptPasswordEncoder().encode("99218841Pp");

        when(this.userClient.getResponseUserWithEmailOrUserId("teste@gmail.com",null))
                .thenReturn(new ResponseUserForLogin("123", encodePassword, "USER"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "teste@gmail.com",
                  "password": "99218841Pp@"
                }
                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn503WhenExternalServiceFails() throws Exception {

        when(this.userClient.getResponseUserWithEmailOrUserId(any(), any()))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "email": "teste@gmail.com",
                  "password": "99218841Pp@"
                }
                """))
                .andExpect(status().isServiceUnavailable());
    }

    // REFRESH TOKEN

    @Test
    void shouldReturnRefreshTokensWithSuccess() throws Exception {

        var generated = this.loginService.generateTokens("123", "USER");

        mockMvc.perform(post("/api/refresh-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "accessToken" : "%s",
                        "refreshToken" : "%s"
                    }
                    """.formatted(
                                generated.accessToken(),
                                generated.refreshToken()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
}
