package br.com.device_login.service;

import br.com.device_login.dtos.loginDto.ResponseUserForLogin;
import br.com.device_login.dtos.tokenDto.RequestTokensDto;
import br.com.device_login.dtos.tokenDto.ResponseTokens;
import br.com.device_login.infra.exceptions.InvalidCredentialsException;
import br.com.device_login.metrics.login.LoginMetrics;
import br.com.device_login.microservice.UserClient;
import io.micrometer.core.instrument.Timer;
import org.bouncycastle.pqc.crypto.util.PQCOtherInfoGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private LoginMetrics loginMetrics;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @InjectMocks
    private LoginService loginService;

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        var email = "test@gmail.com";
        var password = "3211123123";
        var sample = mock(Timer.Sample.class);

        when(this.userClient.getResponseUserWithEmailOrUserId(email, null))
                .thenReturn(null);

        assertThrows(InvalidCredentialsException.class,
                () -> this.loginService.verifyUser(email, password, sample));

        verify(this.userClient).getResponseUserWithEmailOrUserId(email, null);
        verify(this.loginMetrics).userNotFound();
        verify(this.loginMetrics).stopFailedLoginTimer(sample);

        verifyNoInteractions(this.passwordEncoder);
        verifyNoInteractions(this.jwtEncoder);
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsIncorrect() {

        var email = "test@gmail.com";

        var userId = "321";
        var rawPassword = "12345678";
        var encodePassword = "99218841";
        var role = "USER";

        var sample = mock(Timer.Sample.class);

        var response = new ResponseUserForLogin(userId, encodePassword, role);

        when(this.userClient.getResponseUserWithEmailOrUserId(email, null)).thenReturn(response);
        when(this.passwordEncoder.matches(rawPassword, encodePassword)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> this.loginService.verifyUser(email, rawPassword, sample));

        verify(this.userClient).getResponseUserWithEmailOrUserId(email, null);
        verify(this.passwordEncoder).matches(rawPassword, encodePassword);
        verify(this.loginMetrics).invalidCredentials();
        verify(this.loginMetrics).stopFailedLoginTimer(sample);

        verifyNoInteractions(this.jwtEncoder);
    }

    @Test
    void shouldReturnUserWhenEmailExists() {

        var email = "test@gmail.com";

        var rawPassword = "12345678";
        var encodePassword = "12345678";
        var userId = "123";
        var role = "USER";

        var sample = mock(Timer.Sample.class);
        var response = new ResponseUserForLogin(userId, encodePassword, role);

        when(this.userClient.getResponseUserWithEmailOrUserId(email, null)).thenReturn(response);
        when(this.passwordEncoder.matches(rawPassword, encodePassword)).thenReturn(true);

        var success = this.loginService.verifyUser(email, rawPassword, sample);

        assertNotNull(success);
        assertEquals(userId, success.userId());
        assertEquals(encodePassword, success.password());
        assertEquals(role, success.role());

        verify(this.userClient).getResponseUserWithEmailOrUserId(email, null);
        verify(this.passwordEncoder).matches(rawPassword, encodePassword);

        verifyNoInteractions(this.loginMetrics);
        verifyNoInteractions(this.jwtEncoder);
    }

    @Test
    void shouldGenerateAccessAndRefreshTokens() {

        var userId = "123";
        var role = "USER";

        var accessJwt = mock(Jwt.class);
        var refreshJwt = mock(Jwt.class);

        when(accessJwt.getTokenValue()).thenReturn("access-token");
        when(refreshJwt.getTokenValue()).thenReturn("refresh-token");

        when(this.jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(accessJwt, refreshJwt);

        var response = loginService.generateTokens(userId, role);

        assertNotNull(response);
        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());

        verifyNoInteractions(this.userClient);
        verifyNoInteractions(this.passwordEncoder);
        verifyNoInteractions(this.loginMetrics);
    }

    @Test
    void shouldNotGenerateAccessAndRefreshTokens() {

        var userId = "123";
        var role = "USER";

        var accessToken = mock(Jwt.class);
        var refreshToken = mock(Jwt.class);

        when(accessToken.getTokenValue()).thenReturn(null);
        when(refreshToken.getTokenValue()).thenReturn(null);

        when(this.jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(accessToken, refreshToken);

        assertThrows(JwtEncodingException.class,
                () -> this.loginService.generateTokens(userId, role));

        verifyNoInteractions(this.userClient);
        verifyNoInteractions(this.passwordEncoder);
        verifyNoInteractions(this.loginMetrics);
    }

    @Test
    void shouldThrowBecauseRefreshTokenIsExpired() {

        var sample = mock(Timer.Sample.class);
        var accessToken = mock(Jwt.class);
        var refreshToken = mock(Jwt.class);

        when(this.loginMetrics.startTimer()).thenReturn(sample);
        when(this.jwtDecoder.decode("access-token")).thenReturn(accessToken);
        when(this.jwtDecoder.decode("refresh-token")).thenReturn(refreshToken);

        when(refreshToken.getExpiresAt()).thenReturn(null);

        assertThrows(InvalidCredentialsException.class,
                () -> this.loginService.refreshTokens(new RequestTokensDto("access-token", "refresh-token")));

        verify(this.loginMetrics).failedRefreshTokens();
        verify(this.loginMetrics).stopFailedRefreshTokensTimer(sample);

        verifyNoInteractions(this.passwordEncoder);
        verifyNoInteractions(this.userClient);
        verifyNoInteractions(this.jwtEncoder);
    }

    @Test
    void shouldThrowBecauseSubjectIsInvalid() {

        var sample = mock(Timer.Sample.class);
        var accessToken = mock(Jwt.class);
        var refreshToken = mock(Jwt.class);

        when(this.loginMetrics.startTimer()).thenReturn(sample);
        when(this.jwtDecoder.decode("access-token")).thenReturn(accessToken);
        when(this.jwtDecoder.decode("refresh-token")).thenReturn(refreshToken);
        when(refreshToken.getExpiresAt()).thenReturn(Instant.now().plusSeconds(60));

        when(accessToken.getSubject()).thenReturn("user-1");
        when(refreshToken.getSubject()).thenReturn("user-2");

        assertThrows(InvalidCredentialsException.class,
                () -> this.loginService.refreshTokens(new RequestTokensDto("access-token", "refresh-token")));

        verify(this.loginMetrics).failedRefreshTokens();
        verify(this.loginMetrics).stopFailedRefreshTokensTimer(sample);

        verifyNoInteractions(this.passwordEncoder);
        verifyNoInteractions(this.userClient);
        verifyNoInteractions(this.jwtEncoder);
    }
}