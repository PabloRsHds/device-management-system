package br.com.analysis.consumer;

import br.com.analysis.dtos.ConsumerSensorTest;
import br.com.analysis.microservice.DeviceClient;
import br.com.analysis.model.Analysis;
import br.com.analysis.repository.AnalysisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
public class KafkaConsumer {

    private final DeviceClient deviceClient;
    private final AnalysisRepository analysisRepository;

    @Autowired
    public KafkaConsumer(DeviceClient deviceClient, AnalysisRepository analysisRepository) {
        this.deviceClient = deviceClient;
        this.analysisRepository = analysisRepository;
    }

    @Transactional
    @KafkaListener(
            topics = "sensor-test-for-analysis-topic",
            groupId = "sensor-test-for-analysis-groupId",
            containerFactory = "kafkaListenerSensorTestFactory")
    public void consumerIotGateway(ConsumerSensorTest consumer, Acknowledgment ack) {

        log.info("Recebendo mensagem do Kafka para o deviceModel={}", consumer.deviceModel());
        log.info("minLimit={}, maxLimit={}", consumer.minLimit(), consumer.maxLimit());

        // 1️⃣ Verificação externa
        boolean deviceValid = this.deviceClient.verificationForDeviceAnalysis(
                consumer.deviceModel(),
                consumer.minLimit(),
                consumer.maxLimit()
        );

        if (!deviceValid) {
            log.warn("Verificação falhou para o deviceModel={}", consumer.deviceModel());
            ack.acknowledge();
            return;
        }

        // 2️⃣ Busca no banco
        Optional<Analysis> optionalEntity =
                this.analysisRepository.findByDeviceModel(consumer.deviceModel());

        // Data atual formatada (reutilizável)
        String now = LocalDateTime.now()
                .atZone(ZoneId.of("America/Sao_Paulo"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        if (optionalEntity.isPresent()) {

            // =========================
            // 🔁 ATUALIZAÇÃO
            // =========================
            Analysis entity = optionalEntity.get();
            log.info("Dispositivo encontrado, atualizando dados");

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
            listMin.add(consumer.minLimit());
            listMax.add(consumer.maxLimit());
            listUpdate.add(now);

            // 5️⃣ Atualiza valores atuais
            entity.setMinLimit(consumer.minLimit());
            entity.setMaxLimit(consumer.maxLimit());
            entity.setUpdatedAt(now);

            // 6️⃣ Define a leitura anterior (penúltima)
            if (listMin.size() >= 2 && listMax.size() >= 2 && listUpdate.size() >= 2) {

                int index = listMin.size() - 2;

                entity.setLastReadingMinLimit(listMin.get(index));
                entity.setLastReadingMaxLimit(listMax.get(index));
                entity.setLastReadingUpdateAt(listUpdate.get(index));

                log.info("Última leitura anterior salva com sucesso");
            }

            // 7️⃣ Salva no banco
            this.analysisRepository.save(entity);
            ack.acknowledge();
            log.info("Reading min limit",entity.getLastReadingMinLimit());
            log.info("Reading max limit",entity.getLastReadingMaxLimit());
            log.info("Reading update at",entity.getLastReadingUpdateAt());
            log.info("Análise atualizada no banco");

        } else {

            // =========================
            // 🆕 NOVO REGISTRO
            // =========================
            log.info("Novo dispositivo, criando análise");

            Analysis newEntity = new Analysis();
            newEntity.setName(consumer.name());
            newEntity.setType(consumer.type());
            newEntity.setDescription(consumer.description());
            newEntity.setDeviceModel(consumer.deviceModel());
            newEntity.setManufacturer(consumer.manufacturer());
            newEntity.setUnit(consumer.unit());
            newEntity.setMinLimit(consumer.minLimit());
            newEntity.setMaxLimit(consumer.maxLimit());
            newEntity.setCreatedAt(now);

            // Inicializa listas vazias
            newEntity.setHistoryMinLimit(new ArrayList<>());
            newEntity.setHistoryMaxLimit(new ArrayList<>());
            newEntity.setHistoryUpdate(new ArrayList<>());

            this.analysisRepository.save(newEntity);
            log.info("Novo dispositivo salvo no banco");
            ack.acknowledge();
        }
    }
}
