package br.com.device_user.service.user_service;

import br.com.device_user.enums.Role;
import br.com.device_user.metrics.UserMetrics;
import br.com.device_user.model.User;
import br.com.device_user.repository.UserRepository;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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

        Mockito.when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Timer.Sample sample = Mockito.mock(Timer.Sample.class);
        Mockito.when(this.userMetrics.startTimer()).thenReturn(sample);

        var response = this.userService.getResponseUserWithEmailOrUserId(email, userId);

        assertNotNull(response);
        assertEquals(userId, response.userId());
        assertEquals("123456789Rr@", response.password());
        assertEquals("USER", response.role());

        Mockito.verify(this.userMetrics).recordUserIsPresent("true");
        Mockito.verify(this.userMetrics).stopUserResponseSuccessTimer(sample);
    }

    @Test
    public void shouldReturnNullWhenUserNotFound() {

        // Arrange
        var email = "notfound@gmail.com";
        var userId = "1235";

        Mockito.when(this.userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        Mockito.when(this.userRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        Timer.Sample sample = Mockito.mock(Timer.Sample.class);
        Mockito.when(this.userMetrics.startTimer()).thenReturn(sample);

        // Act
        var response = this.userService.getResponseUserWithEmailOrUserId(email, userId);

        // Assert
        assertNull(response);

        // Verify
        Mockito.verify(this.userMetrics).recordUserIsPresent("false");
        Mockito.verify(this.userMetrics).stopUserResponseFailedTimer(sample);
    }
}