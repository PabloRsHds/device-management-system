package br.com.device_notification.service;

import br.com.device_notification.dtos.ResponseNotifications;
import br.com.device_notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }



    public ResponseEntity<List<ResponseNotifications>> allNotifications(String deviceModel,int page, int size) {

        List<ResponseNotifications> notifications = this.notificationRepository.findAllByDeviceModelAndShowNotificationTrue(deviceModel,PageRequest.of(page, size))
                .stream()
                .map(notification -> new ResponseNotifications(
                        notification.getMessage()))
                .toList();

         return ResponseEntity.ok(notifications);
    }


    //public ResponseEntity<List<ResponseNotifications>> allNotificationsOccult(JwtAuthenticationToken token) {

       // List<ResponseNotifications> notifications = this.notificationRepository.findAllByUserId(token.getName()).stream()
               // .filter(notification -> Boolean.FALSE.equals(notification.getShowNotification()))
               // .map(notification -> new ResponseNotifications(
                  //      notification.getNotificationId(),
                  //      notification.getMessage(),
                   //     notification.getShowNotification(),
                  //      notification.getTimestamp()))
                //.collect(Collectors.toList());

        //return ResponseEntity.ok(notifications);
    //}

    //public void occultNotification(RequestNotificationId request) {

        //var notification = this.notificationRepository.findById(request.notificationId());

        //if (notification.isEmpty()) {
        //    return;
        //}

        //notification.get().setShowNotification(false);
        //this.notificationRepository.save(notification.get());
    //}


   // public ResponseEntity<Void> visualisation(JwtAuthenticationToken token) {

      //  List<Notification> notifications = this.notificationRepository.findAllByUserId(token.getName());

      //  for (var notification : notifications) {
      //      notification.setVisualisation(true);
      //      this.notificationRepository.save(notification);
      //  }

     //   return ResponseEntity.ok().build();
    //}


    //public int countNotifications(JwtAuthenticationToken token) {
    //    return this.notificationRepository.countByUserIdAndVisualisationFalse(token.getName());
    //}
}