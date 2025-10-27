package by.bal.server.api.kafka_messaging;

import by.bal.server.api.kafka.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "bal-server.api.kafka.mode", havingValue = "messaging")
@Slf4j
@RequiredArgsConstructor
public class PetMessageProducer {
    private final KafkaTemplate<String, Pet> kafkaTemplate;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void produce() {
        Pet.generate().limit(10).map(this::toMessage).forEach(petMessage -> {
            log.info("[>>> bal-topic-message]: {}", petMessage);
            kafkaTemplate.send(petMessage);
        });
    }

    private Message<Pet> toMessage(Pet pet) {
        return MessageBuilder.withPayload(pet)
                             .setHeader(KafkaHeaders.TOPIC, "bal-topic-pet-message")
                             .build();
    }
}
