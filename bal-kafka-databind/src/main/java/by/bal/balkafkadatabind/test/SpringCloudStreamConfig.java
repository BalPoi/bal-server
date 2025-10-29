package by.bal.balkafkadatabind.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
@Slf4j
public class SpringCloudStreamConfig {

    @Bean
    public Supplier<String> balProducerStr() {
        return () -> {
            String message = "Hello world " + LocalDateTime.now();
            log.info("[OUT] {}", message);
            return message;
        };
    }

    // @Bean
    public Consumer<String> balConsumerStr() {
        return string -> log.info("[IN ] {}", string);
    }

    @Bean
    public Consumer<Message<String>> ackBalConsumer() {
        return msg -> {
            String payload = msg.getPayload();
            log.info("[IN ] {}", payload);
            Acknowledgment ack = msg.getHeaders().get(KafkaHeaders.ACKNOWLEDGMENT, Acknowledgment.class);
            log.info("PROCESSING: {}", payload);
            if (payload.length() <= 38) {
                log.error("Типа ошибка при обработке");
                throw new RuntimeException("Аааааа!");
                // return;
            }
            ack.acknowledge();
        };
    }
}
