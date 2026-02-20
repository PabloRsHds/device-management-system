package br.com.device_login.service;

import br.com.device_login.dtos.loginDto.ResponseUserForLogin;
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

    @InjectMocks
    private LoginService loginService;

    @Test
    public void shouldThrowExceptionWhenUserNotFound() {

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
    public void shouldThrowExceptionWhenPasswordIsIncorrect() {

        var email = "test@gmail.com";

        var userId = "321";
        var password = "12345678";
        var role = "USER";

        var sample = mock(Timer.Sample.class);

        var response = new ResponseUserForLogin(userId, password, role);

        when(this.userClient.getResponseUserWithEmailOrUserId(email, null)).thenReturn(response);
        when(this.passwordEncoder.matches(password, response.password())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> this.loginService.verifyUser(email, password, sample));

        verify(this.userClient).getResponseUserWithEmailOrUserId(email, null);
        verify(this.passwordEncoder).matches(password, response.password());
        verify(this.loginMetrics).invalidCredentials();
        verify(this.loginMetrics).stopFailedLoginTimer(sample);

        verifyNoInteractions(this.jwtEncoder);
    }

    @Test
    public void shouldReturnUserWhenEmailExists() {

        var email = "test@gmail.com";

        var password = "12345678";
        var encodePassword = "12345678";
        var userId = "123";
        var role = "USER";

        var sample = mock(Timer.Sample.class);
        var response = new ResponseUserForLogin(userId, password, role);

        when(this.userClient.getResponseUserWithEmailOrUserId(email, null)).thenReturn(response);
        when(this.passwordEncoder.matches(password, encodePassword)).thenReturn(true);

        var success = this.loginService.verifyUser(email, password, sample);

        assertNotNull(success);
        verify(this.loginMetrics, never()).startTimer();
    }

    @Test
    public void shouldGenerateAccessAndRefreshTokens() {

        var userId = "123";
        var role = "USER";

        var jwtMock = mock(Jwt.class);
        when(jwtMock.getTokenValue()).thenReturn("mocked-token");
        when(this.jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwtMock);

        var response = loginService.generateTokens(userId, role);

        assertNotNull(response);
        assertEquals("mocked-token", response.accessToken());
        assertEquals("mocked-token", response.refreshToken());
    }

    @Test
    public void shouldNotGenerateAccessAndRefreshTokens() {

        var userId = "123";
        var role = "USER";

        var jwtMock = mock(Jwt.class);
        when(jwtMock.getTokenValue()).thenReturn(null);
        when(this.jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwtMock);

        assertThrows(JwtEncodingException.class,
                () -> this.loginService.generateTokens(userId, role));
    }
}