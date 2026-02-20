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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserClient userClient;

    @InjectMocks
    private LoginService loginService;

    @Mock
    private LoginMetrics loginMetrics;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void userNotFound() {

        var email = "notFound@gmail.com";
        var password = "99218841Pp@";
        var sample = mock(Timer.Sample.class);

        when(this.userClient.getResponseUserWithEmailOrUserId(email, null)).thenReturn(null);

        assertThrows(InvalidCredentialsException.class,
                () -> this.loginService.verifyUser(email, password, sample));

        verify(this.loginMetrics).stopFailedLoginTimer(sample);
    }

    @Test
    public void userFoundButThePasswordIsIncorrect() {

        var email = "found@gmail.com";
        var password = "123456789";
        var sample = mock(Timer.Sample.class);

        var response = new ResponseUserForLogin("321", "99218841Pp@", "USER");

        when(this.userClient.getResponseUserWithEmailOrUserId(email, null)).thenReturn(response);
        when(this.passwordEncoder.matches(password, response.password())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> this.loginService.verifyUser(email, password, sample));

        verify(this.loginMetrics).stopFailedLoginTimer(sample);
    }

    @Test
    public void userFound() {

        var email = "teste@gmail.com";
        var password = "123456789";
        var sample = mock(Timer.Sample.class);
        var response = new ResponseUserForLogin("321", "99218841Pp@", "USER");

        when(this.userClient.getResponseUserWithEmailOrUserId(email, null)).thenReturn(response);
        when(this.passwordEncoder.matches(password, response.password())).thenReturn(true);

        var result = this.loginService.verifyUser(email, password, sample);

        assertNotNull(result);
    }
}