package by.bal.server.api.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

//@Component
@ConditionalOnBooleanProperty(name = "bal-server.api.kafka.enabled", matchIfMissing = true)
@KafkaListener(groupId = "bal-server", topics = "bal-topic")
@Slf4j
public class SimpleConsumer {

    @KafkaHandler
    void consume(@Payload String payload) {
        log.info("CONSUME <<< bal-topic");
        log.info("Consumed string: {}", payload);
//        if (System.currentTimeMillis() % 100 < 30) {
//            //Сообщение считается всё равно прочитаным
//            RuntimeException e = new RuntimeException("АААААА! Что-то случилось!");
//            log.error("Ошибочка: {}", e.getLocalizedMessage());
//            throw e;
//        }
    }
}
