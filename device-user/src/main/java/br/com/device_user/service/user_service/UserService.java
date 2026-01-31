package br.com.device_user.service.user_service;

import br.com.device_user.dtos.login.ResponseUserForLogin;
import br.com.device_user.infra.exceptions.ServiceUnavailableException;
import br.com.device_user.metrics.UserMetrics;
import br.com.device_user.model.User;
import br.com.device_user.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    @CircuitBreaker(name = "circuitbreaker_for_database", fallbackMethod = "databaseOfflineFallBack")
    public ResponseUserForLogin getResponseUserWithEmail(String email) {

        var sampleTimer = this.userMetrics.startTimer();

        Optional<User> entity = this.userRepository.findByEmail(email);

        if (entity.isEmpty()) {
            this.userMetrics.recordUserNotFound();
            this.userMetrics.stopUserResponseFailedTimer(sampleTimer);
            return null;
        }

        var user = entity.get();

        this.userMetrics.recordUserFound();
        this.userMetrics.stopUserResponseSuccessTimer(sampleTimer);
        return new ResponseUserForLogin(
                user.getUserId(),
                user.getPassword(),
                user.getRole()
        );
    }

    public ResponseUserForLogin databaseOfflineFallBack(String email, Exception e) {
        log.warn("Database offline, using fallback for email: {}", email);

        throw new ServiceUnavailableException("Database service temporarily unavailable - Circuit Breaker is OPEN");
    }
}
