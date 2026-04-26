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
    void shouldReturnUserWhenEmailExistsInDatabase() throws Exception {

        var user = new User();
        user.setEmail("teste@gmail.com");
        user.setName("Rodrigo");
        user.setPassword("123");
        user.setRole(Role.USER);

        this.userRepository.save(user);

        this.mockMvc.perform(get("/microservice/verify-by-email")
                        .param("email", "teste@gmail.com"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNullWhenEmailNotExistsInDatabase() throws Exception {

        this.mockMvc.perform(get("/microservice/verify-by-email")
                        .param("email", "teste@gmail.com"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUserWhenUserIdExistsInDatabase() throws Exception {

        var user = new User();
        user.setEmail("teste@gmail.com");
        user.setName("Rodrigo");
        user.setPassword("123");
        user.setRole(Role.USER);

        this.userRepository.save(user);

        this.mockMvc.perform(get("/microservice/verify-by-userId")
                        .param("userId", user.getUserId()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNullWhenUserIdNotExistsInDatabase() throws Exception {

        this.mockMvc.perform(get("/microservice/verify-by-userId")
                        .param("userId", "1"))
                .andExpect(status().isOk());
    }
}
