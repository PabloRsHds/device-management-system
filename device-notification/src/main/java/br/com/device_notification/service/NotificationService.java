package br.com.device_notification.service;

import br.com.device_notification.dtos.ResponseNotifications;
import br.com.device_notification.infra.ServiceUnavailableEx;
import br.com.device_notification.model.Notification;
import br.com.device_notification.repository.NotificationRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // ======================================== All NOTIFICATIONS =====================================================

    @Retry(name = "retry_all_notifications", fallbackMethod = "retry_notifications")
    @CircuitBreaker(name = "circuitbreaker_all_notifications", fallbackMethod = "circuitbreaker_notifications")
    public List<ResponseNotifications> allNotifications(int page, int size) {

        return this.notificationRepository.findAllByShowNotificationTrue(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(notification -> new ResponseNotifications(
                        notification.getNotificationId(),
                        notification.getMessage()))
                .toList();
    }

    public List<ResponseNotifications> retry_notifications(int page, int size, Exception ex) {
        return List.of();
    }

    public List<ResponseNotifications> circuitbreaker_notifications(int page, int size, Exception ex) {
        return List.of();
    }

    // ================================================================================================================


    // ================================ ALL NOTIFICATIONS OCCULTS =====================================================

    @Retry(name = "retry_all_notifications", fallbackMethod = "retry_notifications")
    @CircuitBreaker(name = "circuitbreaker_all_notifications", fallbackMethod = "circuitbreaker_notifications")
    public List<ResponseNotifications> allNotificationsOccult(int page, int size) {

        return this.notificationRepository.findAllByShowNotificationFalse(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(notification -> new ResponseNotifications(
                        notification.getNotificationId(),
                        notification.getMessage()))
                .toList();
    }

    // ================================================================================================================


    // ================================================ VISUALIZAÇÃO ==================================================

    @Retry(name = "retry_for_visualization", fallbackMethod = "retry_visualisation")
    @CircuitBreaker(name = "circuitbreaker_for_visualisation", fallbackMethod = "circuitbreaker_visualisation")
    public void visualisation() {
        this.notificationRepository.markAllAsVisualised();
    }

    public void retry_visualisation(Exception ex) {
        log.error("The database service is temporarily down");
    }

    public void circuitbreaker_visualisation(Exception ex) {
        throw new ServiceUnavailableEx("The database service is temporarily down");
    }

    // ================================================================================================================

    // ======================================= OCULTAR NOTIFICAÇÕES ==================================================


    public void occultNotification(Long notificationId) {

        var notification = this.notificationRepository.findById(notificationId);

        if (notification.isEmpty()) {
            return;
        }

        notification.get().setShowNotification(false);
        this.notificationRepository.save(notification.get());
    }
    // ===============================================================================================================

    public int countNotifications() {
        return this.notificationRepository.countByVisualisationFalse();
    }

}