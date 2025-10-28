package by.bal.server.api.kafka_messaging;

import by.bal.server.api.kafka.Man;
import by.bal.server.api.kafka.Pet;
import by.bal.server.api.kafka.StatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.Map;

// Обязательная конфига в by.bal.server.api.kafka_messaging.KafkaMessagingConfig.jsonMessageConverter
// Чтобы нормально определялся тип сообщения

@Component
@ConditionalOnProperty(name = "bal-server.api.kafka.mode", havingValue = "messaging")
@KafkaListener(topics = "bal-topic-pet-message-many-types")
@Slf4j
public class AnotherMessageConsumer {

    @KafkaHandler
    public void consumePet(Acknowledgment ack, @Payload Pet pet, ConsumerRecordMetadata metadata) {
        log.info("[<<< bal-topic-pet PET]: {}", pet);
        log.info("Message: {}", pet);
        mayBeException(pet);
        ack.acknowledge();
    }

    @KafkaHandler
    public void consumeMan(Acknowledgment ack, @Payload Man man, ConsumerRecordMetadata metadata) {
        log.info("[<<< bal-topic-pet MAN]: {}", man);
        ack.acknowledge();
    }

    @KafkaHandler(isDefault = true)
    public void consumeDefault(Acknowledgment ack, @Payload Object message, ConsumerRecordMetadata metadata) {
        log.info("[<<< bal-topic-pet DEFAULT]: {}", message);
        ack.acknowledge();
    }

    private void mayBeException(Pet pet) {
        if (pet.name().equals("Error")) {
            throw new RuntimeException("Что-то сломалось...");
        }
    }
}
