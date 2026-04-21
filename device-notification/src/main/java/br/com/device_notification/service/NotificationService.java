package br.com.device_notification.service;

import br.com.device_notification.dtos.ResponseNotifications;
import br.com.device_notification.enums.Visibility;
import br.com.device_notification.infra.exceptions.NotificationNotFound;
import br.com.device_notification.infra.exceptions.ServiceUnavailable;
import br.com.device_notification.metrics.MetricsService;
import br.com.device_notification.model.Notification;
import br.com.device_notification.repository.NotificationRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class NotificationService {

    // CACHE
    private static final String CACHE_ALL_NOTIFICATIONS = "cache_all_notifications";
    // ================

    // CIRCUIT BREAKER
    private static final String CIRCUIT_BREAKER_NOTIFICATIONS = "circuitbreaker_notifications";
    private static final String CIRCUIT_BREAKER_VISUALISATION = "circuitbreaker_visualisation";
    private static final String CIRCUIT_BREAKER_OCCULT = "circuitbreaker_occult";
    private static final String CIRCUIT_BREAKER_COUNT = "circuitbreaker_count";
    // ===============

    // RETRY
    private static final String RETRY_NOTIFICATIONS = "retry_notifications";
    private static final String RETRY_VISUALISATION = "retry_visualisation";
    private static final String RETRY_OCCULT = "retry_occult";
    private static final String RETRY_COUNT = "retry_count";

    // ===============

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

    @Cacheable(value = CACHE_ALL_NOTIFICATIONS, key = "#page + '-' + #size + '?' + #visibility")
    @Retry(name = RETRY_NOTIFICATIONS, fallbackMethod = "getAllNotificationsRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_NOTIFICATIONS, fallbackMethod = "getAllNotificationsCircuitBreaker")
    public List<ResponseNotifications> getAllNotifications(int page, int size, Visibility visibility) {

        boolean show = visibility == Visibility.VISIBLE;

        log.info("Retornando todas as notificações");
        return this.notificationRepository.findAllByShowNotification(show, PageRequest.of(page, size,
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(notification -> new ResponseNotifications(
                        notification.getNotificationId(),
                        notification.getMessage()))
                .toList();
    }

    public List<ResponseNotifications> getAllNotificationsRetry(int page, int size, Visibility visibility, Exception ex) {
        return List.of();
    }

    public List<ResponseNotifications> getAllNotificationsCircuitBreaker(int page, int size, Visibility visibility, Exception ex) {

        this.metricsService.circuitbreaker("circuitbreaker_notifications");
        return List.of();
    }

    // ================================================================================================================


    // ================================================ VISUALIZAÇÃO ==================================================

    @Retry(name = RETRY_VISUALISATION, fallbackMethod = "visualisationRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_VISUALISATION, fallbackMethod = "visualisationCircuitBreaker")
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

    public void occultNotification(Long notificationId) {
        var notification = this.getNotificationOrThrow(notificationId);
        this.updateShowNotification(notification);
    }

    @Retry(name = RETRY_OCCULT, fallbackMethod = "occultRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_OCCULT, fallbackMethod = "occultCircuitBreaker")
    public Notification getNotificationOrThrow(Long notificationId) {

        var notification = this.notificationRepository.findById(notificationId);

        if (notification.isEmpty()) {
            log.info("Notificação não existe");
            throw new NotificationNotFound("Notification not found");
        }

        log.info("Notificação presente no banco de dados");
        return notification.get();
    }

    @CacheEvict(value = CACHE_ALL_NOTIFICATIONS, allEntries = true)
    @Transactional
    public void updateShowNotification(Notification notification) {

        log.info("Notificação presente, salvando notificação como vista");
        notification.setShowNotification(false);
        this.notificationRepository.save(notification);
    }

    public void occultRetry(Long notificationId, Exception ex) {
        log.error("Retry exhausted while occulting notification {}", notificationId, ex);
    }

    public void occultCircuitBreaker(Long notificationId, Exception ex) {

        this.metricsService.circuitbreaker("circuitbreaker_occult");
        throw new ServiceUnavailable("The database service is temporarily down");
    }

    // ===============================================================================================================

    // ========================================= COUNT NOTIFICATIONS =================================================

    @CacheEvict(value = CACHE_ALL_NOTIFICATIONS, allEntries = true)
    @Retry(name = RETRY_COUNT, fallbackMethod = "countNotificationsRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_COUNT, fallbackMethod = "countNotificationsCircuitBreaker")
    public int countNotifications() {
        return this.notificationRepository.countByVisualisationFalse();
    }

    public int countNotificationsRetry(Exception ex) {
        log.error("Error while counting notifications");
        return 0;
    }

    public int countNotificationsCircuitBreaker(Exception ex) {

        log.info("CircuitBreaker ON");
        this.metricsService.circuitbreaker("circuitbreaker_count");
        throw new ServiceUnavailable("The database service is temporarily down");
    }
}