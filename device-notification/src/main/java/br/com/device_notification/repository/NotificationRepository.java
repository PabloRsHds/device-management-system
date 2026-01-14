package br.com.device_notification.repository;

import br.com.device_notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByShowNotificationTrue(Pageable pageable);

    Page<Notification> findAllByShowNotificationFalse(Pageable pageable);

    int countByVisualisationFalse();
}