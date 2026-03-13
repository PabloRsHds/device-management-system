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
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    private ResultActions expectDefaultErrorStructure(ResultActions result) throws Exception {
        return result
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.source").exists())
                .andExpect(jsonPath("$.service").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists())

                .andExpect(jsonPath("$.source").value("DEVICE-NOTIFICATION"))
                .andExpect(jsonPath("$.service").value("device-notification"));
    }

    @Test
    void shouldReturnServiceUnavailableWhenCountNotifications() throws Exception{

        when(this.notificationService.countNotifications())
                .thenThrow(new ServiceUnavailable("Service unavailable"));

        this.mockMvc.perform(get("/api/count-notification"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("The service is unavailable"))
                .andExpect(jsonPath("$.message").value("Service unavailable"))
                .andExpect(jsonPath("$.path").value("/api/count-notification"));

        verify(this.notificationService).countNotifications();
    }

    @Test
    void shouldReturnNotificationNotFoundWhenOccultNotification() throws Exception{

        doThrow(new NotificationNotFound("Notification not found"))
                .when(notificationService)
                .occultNotification(1L);

        this.mockMvc.perform(put("/api/occult-notification/{notificationId}", "1"))
                .andExpect(status().isNotFound());

        verify(this.notificationService).occultNotification(1L);
    }
}