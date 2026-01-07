package br.com.device_notification.consumer;

import br.com.device_notification.dtos.ConsumerAnalysis;
import br.com.device_notification.model.Notification;
import br.com.device_notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationConsumer(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @KafkaListener(topics = "analysis-for-notification-topic",
            groupId = "analysis-for-notification-groupId",
            containerFactory = "kafkaListenerAnalysisFactory")
    public void createNotificationApproved(ConsumerAnalysis event, Acknowledgment ack){

        var date = LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        var notification = new Notification();
        notification.setDeviceModel(event.deviceModel());
        if (Objects.equals(event.created(), true)) {
            notification.setMessage("This device has been sent for review\n"+date);
        } else {
            notification.setMessage("The device with model: "+event.deviceModel()+" has been updated\n"+date);
        }
        notification.setCreatedAt(date);
        this.notificationRepository.save(notification);
        ack.acknowledge();
    }
}