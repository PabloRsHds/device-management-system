package br.com.analysis.consumer;

import br.com.analysis.dtos.DeviceDto;
import br.com.analysis.enums.Status;
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
            topics = "iot-gateway-topic",
            groupId = "iot-gateway-groupId",
            containerFactory = "kafkaListenerIotGatewayFactory")
    public void consumerIotGateway(DeviceDto consumer, Acknowledgment ack) {

        System.out.println(consumer.minLimit());
        System.out.println(consumer.maxLimit());

        log.info("Verifico a unidade, minLimit e maxLimit batem");
        var device = this.deviceClient.verificationForDeviceAnalysis(
                consumer.deviceId(),
                consumer.unit(),
                consumer.minLimit(),
                consumer.maxLimit());

        if (device == false) {
            log.warn("Verificação precisa falhou");
            ack.acknowledge();
            return;
        }

        Optional<Analysis> entity = this.analysisRepository.findByDeviceId(consumer.deviceId());

        if (entity.isPresent()) {
            log.info("Dispositivo salvo no banco, alterando dados");

            var listMin = entity.get().getHistoryMinLimit();
            var listMax = entity.get().getHistoryMaxLimit();
            var listUpdate = entity.get().getHistoryUpdate();

            listMin.add(consumer.minLimit());
            listMax.add(consumer.maxLimit());
            entity.get().setUpdatedAt(LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            entity.get().setMinLimit(consumer.minLimit());
            entity.get().setMaxLimit(consumer.maxLimit());

            if (listMin.size() >= 2 && listMax.size() >= 2 && listUpdate.size() >= 2) {

                var penultimateMinLimit = listMin.get(listMin.size() - 2);
                var penultimateMaxLimit = listMax.get(listMax.size() - 2);
                var penultimateUpdate = listUpdate.get(listUpdate.size() - 2);


                entity.get().setLastReadingMinLimit(penultimateMinLimit);
                entity.get().setLastReadingMaxLimit(penultimateMaxLimit);
                entity.get().setLastReadingUpdateAt(penultimateUpdate);
            }

            this.analysisRepository.save(entity.get());
            System.out.println("Atualização do valor");
            ack.acknowledge();
        } else {

            log.info("Novo dispositivo salvo na análise");
            var newEntity = new Analysis();

            newEntity.setDeviceId(consumer.deviceId());
            newEntity.setName(consumer.name());
            newEntity.setType(consumer.type());
            newEntity.setDescription(consumer.description());
            newEntity.setDeviceModel(consumer.deviceModel());
            newEntity.setManufacturer(consumer.manufacturer());
            newEntity.setStatus(Status.ACTIVATED);
            newEntity.setLocation(consumer.location());
            newEntity.setUnit(consumer.unit());
            newEntity.setMinLimit(consumer.minLimit());
            newEntity.setMaxLimit(consumer.maxLimit());
            newEntity.setCreatedAt(LocalDateTime.now().atZone(ZoneId.of("America/Sao_Paulo"))
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

            this.analysisRepository.save(newEntity);
            System.out.println("1 vez salvo no banco");
            ack.acknowledge();
        }
    }
}
