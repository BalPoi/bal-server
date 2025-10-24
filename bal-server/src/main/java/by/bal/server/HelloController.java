package by.bal.server;

import by.bal.server.api.kafka.Man;
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

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping
    void hello(@RequestBody String text) {
        log.info("Manual producer: {}", text);
        Man man = Man.builder().name(text).age(24).build();
        kafkaTemplate.send("bal-topic", man);
    }

    @GetMapping("/pet")
    void helloPet() {
        Pet pet = Pet.builder().name("Сёма").type("Кот").age(4).build();
        kafkaTemplate.send("bal-pet-topic", pet);
    }
}
