package br.com.device_user.dtos.metricsDto;

public record CircuitBreakerMetricDto(
        String circuitName,
        String fromState, 
        String toState
) {
}
