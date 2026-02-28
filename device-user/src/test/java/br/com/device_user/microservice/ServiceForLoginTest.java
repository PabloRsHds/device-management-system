package br.com.device_user.microservice;

import br.com.device_user.dtos.login.ResponseUserForLogin;
import br.com.device_user.infra.exceptions.ServiceUnavailableException;
import br.com.device_user.metrics.MetricsForExceptions;
import br.com.device_user.metrics.UserMetrics;
import br.com.device_user.service.user_service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceForLogin.class)
@AutoConfigureMockMvc(addFilters = false)
class ServiceForLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private MetricsForExceptions metricsForExceptions;

    @MockitoBean
    private UserMetrics userMetrics;

    @Test
    void shouldReturn200WhenUserExists() throws Exception {

        var response = new ResponseUserForLogin(
                "123",
                "123456789Rr@",
                "USER"
        );

        when(this.userService.getResponseUserWithEmailOrUserId(
                "teste@gmail.com",
                "123"
        )).thenReturn(response);

        mockMvc.perform(get("/microservice/verify-if-email-already-cadastred")
                        .param("email", "teste@gmail.com")
                        .param("userId", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("123"));

        verify(this.userService).getResponseUserWithEmailOrUserId("teste@gmail.com", "123");
    }

    @Test
    void shouldReturn503WhenGetResponseUserWithEmailOrUserId() throws Exception {

        when(this.userService.getResponseUserWithEmailOrUserId(
                "teste@gmail.com",
                "123"
        )).thenThrow(ServiceUnavailableException.class);

        mockMvc.perform(get("/microservice/verify-if-email-already-cadastred")
                        .param("email", "teste@gmail.com")
                        .param("userId", "123"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.service").exists());

        verify(this.userService).getResponseUserWithEmailOrUserId("teste@gmail.com", "123");
    }

    @Test
    void shouldReturnNullWhenUserNotExist() throws Exception{

        when(this.userService.getResponseUserWithEmailOrUserId(
                "teste@gmail.com",
                "123")).thenReturn(null);

        mockMvc.perform(get("/microservice/verify-if-email-already-cadastred")
                .param("email", "teste@gmail.com")
                .param("userId", "123"))
                .andExpect(status().isOk());

        verify(this.userService).getResponseUserWithEmailOrUserId("teste@gmail.com", "123");
    }
}
