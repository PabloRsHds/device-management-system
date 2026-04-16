package br.com.analysis.service;

import br.com.analysis.dtos.AnalysisEventForNotification;
import br.com.analysis.dtos.ConsumerSensorTest;
import br.com.analysis.dtos.RequestUpdateAnalysis;
import br.com.analysis.dtos.ResponseDeviceAnalysisDto;
import br.com.analysis.enums.AnalysisResult;
import br.com.analysis.infra.exceptions.DeviceNotFoundException;
import br.com.analysis.infra.exceptions.ServiceUnavailableException;
import br.com.analysis.metrics.MetricsService;
import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    // CACHE
    private static final String CACHE_DEVICE_MODEL = "cache_device_model";
    // =======

    // CIRCUIT BREAKER
    private static final String CIRCUIT_BREAKER_KAFKA_PRODUCER = "circuitbreaker_kafka_producer";
    private static final String CIRCUIT_BREAKER_GET_DEVICE = "circuitbreaker_get_device";
    // ===============

    // RETRY
    private static final String RETRY_KAFKA_PRODUCER = "retry_kafka_producer";
    private static final String RETRY_GET_DEVICE = "retry_get_device";
    // ==============

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

    public void consumerSensorTest(ConsumerSensorTest consumer) {

        System.out.println(consumer.minLimit());
        System.out.println(consumer.maxLimit());
        System.out.println(consumer.minValue());
        System.out.println(consumer.maxValue());

        var response = this.analysisResult(
                consumer.deviceModel(),
                consumer.minValue(),
                consumer.minLimit(),
                consumer.maxValue(),
                consumer.maxLimit());

        switch (response) {

            case REGISTER -> this.register(consumer);
            case FAILED -> { return; }
            case SUCCESS -> this.analysisSuccess(consumer.deviceModel(), consumer.minValue(), consumer.maxValue());
        }
    }

    @Transactional
    public AnalysisResult analysisResult(String deviceModel, Float minValue, Float minLimit, Float maxValue, Float maxLimit) {

        log.info("Verificando modelo");
        Optional<Analysis> optionalEntity =
                this.analysisRepository.findByDeviceModel(deviceModel);

        if (optionalEntity.isEmpty()) {
            log.info("Entidade de análise não presente no banco de dados");
            return AnalysisResult.REGISTER;
        }

        if (minValue < minLimit || maxValue > maxLimit) {

            log.info("Valor mínimo ou valor máximo incorretos");

            var entity = optionalEntity.get();
            entity.setAnalysisFailed(entity.getAnalysisFailed() + 1);

            this.analysisRepository.save(entity);
            this.metricsService.analysisSuccess(false);

            return AnalysisResult.FAILED;
        }

        log.info("Valor mínimo e valor máximo corretos");
        return AnalysisResult.SUCCESS;
    }

    @CacheEvict(value = CACHE_DEVICE_MODEL, key = "#deviceModel")
    @Transactional
    public void analysisSuccess(String deviceModel, Float minValue, Float maxValue) {

        Optional<Analysis> optionalEntity = this.analysisRepository.findByDeviceModel(deviceModel);

        if (optionalEntity.isEmpty()) {
            throw new DeviceNotFoundException("Device not found for analysis");
        }
        var entity = optionalEntity.get();

        String now = LocalDateTime.now()
                .atZone(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // Garantir que as listas existam (ANTI NPE)
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

        // Adiciona histórico
        listMin.add(minValue);
        listMax.add(maxValue);
        listUpdate.add(now);

        // Atualiza valores atuais
        entity.setMinLimit(minValue);
        entity.setMaxLimit(maxValue);
        entity.setUpdatedAt(now);

        // Define a leitura anterior (penúltima)
        if (listMin.size() >= 2 && listMax.size() >= 2 && listUpdate.size() >= 2) {

            int index = listMin.size() - 2;

            entity.setLastReadingMinLimit(listMin.get(index));
            entity.setLastReadingMaxLimit(listMax.get(index));
            entity.setLastReadingUpdateAt(listUpdate.get(index));

        }

        entity.setAnalysisWorked(entity.getAnalysisWorked() + 1);

        this.metricsService.analysisSuccess(true);
        log.info("Salvando novos dados para a análise");
        this.analysisRepository.save(entity);

        log.info("Envio um evento para a notificação");
        this.sendEvent("analysis-for-notification-topic",
                new AnalysisEventForNotification(
                        entity.getDeviceModel(),
                        false
                ));
    }

    @CacheEvict(value = CACHE_DEVICE_MODEL, key = "#consumer.deviceModel")
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

        log.info("Salvando uma nova análise");
        this.analysisRepository.save(newEntity);

        log.info("Enviando um evento para a notificação, analise criada");
        this.sendEvent("analysis-for-notification-topic", new AnalysisEventForNotification(
                consumer.deviceModel(),
                true
        ));
    }

    @Retry(name = RETRY_KAFKA_PRODUCER, fallbackMethod = "sendEventRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_KAFKA_PRODUCER, fallbackMethod = "sendEventCircuitBreaker")
    public void sendEvent(String topic, AnalysisEventForNotification event) {
        this.kafkaTemplate.send(topic, event);
    }

    public void sendEventRetry(String topic, AnalysisEventForNotification event, Exception e) {
        log.warn("Erro ao enviar evento para o Kafka, Kafka producer: {}", e.getMessage());
        this.metricsService.failSendEvent();
        throw new ServiceUnavailableException("Service Unavailable, please try again later");
    }

    public void sendEventCircuitBreaker(String topic, AnalysisEventForNotification event, Exception e) {
        log.warn("Circuit breaker aberto, Kafka producer: {}", e.getMessage());
        this.metricsService.failSendEvent();
        throw new ServiceUnavailableException("Service Unavailable, please try again later");
    }

    // ==============================================================================================================

    // ====================================== FIND DEVICE FOR ANALYSIS ==============================================

    public ResponseDeviceAnalysisDto getDeviceForAnalysis(String deviceModel) {

        var entity = this.getDeviceWithModel(deviceModel);

        log.info("Retornando um ResponseDeviceAnalysisDto ");
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

    @Cacheable(value = CACHE_DEVICE_MODEL , key = "#deviceModel")
    @Retry(name = RETRY_GET_DEVICE, fallbackMethod = "getDeviceWithModelRetry")
    @CircuitBreaker(name = CIRCUIT_BREAKER_GET_DEVICE, fallbackMethod = "getDeviceWithModelCircuitBreaker")
    public Analysis getDeviceWithModel(String deviceModel) {

        Optional<Analysis> entity = this.analysisRepository.findByDeviceModel(deviceModel);

        if (entity.isEmpty()) {
            log.info("Entidade vazia, retorno throw");
            throw new DeviceNotFoundException("Device not found for analysis");
        }

        log.info("Retorno a entidade no banco de dados");
        return entity.get();
    }

    public Analysis getDeviceWithModelRetry(String deviceModel, Exception e) {
        log.warn("Erro ao buscar dispositivo para análise, getDeviceModel: {}", e.getMessage());

        throw new ServiceUnavailableException("Service Unavailable, please try again later");
    }

    public Analysis getDeviceWithModelCircuitBreaker(String deviceModel, Exception e) {
        log.warn("Circuit breaker for database: {}", e.getMessage());

        throw new ServiceUnavailableException("Service Unavailable, please try again later");
    }
    // ===============================================================================================================


    // ================================================ UPDATE ========================================================

    public ResponseDeviceAnalysisDto updateAnalysis(String deviceModel, RequestUpdateAnalysis request) {

        var entity = this.getDeviceWithModel(deviceModel);

        return this.update(entity, request);
    }

    @CacheEvict(value = CACHE_DEVICE_MODEL, key = "#entity.deviceModel")
    @Transactional
    public ResponseDeviceAnalysisDto update(Analysis entity, RequestUpdateAnalysis request) {

        if (!request.name().isBlank()) {
            log.info("Salvando um novo nome");
            entity.setName(request.name());
        }

        if (!request.deviceModel().isBlank()) {
            log.info("Salvando um novo model");
            entity.setDeviceModel(request.deviceModel());
        }

        if (!request.manufacturer().isBlank()) {
            log.info("Salvando um novo manufacturer");
            entity.setManufacturer(request.manufacturer());
        }

        if (!request.description().isBlank()) {
            log.info("Salvando um novo description");
            entity.setDescription(request.description());
        }

        this.analysisRepository.save(entity);

        log.info("Retornando um response");
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

        var entity = this.getDeviceWithModel(deviceModel);
        return this.delete(entity);
    }

    @CacheEvict(value = CACHE_DEVICE_MODEL, key = "#entity.deviceModel")
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

        log.info("Apagando um dado no banco de dados referente a uma análise");
        this.analysisRepository.delete(entity);

        log.info("Retornando um response da análise que acabou de ser apagada.");
        return response;
    }


    // ===============================================================================================================
}
