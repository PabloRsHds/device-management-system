package br.com.device_notification.repository;

import br.com.device_notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Collection<Notification> findAllByDeviceModel(String name);
}