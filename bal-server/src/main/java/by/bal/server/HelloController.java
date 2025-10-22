package by.bal.server;

import by.bal.server.api.kafka.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;
import java.util.UUID;

@RestController
@ConditionalOnBooleanProperty(name = "bal-server.api.rest.enabled", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class HelloController {

    //Заинжектится один и тот же бин с JsonSerializer
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, Pet> petKafkaTemplate;

    @GetMapping
    void hello(@RequestBody String text) {
        log.info("Manual producer: {}", text);
        kafkaTemplate.send("bal-topic", "{\"name\":\"%s\",\"age\":24}".formatted(text));
    }

    @GetMapping("/batch")
    void hello(@RequestParam Integer size) {
        UUID uuid = UUID.randomUUID();
        log.info("Batch producer. UUID={} Size={}", uuid, size);
        for (int i = 0; i < size; i++) {
            log.info(">>> {}[{}]", uuid, i);
            kafkaTemplate.send("bal-topic", uuid + "[" + i + "]");
        }
    }

    @GetMapping("/pet")
    void helloPet() {
        Pet pet = Pet.builder().name("Сёма").type("Кот").age(4).build();
        petKafkaTemplate.send("bal-pet-topic", pet);
    }
}
