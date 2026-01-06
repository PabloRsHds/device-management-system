package br.com.analysis.kafkaConfig;

import br.com.analysis.dtos.ConsumerSensorTest;
import br.com.analysis.dtos.DeviceDto;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    private final KafkaProperties kafkaProperties;


    public KafkaConsumerConfig(KafkaProperties kafka) {
        this.kafkaProperties = kafka;
    }

    @Bean
    public ConsumerFactory<String, ConsumerSensorTest> consumerSensorTestEvent() {

        Map<String, Object> props = this.kafkaProperties.buildConsumerProperties();

        JsonDeserializer<ConsumerSensorTest> valueDeserializer =
                new JsonDeserializer<>(ConsumerSensorTest.class, false);

        valueDeserializer.addTrustedPackages("br.com.analysis.dtos");
        valueDeserializer.setRemoveTypeHeaders(false);
        valueDeserializer.setUseTypeMapperForKey(false);

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ConsumerSensorTest> kafkaListenerSensorTestFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ConsumerSensorTest> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerSensorTestEvent());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}