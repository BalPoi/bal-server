package by.bal.server.api.kafka.pet;

import by.bal.server.api.kafka.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnBooleanProperty(name = "bal-server.api.kafka.enabled", matchIfMissing = true)
@ConditionalOnProperty(name = "bal-server.api.kafka.mode", havingValue = "normal")
@Slf4j
@RequiredArgsConstructor
public class SimplePetProducer {
    private final KafkaTemplate<String, Pet> kafkaTemplate;

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    void produce() {
        Pet.generate().limit(10).forEach(pet -> {
            log.info("[>>> bal-topic-pet]: {}", pet);
            kafkaTemplate.send("bal-topic-pet", pet);
        });
    }
}
