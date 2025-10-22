package by.bal.server.api.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
@ConditionalOnBooleanProperty(name = "bal-server.api.kafka.enabled", matchIfMissing = true)
@KafkaListener(groupId = "bal-server-ack", topics = "bal-topic")
@Slf4j
public class AckConsumer {

    /*
    В целом можно иметь несколько методов аннотированных @KafkaHandler (главное с разными типами @Payload)
    Если spring.kafka.listener.type=single, будет использоваться одиночный метод, batch - List<>
     */
    @KafkaHandler
    void consume(Acknowledgment ack, @Payload String payload) {
        log.info("CONSUME w ACK <<< bal-topic");
        log.info("Consumed string: {}", payload);

        ack.acknowledge();
    }

    @KafkaHandler
    void consume(Acknowledgment ack, @Payload List<String> payload) {
        log.info("CONSUME w ACK BATCH <<< bal-topic");
        log.info("Consumed batch: {}", payload);
        ack.acknowledge();
    }
}
