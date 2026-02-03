package br.com.sensor_test.infra;

public class DeviceIsPresentException extends RuntimeException {
    public DeviceIsPresentException(String message) {
        super(message);
    }
}
