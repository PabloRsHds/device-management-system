package br.com.device_notification.infra.global;

import br.com.device_notification.controller.NotificationController;
import br.com.device_notification.infra.exceptions.NotificationNotFound;
import br.com.device_notification.infra.exceptions.ServiceUnavailable;
import br.com.device_notification.repository.NotificationRepository;
import br.com.device_notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalHandlerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NotificationRepository notificationRepository;

    @Test
    void shouldReturnServiceUnavailableWhenCountNotifications() throws Exception{

        when(this.notificationService.countNotifications())
                .thenThrow(new ServiceUnavailable("Service unavailable"));

        this.mockMvc.perform(get("/api/count-notification"))
                .andExpect(status().isServiceUnavailable());
    }
}