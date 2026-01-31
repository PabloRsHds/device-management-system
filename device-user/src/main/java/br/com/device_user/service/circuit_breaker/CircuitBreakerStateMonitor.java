package br.com.device_user.service.circuit_breaker;

import br.com.device_user.dtos.metricsDto.CircuitBreakerMetricDto;
import br.com.device_user.metrics.CircuitBreakerMetrics;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CircuitBreakerStateMonitor {

    private final CircuitBreakerMetrics circuitBreakerMetrics;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerStateMonitor(CircuitBreakerMetrics circuitBreakerMetrics, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerMetrics = circuitBreakerMetrics;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    public void init() {
        log.error("Inicializando monitor manual do Circuit Breaker");

        CircuitBreaker circuitBreaker = circuitBreakerRegistry
                .circuitBreaker("circuitbreaker_for_database");

        // Listener MANUAL
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> {

                    log.info("Aqui salvo as métricas do circuit breaker");
                    circuitBreakerMetrics.recordCircuitBreakerStates(
                            new CircuitBreakerMetricDto(
                                    event.getCircuitBreakerName(),
                                    event.getStateTransition().getFromState().name(),
                                    event.getStateTransition().getToState().name()
                            )
                    );
                })
                .onError(event -> {
                    log.error("Erro no circuit breaker: {} - {}",
                            event.getCircuitBreakerName(),
                            event.getThrowable().getMessage());
                })
                .onSuccess(event -> {
                    log.error("Success event: {}", event.getCircuitBreakerName());
                });

        log.error("Listener manual registrado para: {}", circuitBreaker.getName());
    }
}
