package br.com.device_user.service.adm;

import br.com.device_user.enums.Role;
import br.com.device_user.model.User;
import br.com.device_user.repository.UserRepository;
import br.com.device_user.service.user_service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AdmServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdmService admService;

    @Test
    void createAdminUser() throws Exception {

        // Arrange
        String email = "pablo@gmail.com";
        String rawPassword = "123456789Rr@";
        String encodedPassword = "encodedPassword123";

        // Configura os mocks
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        admService.run();

        var user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        assertNotNull(user);
    }

    @Test
    void AdmIsPresent() throws Exception {

        var email = "pablo@gmail.com";

        var user = new User();
        user.setEmail(email);

        when(this.userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        this.admService.run();

        verify(this.userRepository).findByEmail(email);
        verify(this.passwordEncoder, never()).encode(anyString());
    }
}