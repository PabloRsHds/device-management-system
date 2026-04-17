package br.com.device_notification.service;

import br.com.device_notification.enums.NotificationVisibility;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        var show = false;
        var visibility = NotificationVisibility.VISIBLE;

        when(notificationRepository.findAllByShowNotification(show,any(Pageable.class)))
                .thenReturn(page);

        var response = notificationService.getAllNotifications(0, 10, visibility);

        assertEquals(1, response.size());
        verify(this.notificationRepository).findAllByShowNotification(show ,any(Pageable.class));
    }
    // ================================================================================================================

    // ================================================ VISUALIZAÇÃO ==================================================
    @Test
    void shouldReturnVoidWhenVisualisation(){

        this.notificationService.visualisation();
    }
    //================================================================================================================

    // ======================================= OCULTAR NOTIFICAÇÕES ==================================================

    @Test
    void shouldReturnVoidWhenOccultNotification() {

        when(this.notificationRepository.findById(1L))
                .thenReturn(Optional.of(new Notification()));

        this.notificationService.occultNotification(1L);
    }

    @Test
    void shouldReturnNotificationWhenVerifyIfNotificationIsEmpty(){

        when(this.notificationRepository.findById(1L))
                .thenReturn(Optional.of(new Notification()));

        var response = this.notificationService.getNotificationOrThrow(1L);

        verify(this.notificationRepository).findById(1L);
        assertNotNull(response);
    }

    @Test
    void shouldReturnThrowWhenVerifyIfNotificationIsEmpty(){

        when(this.notificationRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(NotificationNotFound.class,
                () -> this.notificationService.getNotificationOrThrow(1L));

        verify(this.notificationRepository).findById(1L);
    }

    @Test
    void shouldReturnVoidWhenSaveUpdateInShowNotification(){

        this.notificationService.saveUpdateInShowNotification(new Notification());
    }

    //=================================================================================================================

    // ========================================= COUNT NOTIFICATIONS =================================================

    @Test
    void shouldReturnIntWhenCountNotifications(){

        var response = this.notificationService.countNotifications();

        assertEquals(0L, response);
    }
}