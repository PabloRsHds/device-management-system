package br.com.device_notification.repository;

import br.com.device_notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByShowNotificationTrue(Pageable pageable);

    Page<Notification> findAllByShowNotificationFalse(Pageable pageable);

    int countByVisualisationFalse();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.visualisation = true WHERE n.visualisation = false")
    @Transactional
    void markAllAsVisualised();
}