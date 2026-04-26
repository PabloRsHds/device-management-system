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
    void shouldReturnUserWhenFoundByEmail() {

        var email = "teste@gmail.com";

        var user = new User();
        user.setUserId("1");
        user.setName("Pablo");
        user.setEmail(email);
        user.setPassword("123456789Rr@");
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now().toString());

        when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Timer.Sample sample = mock(Timer.Sample.class);
        when(this.userMetrics.startTimer()).thenReturn(sample);

        var response = this.userService.getUserByEmail(email);

        assertNotNull(response);
        assertEquals("123456789Rr@", response.password());
        assertEquals("USER", response.role());

        verify(this.userRepository).findByEmail(email);
        verify(this.userMetrics).recordUserIsPresent("true");
        verify(this.userMetrics).stopUserResponseSuccessTimer(sample);
    }

    @Test
    void shouldReturnUserWhenFoundByUserId() {

        var userId = "1";

        var user = new User();
        user.setUserId(userId);
        user.setName("Rodrigo");
        user.setEmail("teste@gmail.com");
        user.setPassword("123456789Rr@");
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now().toString());

        when(this.userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        Timer.Sample sample = mock(Timer.Sample.class);
        when(this.userMetrics.startTimer()).thenReturn(sample);

        var response = this.userService.getUserByUserId(userId);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals(user.getPassword(), response.password());
        assertEquals(user.getRole().toString(), response.role());

        verify(this.userRepository).findByUserId(userId);
        verify(this.userMetrics).recordUserIsPresent("true");
        verify(this.userMetrics).stopUserResponseSuccessTimer(sample);
    }

    @Test
    void shouldReturnNullWhenUserByEmailNotFound() {

        var email = "notfound@gmail.com";

        when(this.userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        Timer.Sample sample = mock(Timer.Sample.class);
        when(this.userMetrics.startTimer()).thenReturn(sample);

        // Act
        var response = this.userService.getUserByEmail(email);

        // Assert
        assertNull(response);

        // Verify
        verify(this.userRepository).findByEmail(email);
        verify(this.userMetrics).recordUserIsPresent("false");
        verify(this.userMetrics).stopUserResponseFailedTimer(sample);
    }

    @Test
    void shouldReturnNullWhenUserByUserIdNotFound() {

        var userId = "123";

        when(this.userRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        Timer.Sample sample = mock(Timer.Sample.class);
        when(this.userMetrics.startTimer()).thenReturn(sample);

        // Act
        var response = this.userService.getUserByUserId(userId);

        // Assert
        assertNull(response);

        // Verify
        verify(this.userRepository).findByUserId(userId);
        verify(this.userMetrics).recordUserIsPresent("false");
        verify(this.userMetrics).stopUserResponseFailedTimer(sample);
    }

    @Test
    void shouldThrowServiceUnavailableWhenGetUserByEmailRetry(){

        var email = "test@gmail.com";

        assertThrows(ServiceUnavailableException.class, () ->
                userService.getUserByEmailRetry(email,
                        new DataAccessException("Database temporarily unavailable after retries") {}
                )
        );

        verifyNoInteractions(this.userMetrics);
        verifyNoInteractions(this.userRepository);
    }

    @Test
    void shouldThrowServiceUnavailableWhenGetUserByEmailCircuitBreaker() {

        var email = "test@gmail.com";

        assertThrows(ServiceUnavailableException.class, () ->
                userService.getUserByEmailCircuitBreaker(email,
                        new DataAccessException("Database service temporarily unavailable - Circuit Breaker is OPEN") {}));

        verifyNoInteractions(this.userMetrics);
        verifyNoInteractions(this.userRepository);
    }

    @Test
    void shouldThrowServiceUnavailableWhenGetUserByUserIdRetry(){

        var userId = "1";

        assertThrows(ServiceUnavailableException.class, () ->
                userService.getUserByUserIdRetry(userId,
                        new DataAccessException("Database temporarily unavailable after retries") {}
                )
        );

        verifyNoInteractions(this.userMetrics);
        verifyNoInteractions(this.userRepository);
    }

    @Test
    void shouldThrowServiceUnavailableWhenGetUserByUserIdCircuitBreaker() {

        var userId = "1";

        assertThrows(ServiceUnavailableException.class, () ->
                userService.getUserByUserIdCircuitBreaker(userId,
                        new DataAccessException("Database service temporarily unavailable - Circuit Breaker is OPEN") {}));

        verifyNoInteractions(this.userMetrics);
        verifyNoInteractions(this.userRepository);
    }
}