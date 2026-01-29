package br.com.device_login.infra.exceptions;

public class ServiceUnavailableException extends RuntimeException{

    // Tratamento de erro para serviço indisponível
    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceUnavailableException(Throwable cause) {
        super(cause);
    }
}
