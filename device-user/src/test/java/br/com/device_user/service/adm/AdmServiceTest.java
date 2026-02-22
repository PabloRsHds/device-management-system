package br.com.device_user.service.adm;

import br.com.device_user.model.User;
import br.com.device_user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

        String email = "pablo@gmail.com";
        String rawPassword = "123456789Rr@";
        String encodedPassword = "encodedPassword123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        admService.run();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(email, savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPassword());
    }

    @Test
    void shouldReturnThrowBecauseDatabaseOff() throws Exception{

        when(this.userRepository.findByEmail("pablo@gmail.com"))
                .thenReturn(Optional.empty());

        when(this.passwordEncoder.encode("123456789Rr@"))
                .thenReturn("encode");

        when(this.userRepository.save(any(User.class)))
                .thenThrow(new DataAccessException("Database is down") {});

        assertThrows(DataAccessException.class,
                () -> this.admService.run());

        verify(this.userRepository).findByEmail("pablo@gmail.com");
        verify(this.passwordEncoder).encode("123456789Rr@");
        verify(this.userRepository).save(any(User.class));
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