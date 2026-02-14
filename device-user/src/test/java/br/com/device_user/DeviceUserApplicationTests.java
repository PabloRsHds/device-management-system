package br.com.device_user;

import br.com.device_user.dtos.login.ResponseUserForLogin;
import br.com.device_user.enums.Role;
import br.com.device_user.metrics.UserMetrics;
import br.com.device_user.model.User;
import br.com.device_user.repository.UserRepository;
import br.com.device_user.service.user_service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class DeviceUserApplicationTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMetrics userMetrics;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldReturnUserWhenFoundByEmail() {

        String email = "test@email.com";
        String userId = "123";

        var user = mock(User.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        when(user.getUserId()).thenReturn("123");
        when(user.getPassword()).thenReturn("senha");
        when(user.getRole()).thenReturn(Role.USER);

        when(userMetrics.startTimer()).thenReturn(null);

        var response =
                userService.getResponseUserWithEmailOrUserId(email, userId);

        assertNotNull(response);
        assertEquals("123", response.userId());
        assertEquals("senha", response.password());
        assertEquals("USER", response.role());

        verify(userRepository).findByEmail(email);
        verify(userRepository).findByUserId(userId);
    }

    @Test
    void shouldReturnUserWhenFoundByUserId() {

        String email = "test@email.com";
        String userId = "123";

        User user = mock(User.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        when(user.getUserId()).thenReturn("123");
        when(user.getPassword()).thenReturn("senha");
        when(user.getRole()).thenReturn(Role.ADMIN);

        when(userMetrics.startTimer()).thenReturn(null);

        ResponseUserForLogin response =
                userService.getResponseUserWithEmailOrUserId(email, userId);

        assertNotNull(response);
        assertEquals("ADMIN", response.role());
    }

    @Test
    void shouldReturnNullWhenUserNotFound() {

        String email = "notfound@email.com";
        String userId = "999";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        when(userMetrics.startTimer()).thenReturn(null);

        ResponseUserForLogin response =
                userService.getResponseUserWithEmailOrUserId(email, userId);

        assertNull(response);

        verify(userMetrics).recordUserIsPresent("false");
    }

    @Test
    void shouldThrowExceptionWhenRepositoryFails() {

        String email = "test@email.com";
        String userId = "123";

        when(userRepository.findByEmail(email))
                .thenThrow(new RuntimeException("Database error"));

        when(userMetrics.startTimer()).thenReturn(null);

        assertThrows(RuntimeException.class, () ->
                userService.getResponseUserWithEmailOrUserId(email, userId)
        );
    }

}
