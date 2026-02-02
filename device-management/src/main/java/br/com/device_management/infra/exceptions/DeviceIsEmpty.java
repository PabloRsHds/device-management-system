package br.com.device_management.infra.exceptions;

public class DeviceIsEmpty extends RuntimeException {
    public DeviceIsEmpty(String message) {
        super(message);
    }
}
