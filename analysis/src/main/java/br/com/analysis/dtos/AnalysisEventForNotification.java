package br.com.analysis.dtos;

public record AnalysisEventForNotification(
        String deviceModel,
        boolean created
) {
}
