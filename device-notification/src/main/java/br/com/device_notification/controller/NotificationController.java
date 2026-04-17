package br.com.device_notification.controller;

import br.com.device_notification.dtos.ResponseNotifications;
import br.com.device_notification.enums.NotificationVisibility;
import br.com.device_notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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
            @RequestParam int size,
            @RequestParam NotificationVisibility visibility
            ) {
        var response = this.notificationService.getAllNotifications(page, size, visibility);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/visualisation-notifications")
    public ResponseEntity<Void> visualisation() {
        this.notificationService.visualisation();
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/occult-notification/{notificationId}")
    public ResponseEntity<Void> occultNotification(@PathVariable Long notificationId) {
        this.notificationService.occultNotification(notificationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count-notification")
    public int countNotifications(){
        return this.notificationService.countNotifications();
    }
}