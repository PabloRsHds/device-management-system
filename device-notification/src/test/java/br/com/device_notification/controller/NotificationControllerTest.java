package br.com.device_notification.controller;

import br.com.device_notification.dtos.ResponseNotifications;
import br.com.device_notification.infra.exceptions.NotificationNotFound;
import br.com.device_notification.infra.exceptions.ServiceUnavailable;
import br.com.device_notification.repository.NotificationRepository;
import br.com.device_notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private NotificationRepository notificationRepository;

    private ResultActions expectDefaultErrorStructure(ResultActions result) throws Exception{

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

    // ========================================== allNotifications ===================================================

    @Test
    void shouldReturnListResponseNotificationsWhenAllNotifications() throws Exception{

        when(this.notificationService.allNotifications(0, 10))
                .thenReturn(List.of(new ResponseNotifications(1L, "Mensagem 1")));

        mockMvc.perform(get("/api/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value(1))
                .andExpect(jsonPath("$[0].message").value("Mensagem 1"));

        assertEquals(1, this.notificationService.allNotifications(0, 10).size());
    }
    // ===============================================================================================================

    // ========================================== allNotificationsOccult =============================================

    @Test
    void shouldReturnListResponseNotificationsWhenAllNotificationsOccult() throws Exception{

        when(this.notificationService.allNotificationsOccult(0, 10))
                .thenReturn(List.of(new ResponseNotifications(1L, "Mensagem")));

        mockMvc.perform(get("/api/notifications-occult")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value(1))
                .andExpect(jsonPath("$[0].message").value("Mensagem"));
    }
    // ===============================================================================================================

    // ========================================== visualisation ======================================================

    @Test
    void shouldReturnVoidWhenVisualisation() throws Exception{

        mockMvc.perform(put("/api/visualisation-notification"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnThrowWhenVisualisation() throws Exception{

        doThrow(new ServiceUnavailable("Service unavailable"))
                .when(this.notificationService)
                .visualisation();

        mockMvc.perform(put("/api/visualisation-notification"))
                .andExpect(status().isServiceUnavailable());
    }
    // ===============================================================================================================

    // ========================================== occultNotification =================================================

    @Test
    void shouldReturnVoidWhenOccultNotification() throws Exception{

        mockMvc.perform(put("/api/occult-notification/{notificationId}","1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnThrowNotificationNotFoundWhenOccultNotification() throws Exception{

        doThrow(new NotificationNotFound("Notification not found"))
                .when(this.notificationService)
                .occultNotification(1L);

        mockMvc.perform(put("/api/occult-notification/{notificationId}","1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnThrowServiceUnavailableWhenOccultNotification() throws Exception{

        doThrow(new ServiceUnavailable("Service unavailable"))
                .when(this.notificationService)
                .occultNotification(1L);

        mockMvc.perform(put("/api/occult-notification/{notificationId}","1"))
                .andExpect(status().isServiceUnavailable());
    }
    // ===============================================================================================================

    // ========================================== countNotifications =================================================

    @Test
    void shouldReturnIntWhenCountNotifications() throws Exception{

        mockMvc.perform(get("/api/count-notification"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnThrowWhenCountNotifications() throws Exception{

        doThrow(new ServiceUnavailable("Service unavailable"))
                .when(this.notificationService)
                .countNotifications();

        mockMvc.perform(get("/api/count-notification"))
                .andExpect(status().isServiceUnavailable());
    }

    // ===============================================================================================================
}