package br.com.device_notification.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "device_model")
    private String deviceModel;
    private String message;
    private Boolean visualisation = false;
    @Column(name = "show_notification")
    private Boolean showNotification = true;

    @Column(name = "created_at")
    private String createdAt;
}