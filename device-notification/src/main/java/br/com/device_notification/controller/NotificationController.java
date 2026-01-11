package br.com.device_notification.controller;

import br.com.device_notification.dtos.ResponseNotifications;
import br.com.device_notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
            @RequestParam String deviceModel,
            @RequestParam int page,
            @RequestParam int size
    ) {
        return this.notificationService.allNotifications(deviceModel, page, size);
    }

    //@GetMapping("/notifications-occult")
    //public ResponseEntity<List<ResponseNotifications>> allNotificationsOccult(JwtAuthenticationToken token){
    //    return this.notificationService.allNotificationsOccult(token);
    //}

    //@PostMapping("/occult-notification")
    //public void occultNotification(@RequestBody RequestNotificationId request) {
    //    this.notificationService.occultNotification(request);
    //}

    //@PutMapping("/visualisation-notification")
    //public ResponseEntity<Void> visualisation(JwtAuthenticationToken token) {
    //    return this.notificationService.visualisation(token);
    //}

    //@GetMapping("/count-notification")
    //public int countNotifications(JwtAuthenticationToken token){
    //    return this.notificationService.countNotifications(token);
    //}
}