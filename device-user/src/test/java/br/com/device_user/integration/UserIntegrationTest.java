package br.com.device_user.integration;

import br.com.device_user.enums.Role;
import br.com.device_user.model.User;
import br.com.device_user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldReturnUserWhenExistsInDatabase() throws Exception {

        // Arrange - inserir usuário real no banco H2
        var user = new User();
        user.setEmail("teste@gmail.com");
        user.setName("Rodrigo");
        user.setPassword("123");
        user.setRole(Role.USER);

        this.userRepository.save(user);
        // Act + Assert
        this.mockMvc.perform(get("/microservice/verify-if-email-already-cadastred")
                        .param("email", "teste@gmail.com")
                        .param("userId", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void shouldReturnNullWhenUserNotExistsInDatabase() throws Exception {

        this.mockMvc.perform(get("/microservice/verify-if-email-already-cadastred")
                        .param("email", "teste@gmail.com")
                        .param("userId", "123"))
                .andExpect(status().isOk());
    }
}
