package br.com.device_notification.infra;

public class NotificationNotFoundEx extends RuntimeException {
    public NotificationNotFoundEx(String message) {
        super(message);
    }
}
