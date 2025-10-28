package by.bal.server.api.kafka_messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.config.validate.ValidationException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonDelegatingErrorHandler;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.ConversionException;
import org.springframework.kafka.support.converter.JsonMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper;
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper;

@Configuration
@ConditionalOnProperty(name = "bal-server.api.kafka.mode", havingValue = "messaging")
public class KafkaMessagingConfig {

    @Bean
    public JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        var messageConverter = new StringJsonMessageConverter(objectMapper);

        // Переопределение тайпмапера нужно обязательно, если в одном KafkaListener несколько KafkaHandlers
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.addTrustedPackages("by.bal.server.api.kafka");
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID);

        messageConverter.setTypeMapper(typeMapper);

        return messageConverter;
    }

    // @Bean
    // public CommonErrorHandler commonErrorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
    //     // Дефолтный обработчик для всех исключений: кидаем сообщение в DLT
    //     var defaultErrorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate));
    //     defaultErrorHandler.addNotRetryableExceptions(ValidationException.class, IllegalArgumentException.class);
    //     var delegatingErrorHandler = new CommonDelegatingErrorHandler(defaultErrorHandler);
    //
    //     // Обработчик DeserializationException: кидаем в отдельный DLT
    //     var deserializationExceptionRecoverer = new DeadLetterPublishingRecoverer(
    //             kafkaTemplate, (cr, e) -> new TopicPartition(cr.topic() + "-deserialization-dlt", cr.partition())
    //     );
    //     deserializationExceptionRecoverer.setLogRecoveryRecord(true);
    //     DefaultErrorHandler deserializationErrorHandler = new DefaultErrorHandler(deserializationExceptionRecoverer);
    //     // delegatingErrorHandler.addDelegate(DeserializationException.class, deserializationErrorHandler);
    //     delegatingErrorHandler.addDelegate(ConversionException.class, deserializationErrorHandler);
    //
    //     return delegatingErrorHandler;
    // }
}
