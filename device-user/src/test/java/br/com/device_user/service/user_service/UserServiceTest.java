package br.com.device_user.service.user_service;

import br.com.device_user.enums.Role;
import br.com.device_user.infra.exceptions.ServiceUnavailableException;
import br.com.device_user.metrics.UserMetrics;
import br.com.device_user.model.User;
import br.com.device_user.repository.UserRepository;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMetrics userMetrics;

    @InjectMocks
    private UserService userService;

    @Test
    public void shouldReturnUserWhenFoundByEmail() {

        var email = "teste@gmail.com";
        var userId = "123";

        var user = new User();
        user.setUserId(userId);
        user.setName("Pablo");
        user.setEmail(email);
        user.setPassword("123456789Rr@");
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now().toString());

        when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Timer.Sample sample = mock(Timer.Sample.class);
        when(this.userMetrics.startTimer()).thenReturn(sample);

        var response = this.userService.getResponseUserWithEmailOrUserId(email, userId);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals("123456789Rr@", response.password());
        assertEquals("USER", response.role());

        verify(this.userRepository).findByEmail(email);
        verify(this.userRepository, never()).findByUserId(userId);
        verify(this.userMetrics).recordUserIsPresent("true");
        verify(this.userMetrics).stopUserResponseSuccessTimer(sample);
    }

    @Test
    public void shouldReturnUserWhenFoundByUserId() {

        var email = "teste@gmail.com";
        var userId = "123";

        var user = new User();
        user.setUserId(userId);
        user.setName("Rodrigo");
        user.setEmail(email);
        user.setPassword("123456789Rr@");
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now().toString());

        when(this.userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        Timer.Sample sample = mock(Timer.Sample.class);
        when(this.userMetrics.startTimer()).thenReturn(sample);

        var response = this.userService.getResponseUserWithEmailOrUserId(email, userId);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(user.getPassword(), response.password());
        assertEquals(user.getRole().toString(), response.role());

        verify(this.userRepository).findByEmail(email);
        verify(this.userRepository).findByUserId(userId);
        verify(this.userMetrics).recordUserIsPresent("true");
        verify(this.userMetrics).stopUserResponseSuccessTimer(sample);
    }

    @Test
    public void shouldReturnNullWhenUserNotFound() {

        // Arrange
        var email = "notfound@gmail.com";
        var userId = "1235";

        when(this.userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        when(this.userRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        Timer.Sample sample = mock(Timer.Sample.class);
        when(this.userMetrics.startTimer()).thenReturn(sample);

        // Act
        var response = this.userService.getResponseUserWithEmailOrUserId(email, userId);

        // Assert
        assertNull(response);

        // Verify
        verify(this.userRepository).findByEmail(email);
        verify(this.userRepository).findByUserId(userId);
        verify(this.userMetrics).recordUserIsPresent("false");
        verify(this.userMetrics).stopUserResponseFailedTimer(sample);
    }

    @Test
    public void shouldThrowServiceUnavailableWhenRetryFallbackIsCalled(){

        var email = "test@gmail.com";
        var userId = "123";

        assertThrows(ServiceUnavailableException.class, () ->
                userService.userRetryFallback(email, userId,
                        new DataAccessException("Database temporarily unavailable after retries") {})
        );

        verifyNoInteractions(this.userMetrics);
        verifyNoInteractions(this.userRepository);
    }

    @Test
    public void shouldThrowServiceUnavailableWhenCircuitBreakerIsCalled() {

        var email = "test@gmail.com";
        var userId = "123";

        assertThrows(ServiceUnavailableException.class, () ->
                userService.databaseOfflineFallBack(email, userId,
                        new DataAccessException("Database service temporarily unavailable - Circuit Breaker is OPEN") {}));

        verifyNoInteractions(this.userMetrics);
        verifyNoInteractions(this.userRepository);
    }
}