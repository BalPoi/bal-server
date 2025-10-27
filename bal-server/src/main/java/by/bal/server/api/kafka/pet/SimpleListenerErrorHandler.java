package by.bal.server.api.kafka.pet;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SimpleListenerErrorHandler implements KafkaListenerErrorHandler {
    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception) {
        log.error("SimpleListenerErrorHandler словил исключение!", exception);
        log.error("Message: {}", message);
        // throw exception; // Если пробросить исклбчение дальше, то его будет уже обрабатывать CommonErrorHandler
        return null;
    }
}
