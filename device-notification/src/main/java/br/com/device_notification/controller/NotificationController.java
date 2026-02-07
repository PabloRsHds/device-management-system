package br.com.device_notification.controller;

import br.com.device_notification.dtos.ResponseNotifications;
import br.com.device_notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;


    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }


    @GetMapping("/notifications")
    public ResponseEntity<List<ResponseNotifications>> allNotifications(
            @RequestParam int page,
            @RequestParam int size
    ) {
        var response = this.notificationService.allNotifications(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/notifications-occult")
    public ResponseEntity<List<ResponseNotifications>> allNotificationsOccult(
            @RequestParam int page,
            @RequestParam int size
    ) {
        return this.notificationService.allNotificationsOccult(page, size);
    }

    @PutMapping("/visualisation-notification")
    public ResponseEntity<Void> visualisation() {
        return this.notificationService.visualisation();
    }

    @PutMapping("/occult-notification/{notificationId}")
    public void occultNotification(@PathVariable Long notificationId) {
        this.notificationService.occultNotification(notificationId);
    }

    @GetMapping("/count-notification")
    public int countNotifications(){
        return this.notificationService.countNotifications();
    }
}