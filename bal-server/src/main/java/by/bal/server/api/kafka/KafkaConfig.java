package by.bal.server.api.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

/**
 * Дополнительные проперти для Kafka, которые не меняются между различными конфигурациями
 */
@Configuration
@Slf4j
public class KafkaConfig {

   @Bean
   public DefaultKafkaProducerFactoryCustomizer kafkaProducerFactoryCustomizer() {
       return producerFactory -> {
           Map<String, Object> staticProducersProps = Map.of(
                   ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
           );
           producerFactory.updateConfigs(staticProducersProps);
       };
   }

   @Bean
   public DefaultKafkaConsumerFactoryCustomizer kafkaConsumerFactoryCustomizer() {
       return consumerFactory -> {
           Map<String, Object> staticConsumersProps = Map.of(
                   ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
                   ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class,
                   // Требуется для JsonDeserializer
                   JsonDeserializer.TRUSTED_PACKAGES, "by.bal.server.api.kafka,by.bal.server.api.rest",
                   // Маппинг на случий несовпадения пакетов DTO классов между отправителями/получателями
                   JsonDeserializer.TYPE_MAPPINGS, "by.another.dev.Pet:"+Pet.class.getCanonicalName()
           );
           consumerFactory.updateConfigs(staticConsumersProps);
       };
   }
}
