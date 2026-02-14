package br.com.device_notification.service;

import br.com.device_notification.dtos.ResponseNotifications;
import br.com.device_notification.infra.exceptions.NotificationNotFound;
import br.com.device_notification.infra.exceptions.ServiceUnavailable;
import br.com.device_notification.metrics.MetricsService;
import br.com.device_notification.repository.NotificationRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MetricsService metricsService;

    @Autowired
    public NotificationService(
            NotificationRepository notificationRepository,
            MetricsService metricsService) {

        this.notificationRepository = notificationRepository;
        this.metricsService = metricsService;
    }

    // ======================================== All NOTIFICATIONS =====================================================

    @Retry(name = "retry_notifications", fallbackMethod = "allNotificationsRetry")
    @CircuitBreaker(name = "circuitbreaker_notifications", fallbackMethod = "allNotificationsCircuitBreaker")
    public List<ResponseNotifications> allNotifications(int page, int size) {

        return this.notificationRepository.findAllByShowNotificationTrue(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(notification -> new ResponseNotifications(
                        notification.getNotificationId(),
                        notification.getMessage()))
                .toList();
    }

    public List<ResponseNotifications> allNotificationsRetry(int page, int size, Exception ex) {
        return List.of();
    }

    public List<ResponseNotifications> allNotificationsCircuitBreaker(int page, int size, Exception ex) {

        this.metricsService.circuitbreaker("circuitbreaker_notifications");
        return List.of();
    }

    // ================================================================================================================


    // ================================ ALL NOTIFICATIONS OCCULTS =====================================================

    @Retry(name = "retry_occult_notifications", fallbackMethod = "allNotificationsOccultRetry")
    @CircuitBreaker(name = "circuitbreaker_occult_notifications", fallbackMethod = "allNotificationsOccultCircuitBreaker")
    public List<ResponseNotifications> allNotificationsOccult(int page, int size) {

        return this.notificationRepository.findAllByShowNotificationFalse(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(notification -> new ResponseNotifications(
                        notification.getNotificationId(),
                        notification.getMessage()))
                .toList();
    }

    public List<ResponseNotifications> allNotificationsOccultRetry(int page, int size, Exception ex) {
        return List.of();
    }

    public List<ResponseNotifications> allNotificationsOccultCircuitBreaker(int page, int size, Exception ex) {

        this.metricsService.circuitbreaker("circuitbreaker_occult_notifications");
        return List.of();
    }

    // ================================================================================================================


    // ================================================ VISUALIZAÇÃO ==================================================

    @Retry(name = "retry_visualisation", fallbackMethod = "visualisationRetry")
    @CircuitBreaker(name = "circuitbreaker_visualisation", fallbackMethod = "visualisationCircuitBreaker")
    public void visualisation() {
        this.notificationRepository.markAllAsVisualised();
    }

    public void visualisationRetry(Exception ex) {
        log.error("The database service is temporarily down");
    }

    public void visualisationCircuitBreaker(Exception ex) {

        this.metricsService.circuitbreaker("circuitbreaker_visualisation");
        throw new ServiceUnavailable("The database service is temporarily down");
    }

    // ================================================================================================================

    // ======================================= OCULTAR NOTIFICAÇÕES ==================================================

    @Retry(name = "retry_occult", fallbackMethod = "occultRetry")
    @CircuitBreaker(name = "circuitbreaker_occult", fallbackMethod = "occultCircuitBreaker")
    public void occultNotification(Long notificationId) {
        this.occult(notificationId);
    }

    @Transactional
    public void occult(Long notificationId) {
        var notification = this.notificationRepository.findById(notificationId);

        if (notification.isEmpty()) {
            throw new NotificationNotFound("Notification not found");
        }

        notification.get().setShowNotification(false);
        this.notificationRepository.save(notification.get());
    }

    public void occultRetry(Long notificationId, Exception ex) {
        log.error("Retry exhausted while occulting notification {}", notificationId, ex);
    }

    public void occultCircuitBreaker(Long notificationId, Exception ex) {

        this.metricsService.circuitbreaker("circuitbreaker_occult");
        throw new ServiceUnavailable("The database service is temporarily down");
    }

    // ===============================================================================================================

    @Retry(name = "retry_count", fallbackMethod = "countRetry")
    @CircuitBreaker(name = "circuitbreaker_count", fallbackMethod = "countCircuitBreaker")
    public int countNotifications() {
        return this.notificationRepository.countByVisualisationFalse();
    }

    public int countRetry(Exception ex) {
        log.error("Error while counting notifications");
        return 0;
    }

    public int countCircuitBreaker(Exception ex) {

        this.metricsService.circuitbreaker("circuitbreaker_count");
        throw new ServiceUnavailable("The database service is temporarily down");
    }
}