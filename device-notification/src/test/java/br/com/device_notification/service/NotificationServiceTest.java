package br.com.device_notification.service;

import br.com.device_notification.infra.exceptions.NotificationNotFound;
import br.com.device_notification.metrics.MetricsService;
import br.com.device_notification.model.Notification;
import br.com.device_notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {


    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private MetricsService metricsService;

    // ======================================== All NOTIFICATIONS =====================================================
    @Test
    void shouldReturnResponseNotificationsWhenAllNotifications() {

        var notification = new Notification();
        notification.setNotificationId(1L);
        notification.setMessage("Mensagem teste");

        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(notificationRepository.findAllByShowNotificationTrue(any(Pageable.class)))
                .thenReturn(page);

        var response = notificationService.allNotifications(0, 10);

        assertEquals(1, response.size());
        verify(this.notificationRepository).findAllByShowNotificationTrue(any(Pageable.class));
    }
    // ================================================================================================================

    // ================================ ALL NOTIFICATIONS OCCULTS =====================================================

    @Test
    void shouldReturnResponseNotificationsWhenAllNotificationsOccult() {

        var notification = new Notification();
        notification.setNotificationId(1L);
        notification.setMessage("Mensagem teste");

        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(notificationRepository.findAllByShowNotificationFalse(any(Pageable.class)))
                .thenReturn(page);

        var response = notificationService.allNotificationsOccult(0, 10);

        assertEquals(1, response.size());
        verify(this.notificationRepository).findAllByShowNotificationFalse(any(Pageable.class));
    }

    // ================================================================================================================

    // ================================================ VISUALIZAÇÃO ==================================================
    @Test
    void shouldReturnVoidWhenVisualisation(){

        this.notificationService.visualisation();
    }
    //================================================================================================================

    // ======================================= OCULTAR NOTIFICAÇÕES ==================================================



    //=================================================================================================================

    // ========================================= COUNT NOTIFICATIONS =================================================

    @Test
    void shouldReturnIntWhenCountNotifications(){

        var response = this.notificationService.countNotifications();

        assertEquals(0L, response);
    }
}