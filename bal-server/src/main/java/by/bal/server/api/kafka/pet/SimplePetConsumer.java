package by.bal.server.api.kafka.pet;

import by.bal.server.api.kafka.Pet;
import io.micrometer.core.instrument.config.validate.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.listener.BatchListenerFailedException;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnBooleanProperty(name = "bal-server.api.kafka.enabled", matchIfMissing = true)
// Ретраи и отправка в DLT на уровне KafkaListenerErrorHandler
// @RetryableTopic(
//         attempts = "4", // Общее количество попыток (включая первую)
//         backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000), // Экспоненциальная задержка
//         // include = {MyRetryableException.class}, // Какие исключения повторять (опционально)
//         exclude = {ValidationException.class}, // Какие исключения не повторять (опционально)
//         dltTopicSuffix = ".dlt" // Суффикс для DLT
// )
@KafkaListener(
        topics = "bal-topic-pet",
        errorHandler = "simpleListenerErrorHandler"
        // properties = { // Можно вот так указывать какие-то проперти для конкретного KafkaListener
        //         "max.poll.interval.ms:60000",
        //         "isolation.level=read_committed"
        // }
)
@Slf4j
public class SimplePetConsumer {
    private static void mayBeException(Pet pet) {
        if (pet.name().equals("Error")) {
            throw new RuntimeException("Что-то сломалось...");
        }
    }

    // Реализация @KafkaHandler для batch режима
    // @KafkaHandler
    // void consumeBatchManualImmediate(Acknowledgment ack, @Payload List<Pet> pets) {
    //     log.info("[<<< bal-topic-pet BATCH {}]", pets.size());
    //     int i = 0;
    //     try {
    //         while (i < pets.size()) {
    //             Pet pet = pets.get(i);
    //             log.info("[{}]: {}", i, pet);
    //
    //             mayBeException(pet);
    //
    //             // ack.acknowledge(i);// ack-mode = manual_immediate; Коммитит каждый оффсет сразу
    //             i++;
    //         }
    //     } catch (Exception e) {
    //         // /!\ Это исключение обязательно для корректных коммитом оффсетов при исключениях
    //         // Если кидать это исключение, то корректно обработанные сооьщения будут закомичены
    //         // А также после какой-либо обработки этого исключения offset этого сообщения также будет закомичен
    //         throw new BatchListenerFailedException("Ошибка при обработке сообщения", e, i);
    //     }
    //
    //     ack.acknowledge();// ack-mode = manual; Коммитит последний оффсет батча
    // }

    // Реализация @KafkaHandler для single режима
    // (!) poll() в любом случае принимает батч сообщений, просто они по-одному передаются в KafkaHandler
    @KafkaHandler
    void consume(Acknowledgment ack, @Payload Pet pet) {
        log.info("[<<< bal-topic-pet]: {}", pet);
        mayBeException(pet);

        // Если ack-mode = manual, то коммит будет либо при ошибке, либо при следующем poll(),
        // если manual_immediate - то коммитится каждое сообщение последовательно
        ack.acknowledge();
    }
}
