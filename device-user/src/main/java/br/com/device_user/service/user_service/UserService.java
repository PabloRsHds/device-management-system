package br.com.device_user.service.user_service;

import br.com.device_user.dtos.login.ResponseUserForLogin;
import br.com.device_user.infra.exceptions.ServiceUnavailableException;
import br.com.device_user.metrics.UserMetrics;
import br.com.device_user.model.User;
import br.com.device_user.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMetrics userMetrics;

    public UserService(UserRepository userRepository,
                       UserMetrics userMetrics) {
        this.userRepository = userRepository;
        this.userMetrics = userMetrics;
    }

    /*
        Metodo onde é feita a busca do usuário pelo seu e-mail ou pelo seu Id (FEIGN CLIENT = MICROSERVICE DEVICE-LOGIN)
    */
    @Retry(name = "retry_database", fallbackMethod = "userRetryFallback")
    @CircuitBreaker(name = "circuitbreaker_database", fallbackMethod = "databaseOfflineFallBack")
    public ResponseUserForLogin getResponseUserWithEmailOrUserId(String email, String userId) {

        var sampleTimer = this.userMetrics.startTimer();

        // Tento retornar o usuário pelo seu e-mail.
        Optional<User> entity_email = this.userRepository.findByEmail(email);

        // Tento retornar o usuário pelo seu Id.
        Optional<User> entity_userId = this.userRepository.findByUserId(userId);

        if (entity_email.isEmpty() && entity_userId.isEmpty()) {

            log.info("Usuário não encontrado | email={} | userId={}", email, userId);

            this.userMetrics.recordUserIsPresent("false");

            log.debug("Parando o timer porque o usuário não foi encontrado!");
            this.userMetrics.stopUserResponseFailedTimer(sampleTimer);

            // Retorno null para o microserviço de login tratar, aí ele retorna um erro para o usuário.
            return null;
        }

        if (entity_email.isPresent()) {

            log.info("Usuário encontrado pelo e-mail!");
            var user = entity_email.get();
            this.userMetrics.recordUserIsPresent("true");
            this.userMetrics.stopUserResponseSuccessTimer(sampleTimer);

            return new ResponseUserForLogin(
                    user.getUserId(),
                    user.getPassword(),
                    user.getRole().toString()
            );
        }

        log.info("Usuário encontrado pelo Id!");
        var user = entity_userId.get();

        this.userMetrics.recordUserIsPresent("true");
        this.userMetrics.stopUserResponseSuccessTimer(sampleTimer);

        return new ResponseUserForLogin(
                user.getUserId(),
                user.getPassword(),
                user.getRole().toString()
        );
    }

    public ResponseUserForLogin userRetryFallback(String email, String userId, Exception e) {
        log.warn("Database retry exhausted after multiple attempts for email: {}", email, e);
        throw new ServiceUnavailableException("Database temporarily unavailable after retries");
    }

    public ResponseUserForLogin databaseOfflineFallBack(String email, String userId, Exception e) {
        log.warn("Database offline, using fallback for email: {}", email);
        throw new ServiceUnavailableException("Database service temporarily unavailable - Circuit Breaker is OPEN");
    }
}
