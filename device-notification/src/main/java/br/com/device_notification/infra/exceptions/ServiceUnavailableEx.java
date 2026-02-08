package br.com.device_notification.infra.exceptions;

public class ServiceUnavailableEx extends RuntimeException {
    public ServiceUnavailableEx(String message) {
        super(message);
    }
}
