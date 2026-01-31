package br.com.device_user.infra.exceptions;

public class ServiceUnavailableException extends RuntimeException{

    public ServiceUnavailableException(String message) {
        super(message);
    }
}
