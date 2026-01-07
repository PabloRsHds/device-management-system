package br.com.device_notification.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tb_notifications")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long notificationId;

    private String deviceModel;
    private String message;
    private Boolean visualisation = false;
    private Boolean showNotification = true;

    private String createdAt;
}