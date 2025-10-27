package by.bal.server.api.kafka_messaging;

import by.bal.server.api.kafka.Pet;
import by.bal.server.api.kafka.StatusDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bal-server.api.kafka.mode", havingValue = "messaging")
// @KafkaListener(topics = "bal-topic-pet-message")
// /!\Связка @KafkaListener и @KafkaHandler НЕ РАБОТАЕТ для Spring Messaging. Нужно @KafkaListener именно над методом
@Slf4j
public class PetMessageConsumer {

    // @KafkaListener(topics = "bal-topic-pet-message"/*, groupId = "bal-server-pet-mess1"*/)
    // public void consume(Acknowledgment ack, Message<Pet> petMessage) {
    //     Pet pet = petMessage.getPayload();
    //     log.info("[<<< bal-topic-pet1]: {}", pet);
    //     log.info("Message: {}", petMessage);
    //     mayBeException(pet);
    //     ack.acknowledge();
    // }

    // @KafkaListener(topics = "bal-topic-pet-message", groupId = "bal-server-pet-mess2")
    // public void consume(Acknowledgment ack, @Payload Pet pet, ConsumerRecordMetadata metadata) {
    //     log.info("[<<< bal-topic-pet2]: {}", pet);
    //     log.info("ConsumerRecordMetadata: {}", metadata);
    //     mayBeException(pet);
    //     ack.acknowledge();
    // }

    // @KafkaListener(topics = "bal-topic-pet-message", groupId = "bal-server-pet-mess3")
    // public void consume(Acknowledgment ack, @Payload Pet pet, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
    //     log.info("[<<< bal-topic-pet3]: {}", pet);
    //     log.info("Topic from headers: {}", topic);
    //     mayBeException(pet);
    //     ack.acknowledge();
    // }

    // @SendTo("bal-topic-pet-message-reply") // Отправляет результат метода в указанные топики. Можно только один указать топик
    // @KafkaListener(topics = "bal-topic-pet-message")
    public StatusDto consume(Acknowledgment ack, @Payload Pet pet) {
        log.info("[<<< bal-topic-pet4]: {}", pet);
        mayBeException(pet);
        ack.acknowledge();
        return new StatusDto("Пет обработан успешно: " + pet, true);
    }

    private void mayBeException(Pet pet) {
        if (pet.name().equals("Error")) {
            throw new RuntimeException("Что-то сломалось...");
        }
    }
}
