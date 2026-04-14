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

@Slf4j // Habilita logs com "log.info", "log.warn", etc
@Service // Marca como Service (camada de regra de negócio)
public class UserService {

    // Repositório para buscar usuários no banco
    private final UserRepository userRepository;

    // Classe responsável por registrar métricas
    private final UserMetrics userMetrics;

    // Injeção de dependências via construtor
    public UserService(UserRepository userRepository,
                       UserMetrics userMetrics) {
        this.userRepository = userRepository;
        this.userMetrics = userMetrics;
    }

    /*
        Método responsável por buscar o usuário pelo email ou pelo userId.
        Esse método provavelmente é chamado por outro microserviço (ex: login via Feign Client).
    */

    // Retry: tenta novamente em caso de falha (ex: erro de banco)
    @Retry(name = "retry_database", fallbackMethod = "userRetryFallback")

    // Circuit Breaker: abre o circuito se houver muitas falhas
    @CircuitBreaker(name = "circuitbreaker_database", fallbackMethod = "databaseOfflineFallBack")
    public ResponseUserForLogin getResponseUserWithEmailOrUserId(String email, String userId) {

        // Inicia um timer para métricas de performance
        var sampleTimer = this.userMetrics.startTimer();

        // Busca usuário pelo email
        Optional<User> entity_email = this.userRepository.findByEmail(email);

        // Se encontrou pelo email
        if (entity_email.isPresent()) {

            log.info("Usuário encontrado pelo e-mail!");

            var user = entity_email.get();

            // Marca métrica como usuário encontrado
            this.userMetrics.recordUserIsPresent("true");

            // Finaliza timer de sucesso
            this.userMetrics.stopUserResponseSuccessTimer(sampleTimer);

            // Retorna DTO com dados necessários para login
            return new ResponseUserForLogin(
                    user.getUserId(),
                    user.getPassword(),
                    user.getRole().toString()
            );
        }

        // Caso não encontre pelo email, tenta pelo userId
        Optional<User> entity_userId = this.userRepository.findByUserId(userId);

        // Se NÃO encontrou nem pelo userId
        if (entity_userId.isEmpty()) {

            log.info("Usuário não encontrado | email={} | userId={}", email, userId);

            // Marca métrica como não encontrado
            this.userMetrics.recordUserIsPresent("false");

            log.debug("Parando o timer porque o usuário não foi encontrado!");

            // Finaliza timer de falha
            this.userMetrics.stopUserResponseFailedTimer(sampleTimer);

            // Retorna null (outro microserviço trata isso)
            return null;
        }

        // Se encontrou pelo userId
        log.info("Usuário encontrado pelo Id!");

        var user = entity_userId.get();

        // Marca métrica como encontrado
        this.userMetrics.recordUserIsPresent("true");

        // Finaliza timer de sucesso
        this.userMetrics.stopUserResponseSuccessTimer(sampleTimer);

        // Retorna DTO com dados do usuário
        return new ResponseUserForLogin(
                user.getUserId(),
                user.getPassword(),
                user.getRole().toString()
        );
    }

    // Método fallback do Retry
    // É chamado quando todas as tentativas de retry falham
    public ResponseUserForLogin userRetryFallback(String email, String userId, Exception e) {

        log.warn("Database retry exhausted after multiple attempts for email: {}", email, e);

        // Lança exceção informando indisponibilidade temporária
        throw new ServiceUnavailableException("Database temporarily unavailable after retries");
    }

    // 🔌 Método fallback do Circuit Breaker
    // É chamado quando o circuito está aberto (muitas falhas recentes)
    public ResponseUserForLogin databaseOfflineFallBack(String email, String userId, Exception e) {

        log.warn("Database offline, using fallback for email: {}", email);

        // Lança exceção informando que o serviço está indisponível
        throw new ServiceUnavailableException("Database service temporarily unavailable - Circuit Breaker is OPEN");
    }
}