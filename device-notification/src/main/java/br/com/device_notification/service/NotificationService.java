package br.com.device_notification.service;

import br.com.device_notification.dtos.ResponseNotifications;
import br.com.device_notification.model.Notification;
import br.com.device_notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }



    public ResponseEntity<List<ResponseNotifications>> allNotifications(int page, int size) {

        List<ResponseNotifications> notifications = this.notificationRepository.findAllByShowNotificationTrue(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(notification -> new ResponseNotifications(
                        notification.getNotificationId(),
                        notification.getMessage()))
                .toList();

         return ResponseEntity.ok(notifications);
    }

    public ResponseEntity<List<ResponseNotifications>> allNotificationsOccult(int page, int size) {

        List<ResponseNotifications> notifications = this.notificationRepository.findAllByShowNotificationFalse(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(notification -> new ResponseNotifications(
                        notification.getNotificationId(),
                        notification.getMessage()))
                .toList();

        return ResponseEntity.ok(notifications);
    }

    public ResponseEntity<Void> visualisation() {
        List<Notification> notifications = this.notificationRepository.findAll();

        for (var notification : notifications) {
            notification.setVisualisation(true);
            this.notificationRepository.save(notification);
        }

        return ResponseEntity.ok().build();
    }

    public void occultNotification(Long notificationId) {

        var notification = this.notificationRepository.findById(notificationId);

        if (notification.isEmpty()) {
            return;
        }

        notification.get().setShowNotification(false);
        this.notificationRepository.save(notification.get());
    }

    public int countNotifications() {
        return this.notificationRepository.countByVisualisationFalse();
    }

}