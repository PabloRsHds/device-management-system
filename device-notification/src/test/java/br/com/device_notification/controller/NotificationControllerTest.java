package br.com.device_notification.controller;

import br.com.device_notification.dtos.ResponseNotifications;
import br.com.device_notification.metrics.MetricsService;
import br.com.device_notification.repository.NotificationRepository;
import br.com.device_notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationController notificationController;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NotificationRepository notificationRepository;

    @MockitoBean
    private MetricsService metricsService;

    @Test
    void shouldReturnListResponseNotificationsWhenAllNotifications() throws Exception{

        when(this.notificationService.allNotifications(anyInt(), anyInt()))
                .thenReturn(List.of(new ResponseNotifications(1L, "message")));

        mockMvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk());
    }
}