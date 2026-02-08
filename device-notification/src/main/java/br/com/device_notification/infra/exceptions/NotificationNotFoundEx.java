package br.com.device_notification.infra.exceptions;

public class NotificationNotFoundEx extends RuntimeException {
    public NotificationNotFoundEx(String message) {
        super(message);
    }
}
