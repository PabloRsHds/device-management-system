package br.com.device_management.infra.exceptions;

public class DeviceIsPresent extends RuntimeException {

    public DeviceIsPresent(String message) {
        super(message);
    }
}
