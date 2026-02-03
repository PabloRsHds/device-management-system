package br.com.sensor_test.infra.exceptions;

public class SensorIsEmptyException extends RuntimeException {
    public SensorIsEmptyException(String message) {
        super(message);
    }
}
