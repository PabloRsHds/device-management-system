package br.com.analysis.service;

import br.com.analysis.dtos.AnalysisEventForNotification;
import br.com.analysis.dtos.ConsumerSensorTest;
import br.com.analysis.dtos.RequestUpdateAnalysis;
import br.com.analysis.dtos.ResponseDeviceAnalysisDto;
import br.com.analysis.infra.exceptions.DeviceNotFoundException;
import br.com.analysis.infra.exceptions.ServiceUnavailableException;
import br.com.analysis.metrics.MetricsService;
import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final MetricsService metricsService;


    public AnalysisService(
            AnalysisRepository analysisRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            MetricsService metricsService) {
        this.analysisRepository = analysisRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.metricsService = metricsService;
    }

    // ============================================= REGISTER =======================================================

    public void consumerIotGatewayService(ConsumerSensorTest consumer) {

        System.out.println(consumer.minLimit());
        System.out.println(consumer.maxLimit());
        System.out.println(consumer.minValue());
        System.out.println(consumer.maxValue());

        var entity = this.analysisFailed(
                consumer.deviceModel(),
                consumer.minValue(),
                consumer.minLimit(),
                consumer.maxValue(),
                consumer.maxLimit());

        if (entity != null) {

            this.analysisSuccess(
                    entity,
                    consumer.minValue(),
                    consumer.maxValue());

        } else {
            this.register(consumer);
        }
    }

    @Transactional
    public Analysis analysisFailed(String deviceModel, Float minValue, Float minLimit, Float maxValue, Float maxLimit) {

        if (minValue < minLimit ||
                maxValue > maxLimit) {

            Optional<Analysis> optionalEntity =
                    this.analysisRepository.findByDeviceModel(deviceModel);

            if (optionalEntity.isPresent()) {

                var entity = optionalEntity.get();
                entity.setAnalysisFailed(entity.getAnalysisFailed() + 1);

                this.analysisRepository.save(entity);

                this.metricsService.analysisSuccess(false);
                return entity;
            }

        }
        return null;
    }

    @Transactional
    public void analysisSuccess(Analysis entity, Float minValue, Float maxValue) {

        String now = LocalDateTime.now()
                .atZone(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // 3️⃣ Garantir que as listas existam (ANTI NPE)
        if (entity.getHistoryMinLimit() == null) {
            entity.setHistoryMinLimit(new ArrayList<>());
        }
        if (entity.getHistoryMaxLimit() == null) {
            entity.setHistoryMaxLimit(new ArrayList<>());
        }
        if (entity.getHistoryUpdate() == null) {
            entity.setHistoryUpdate(new ArrayList<>());
        }

        var listMin = entity.getHistoryMinLimit();
        var listMax = entity.getHistoryMaxLimit();
        var listUpdate = entity.getHistoryUpdate();

        // 4️⃣ Adiciona histórico
        listMin.add(minValue);
        listMax.add(maxValue);
        listUpdate.add(now);

        // 5️⃣ Atualiza valores atuais
        entity.setMinLimit(minValue);
        entity.setMaxLimit(maxValue);
        entity.setUpdatedAt(now);

        // 6️⃣ Define a leitura anterior (penúltima)
        if (listMin.size() >= 2 && listMax.size() >= 2 && listUpdate.size() >= 2) {

            int index = listMin.size() - 2;

            entity.setLastReadingMinLimit(listMin.get(index));
            entity.setLastReadingMaxLimit(listMax.get(index));
            entity.setLastReadingUpdateAt(listUpdate.get(index));

        }

        entity.setAnalysisWorked(entity.getAnalysisWorked() + 1);

        this.metricsService.analysisSuccess(true);
        this.analysisRepository.save(entity);

        this.sendEvent("analysis-for-notification-topic",
                new AnalysisEventForNotification(
                        entity.getDeviceModel(),
                        false
                ));
    }

    @Transactional
    public void register(ConsumerSensorTest consumer) {

        String now = LocalDateTime.now()
                .atZone(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        var newEntity = new Analysis();
        newEntity.setName(consumer.name());
        newEntity.setType(consumer.type());
        newEntity.setDescription(consumer.description());
        newEntity.setDeviceModel(consumer.deviceModel());
        newEntity.setManufacturer(consumer.manufacturer());
        newEntity.setUnit(consumer.unit());
        newEntity.setMinLimit(consumer.minValue());
        newEntity.setMaxLimit(consumer.maxValue());
        newEntity.setCreatedAt(now);

        // Inicializa listas vazias
        newEntity.setHistoryMinLimit(new ArrayList<>());
        newEntity.setHistoryMaxLimit(new ArrayList<>());
        newEntity.setHistoryUpdate(new ArrayList<>());

        this.analysisRepository.save(newEntity);

        this.sendEvent("analysis-for-notification-topic", new AnalysisEventForNotification(
                consumer.deviceModel(),
                true
        ));
    }

    @CircuitBreaker(name = "circuitbreaker_kafka_producer", fallbackMethod = "circuitbreaker_for_kafka_producer")
    public void sendEvent(String topic, AnalysisEventForNotification event) {
        this.kafkaTemplate.send(topic, event);
    }

    public void circuitbreaker_for_kafka_producer(String topic, AnalysisEventForNotification event, Exception e) {
        log.warn("Circuit breaker for kafka: {}", e.getMessage());
        this.metricsService.failSendEvent();
    }

    // ==============================================================================================================

    // ====================================== FIND DEVICE FOR ANALYSIS ==============================================

    public ResponseDeviceAnalysisDto findDeviceForAnalysis(String deviceModel) {

        var entity = this.findDeviceModel(deviceModel);

        return new ResponseDeviceAnalysisDto(
                entity.getName(),
                entity.getDeviceModel(),
                entity.getMinLimit(),
                entity.getMaxLimit(),
                entity.getUnit(),
                entity.getUpdatedAt(),
                entity.getCreatedAt(),
                entity.getLastReadingMinLimit(),
                entity.getLastReadingMaxLimit(),
                entity.getLastReadingUpdateAt(),
                entity.getAnalysisWorked(),
                entity.getAnalysisFailed());
    }


    @CircuitBreaker(name = "circuitbreaker_database", fallbackMethod = "circuitbreaker_for_database")
    public Analysis findDeviceModel(String deviceModel) {

        Optional<Analysis> entity = this.analysisRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            throw new DeviceNotFoundException("Device not found for analysis");
        }

        return entity.get();
    }

    public Analysis circuitbreaker_for_database(String deviceModel, Exception e) {
        log.warn("Circuit breaker for database: {}", e.getMessage());

        throw new ServiceUnavailableException("Service Unavailable, please try again later");
    }
    // ===============================================================================================================


    // ================================================ UPDATE ========================================================

    public ResponseDeviceAnalysisDto updateAnalysis(String deviceModel, RequestUpdateAnalysis request) {

        var entity = this.findDeviceModel(deviceModel);

        return this.update(entity, request);
    }

    @Transactional
    public ResponseDeviceAnalysisDto update(Analysis entity, RequestUpdateAnalysis request) {

        if (!request.name().isBlank()) {
            entity.setName(request.name());
        }

        if (!request.deviceModel().isBlank()) {
            entity.setDeviceModel(request.deviceModel());
        }

        if (!request.manufacturer().isBlank()) {
            entity.setManufacturer(request.manufacturer());
        }

        if (!request.description().isBlank()) {
            entity.setDescription(request.description());
        }

        this.analysisRepository.save(entity);

        return new ResponseDeviceAnalysisDto(
                entity.getName(),
                entity.getDeviceModel(),
                entity.getMinLimit(),
                entity.getMaxLimit(),
                entity.getUnit(),
                entity.getUpdatedAt(),
                entity.getCreatedAt(),
                entity.getLastReadingMinLimit(),
                entity.getLastReadingMaxLimit(),
                entity.getLastReadingUpdateAt(),
                entity.getAnalysisWorked(),
                entity.getAnalysisFailed()
        );
    }

    // ===============================================================================================================

    // ================================================ DELETE =======================================================


    public ResponseDeviceAnalysisDto deleteAnalysis(String deviceModel) {

        var entity = this.findDeviceModel(deviceModel);
        return this.delete(entity);
    }

    @Transactional
    public ResponseDeviceAnalysisDto delete(Analysis entity) {

        var response = new ResponseDeviceAnalysisDto(
                entity.getName(),
                entity.getDeviceModel(),
                entity.getMinLimit(),
                entity.getMaxLimit(),
                entity.getUnit(),
                entity.getUpdatedAt(),
                entity.getCreatedAt(),
                entity.getLastReadingMinLimit(),
                entity.getLastReadingMaxLimit(),
                entity.getLastReadingUpdateAt(),
                entity.getAnalysisWorked(),
                entity.getAnalysisFailed()
        );

        this.analysisRepository.delete(entity);
        return response;
    }


    // ===============================================================================================================
}
