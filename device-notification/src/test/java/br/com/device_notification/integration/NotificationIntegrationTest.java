package br.com.device_notification.integration;

import br.com.device_notification.infra.exceptions.NotificationNotFound;
import br.com.device_notification.model.Notification;
import br.com.device_notification.repository.NotificationRepository;
import br.com.device_notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static reactor.core.publisher.Mono.when;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    // ============================================ allNotifications =================================================
    @Test
    void shouldReturnListResponseNotificationsWhenAllNotifications() throws Exception{

        var notification = new Notification();
        notification.setMessage("Message");
        notification.setShowNotification(true);
        this.notificationRepository.save(notification);

        this.mockMvc.perform(get("/api/notifications")
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value(notification.getNotificationId()))
                .andExpect(jsonPath("$[0].message").value(notification.getMessage()));
    }

    // ===============================================================================================================

    // ====================================== allNotificationsOccult =================================================

    @Test
    void shouldReturnListResponseNotificationsWhenAllNotificationsOccult() throws Exception{

        var notification = new Notification();
        notification.setMessage("Message");
        notification.setShowNotification(false);
        this.notificationRepository.save(notification);

        this.mockMvc.perform(get("/api/notifications-occult")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].notificationId").value(notification.getNotificationId()))
                .andExpect(jsonPath("$[0].message").value(notification.getMessage()));
    }

    // ===============================================================================================================

    // =============================================== visualisation =================================================

    @Test
    void shouldReturnVoidWhenVisualisation() throws Exception{

        var notification = new Notification();
        notification.setMessage("Message");
        notification.setShowNotification(false);
        notification.setVisualisation(false);
        this.notificationRepository.save(notification);

        this.mockMvc.perform(put("/api/visualisation-notification"))
                .andExpect(status().isOk());
    }

    // ===============================================================================================================

    // ========================================== occultNotification =================================================

    @Test
    void shouldReturnVoidWhenOccultNotification() throws Exception{

        var notification = new Notification();
        notification.setMessage("Message");
        notification.setShowNotification(true);
        this.notificationRepository.save(notification);

        this.mockMvc.perform(put("/api/occult-notification/{notificationId}",
                        notification.getNotificationId()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnThrowWhenOccultNotification() throws Exception {

        mockMvc.perform(put("/api/occult-notification/{notificationId}", 999))
                .andExpect(status().isConflict());
    }

    // ===============================================================================================================


}
