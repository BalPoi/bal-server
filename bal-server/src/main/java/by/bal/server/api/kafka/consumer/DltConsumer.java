package by.bal.server.api.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

//@Component
@ConditionalOnBooleanProperty(name = "bal-server.api.kafka.enabled", matchIfMissing = true)
@KafkaListener(groupId = "bal-server", topics = "bal-topic-dlt")
@Slf4j
public class DltConsumer {

    @KafkaHandler
    void consume(@Payload String payload, MessageHeaders headers) {
        log.warn("CONSUMED <<< bal-topic-dlt");
        log.warn("Payload: {}", payload);
        log.warn("Headers: {}", headers);
    }
}
