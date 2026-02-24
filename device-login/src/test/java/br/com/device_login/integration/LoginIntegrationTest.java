package br.com.device_login.integration;

import br.com.device_login.dtos.loginDto.ResponseUserForLogin;
import br.com.device_login.microservice.UserClient;
import br.com.device_login.service.LoginService;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void shouldLoginSuccessfully() throws Exception {

        when(this.userClient.getResponseUserWithEmailOrUserId("teste@gmail.com",null))
                .thenReturn(new ResponseUserForLogin("123", new BCryptPasswordEncoder().encode("99218841Pp@"), "USER"));

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

        // Gerei o token usando um metodo do loginService, para que eu enviasse um token válido
        var tokens = this.loginService.generateTokens("123", "USER");

        // Mockei o userClient para retornar um usuário válido para a criação do refresh token.
        when(this.userClient.getResponseUserWithEmailOrUserId(null, "123"))
                .thenReturn(new ResponseUserForLogin("123", "99218841Pp@", "USER"));

        mockMvc.perform(post("/api/refresh-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "accessToken" : "%s",
                        "refreshToken" : "%s"
                    }
                    """.formatted(
                                tokens.accessToken(),
                                tokens.refreshToken()
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void shouldReturnUnauthorizedWhenRefreshTokenAreExpired() throws Exception {

        var accessToken = this.loginService.generateTokens("123", "USER").accessToken();
        var refreshToken = this.generateExpiredRefreshToken("123", "USER");

        mockMvc.perform(post("/api/refresh-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                "accessToken" : "%s",
                "refreshToken" : "%s"
            }
            """.formatted(accessToken, refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    private String generateExpiredRefreshToken(String subject, String role) {

        var claims = JwtClaimsSet.builder()
                .subject(subject)
                .expiresAt(Instant.now().minusSeconds(10))
                .claim("scope", role)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims))
                .getTokenValue();
    }

    @Test
    void shouldReturnUnauthorizedWhenSubjectsAreDifferent() throws Exception {

        var accessToken = this.loginService.generateTokens("123", "USER").accessToken();
        var refreshToken = this.loginService.generateTokens("456", "USER").refreshToken();

        mockMvc.perform(post("/api/refresh-tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
            {
                "accessToken" : "%s",
                "refreshToken" : "%s"
            }
            """.formatted(accessToken, refreshToken)))
                .andExpect(status().isUnauthorized());
    }
}
