package br.com.sensor_test.infra.exceptions;

public class SensorIsPresentException extends RuntimeException {
    public SensorIsPresentException(String message) {
        super(message);
    }
}
