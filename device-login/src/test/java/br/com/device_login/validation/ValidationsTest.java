package br.com.device_login.validation;

import br.com.device_login.controller.LoginController;
import br.com.device_login.metrics.exception.MetricsForExceptions;
import br.com.device_login.service.LoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(LoginController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ValidationsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

    //EMAIL

    @Test
    void shouldReturn400WhenEmailIsEmpty() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "",
                          "password": "99218841Pp@"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("The email field cannot be blank"));
    }

    @Test
    void shouldReturn400WhenEmailHaveSizeInvalid() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "a@gmail.com",
                          "password": "99218841Pp@"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("The E-mail must have at least 11 characters, and a maximum of 60 characters"));
    }

    @Test
    void shouldReturn400WhenTheEmailHasNoSymbol() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "pablogmail.com",
                          "password": "99218841Pp@"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("@ is required"));
    }

    @Test
    void shouldReturn400WhenTheEmailDoesNotHaveTheCorrectFormat() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "pablo@gmailcom",
                          "password": "99218841Pp@"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid email format. Exemple lara@gmail.com"));
    }

    // PASSWORD

    @Test
    void shouldReturn400WhenPasswordIsEmpty() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "pablo@gmail.com",
                          "password": ""
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("The password field cannot be blank"));
    }

    @Test
    void shouldReturn400WhenPasswordHaveSizeInvalid() throws Exception{

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "pablo@gmail.com",
                          "password": "1234567"
                        }
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("The password must be between 8 and 30 characters"));
    }



}
