package br.com.analysis.dtos;

public record RequestUpdateAnalysis(
        String name,
        String deviceModel,
        String manufacturer,
        String description
) {
}
