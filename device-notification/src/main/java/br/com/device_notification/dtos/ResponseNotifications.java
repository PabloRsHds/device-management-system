package br.com.device_notification.dtos;

public record ResponseNotifications(
        Long notificationId,
        String message
) {
}
