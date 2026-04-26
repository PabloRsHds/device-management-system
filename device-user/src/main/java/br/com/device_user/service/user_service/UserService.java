package br.com.device_user.service.user_service;

// DTO de resposta usado no login
import br.com.device_user.dtos.login.ResponseUserForLogin;

// Exceção personalizada para indisponibilidade do serviço
import br.com.device_user.infra.exceptions.ServiceUnavailableException;

// Classe de métricas (provavelmente Prometheus/Micrometer)
import br.com.device_user.metrics.UserMetrics;

// Entidade User
import br.com.device_user.model.User;

// Repositório de acesso ao banco
import br.com.device_user.repository.UserRepository;

// Anotações do Resilience4j para tolerância a falhas
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

// Logger automático (Lombok)
import lombok.extern.slf4j.Slf4j;

// Define como serviço Spring
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private static final String CIRCUIT_BREAKER_DATABASE = "circuitbreaker_database";
    private static final String RETRY_DATABASE = "retry_database";

    private final UserRepository userRepository;
    private final UserMetrics userMetrics;

    public UserService(UserRepository userRepository,
                       UserMetrics userMetrics) {
        this.userRepository = userRepository;
        this.userMetrics = userMetrics;
    }

    // ================================
    // BUSCAR POR EMAIL
    // ================================

    @Retry(name = RETRY_DATABASE, fallbackMethod = "getUserByEmailRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_DATABASE, fallbackMethod = "getUserByEmailCircuitBreaker")
    public ResponseUserForLogin getUserByEmail(String email) {

        var sampleTimer = this.userMetrics.startTimer();

        Optional<User> entity = this.userRepository.findByEmail(email);

        if (entity.isEmpty()) {
            log.info("Usuário não encontrado pelo email={}", email);

            this.userMetrics.recordUserIsPresent("false");
            this.userMetrics.stopUserResponseFailedTimer(sampleTimer);

            return null;
        }

        var user = entity.get();

        log.info("Usuário encontrado pelo e-mail!");

        this.userMetrics.recordUserIsPresent("true");
        this.userMetrics.stopUserResponseSuccessTimer(sampleTimer);

        return new ResponseUserForLogin(
                user.getUserId(),
                user.getPassword(),
                user.getRole().toString()
        );
    }

    // ================================
    // BUSCAR POR USER ID
    // ================================

    @Retry(name = RETRY_DATABASE, fallbackMethod = "getUserByUserIdRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_DATABASE, fallbackMethod = "getUserByUserIdCircuitBreaker")
    public ResponseUserForLogin getUserByUserId(String userId) {

        var sampleTimer = this.userMetrics.startTimer();

        Optional<User> entity = this.userRepository.findByUserId(userId);

        if (entity.isEmpty()) {
            log.info("Usuário não encontrado pelo userId={}", userId);

            this.userMetrics.recordUserIsPresent("false");
            this.userMetrics.stopUserResponseFailedTimer(sampleTimer);

            return null;
        }

        var user = entity.get();

        log.info("Usuário encontrado pelo Id!");

        this.userMetrics.recordUserIsPresent("true");
        this.userMetrics.stopUserResponseSuccessTimer(sampleTimer);

        return new ResponseUserForLogin(
                user.getUserId(),
                user.getPassword(),
                user.getRole().toString()
        );
    }

    // ================================
    // FALLBACKS EMAIL
    // ================================

    public ResponseUserForLogin getUserByEmailRetry(String email, Exception e) {
        log.warn("Retry falhou para email={}", email, e);
        throw new ServiceUnavailableException("Database unavailable (retry)");
    }

    public ResponseUserForLogin getUserByEmailCircuitBreaker(String email, Exception e) {
        log.warn("Circuit breaker aberto para email={}", email);
        throw new ServiceUnavailableException("Circuit breaker OPEN");
    }

    // ================================
    // FALLBACKS USER ID
    // ================================

    public ResponseUserForLogin getUserByUserIdRetry(String userId, Exception e) {
        log.warn("Retry falhou para userId={}", userId, e);
        throw new ServiceUnavailableException("Database unavailable (retry)");
    }

    public ResponseUserForLogin getUserByUserIdCircuitBreaker(String userId, Exception e) {
        log.warn("Circuit breaker aberto para userId={}", userId);
        throw new ServiceUnavailableException("Circuit breaker OPEN");
    }
}