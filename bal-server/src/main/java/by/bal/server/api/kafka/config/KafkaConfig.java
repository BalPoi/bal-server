package by.bal.server.api.kafka.config;

import io.micrometer.core.instrument.config.validate.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonContainerStoppingErrorHandler;
import org.springframework.kafka.listener.CommonDelegatingErrorHandler;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static by.bal.server.api.kafka.config.KafkaTopicValueTypeMappingConfig.getValueTypeMethod;
import static java.lang.Boolean.TRUE;

/**
 * Дополнительные проперти для Kafka, которые не меняются между различными конфигурациями
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class KafkaConfig {

    /**
     * Равен имени бина {@link org.springframework.boot.autoconfigure.kafka.KafkaAnnotationDrivenConfiguration#kafkaListenerContainerFactory}
     */
    @SuppressWarnings("JavadocReference")
    private static final String CONTAINER_FACTORY_BEAN_NAME = "kafkaListenerContainerFactory";
    private static final BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition>
            DESERIALIZATION_DLT_TOPIC_RESOLVER =
            (cr, e) -> new TopicPartition(cr.topic() + "-deserialization-dlt", 0);

    /**
     * Постпроцессор для бина kafkaListenerContainerFactory для дополнительной конфигурации фабрики<br/>
     * Приходится использовать BeanPostProcessor, т.к. в Spring Kafka нет заготовленной точки расширения фабрики
     */
    @Bean
    public static BeanPostProcessor concurrentKafkaListenerContainerFactoryCustomizer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof ConcurrentKafkaListenerContainerFactory factory
                    && CONTAINER_FACTORY_BEAN_NAME.equals(beanName)) {

                    if (TRUE.equals(factory.isBatchListener())) {
                        log.warn("Недопустимое значение проперти: spring.kafka.listener.type=batch. " +
                                 "Значение изменено на single воизбежании падения консюмеров.");
                        factory.setBatchListener(false);
                    }
                }
                return bean;
            }
        };
    }

    /**
     * Определение общих для всех KafkaListenerContainer пропертей
     */
    @Bean
    public ContainerCustomizer<Object, Object, ConcurrentMessageListenerContainer<Object, Object>> containerCustomizer() {
        return container -> {
            ContainerProperties containerProps = container.getContainerProperties();

            // Консюмеры не запустятся, если будет на мануальный коммит оффсетов
            ContainerProperties.AckMode ackMode = containerProps.getAckMode();
            if (ackMode != ContainerProperties.AckMode.MANUAL
                && ackMode != ContainerProperties.AckMode.MANUAL_IMMEDIATE) {
                log.warn("Topics: {}; Пропертя spring.kafka.listener.type изменена на manual",
                         containerProps.getTopics());
                containerProps.setAckMode(ContainerProperties.AckMode.MANUAL);
            }
        };
    }

    /**
     * Определение общих для всех продюсеров пропертей
     */
    @Bean
    public DefaultKafkaProducerFactoryCustomizer kafkaProducerFactoryCustomizer() {
        return producerFactory -> {
            Map<String, Object> staticProducersProps = Map.of(
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class,
                    JsonSerializer.ADD_TYPE_INFO_HEADERS, true // Оставляем для потребителей, но сами не используем
            );
            producerFactory.updateConfigs(staticProducersProps);
        };
    }

    /**
     * Определение общих для всех консумеров пропертей
     */
    @Bean
    public DefaultKafkaConsumerFactoryCustomizer kafkaConsumerFactoryCustomizer() {
        return consumerFactory -> {
            // Переопределяемые проперти
            Map<String, Object> consumersProps = new HashMap<>();
            consumersProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

            // Если использовать просто JsonDeserializer вместо ErrorHandlingDeserializer, то если придёт кривое сообщение,
            // а JsonDeserializer консюмера не сможет его десериализовать, то консюмер будет бесконечно его пытаться считать
            consumersProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
            consumersProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

            // Нужно, когда мы используем хедер __Type__ при десериализации
            // consumersProps.put(JsonDeserializer.TRUSTED_PACKAGES, "by.bal.server.api.kafka,by.bal.server.api.rest");
            // consumersProps.put(JsonDeserializer.TYPE_MAPPINGS, "by.another.dev.Pet:" + Pet.class.getCanonicalName());

            // При десериализации полагаемся не на хедер __Type__, а на статичную таблицу маппингов топиков на DTO
            // Полезно:
            // 1. Есть не Spring Kafka продюсеры в системе (не будет type хедера)
            // 2. Не нужно прописывать JsonDeserializer.TYPE_MAPPINGS
            // НО, если в одном топике несколько типом сообщений, то усложняется логика мапинга
            consumersProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
            consumersProps.put(JsonDeserializer.VALUE_TYPE_METHOD, getValueTypeMethod());

            consumerFactory.updateConfigs(consumersProps);
        };
    }

    /**
     * Обработчики исключений на уровне KafkaMessageListenerContainer
     * // TODO: Инжектится дефолтный KafkaTemplate с JsonSerializer, который сериализует проблемный json в base64. Хотелось бы в текст просто
     */
    @Bean
    public CommonErrorHandler commonErrorHandler(KafkaTemplate<?, ?> kafkaTemplate) {
        // Дефолтный обработчик для всех исключений: кидаем сообщение в DLT
        var defaultErrorHandler = new DefaultErrorHandler(new DeadLetterPublishingRecoverer(kafkaTemplate));
        defaultErrorHandler.addNotRetryableExceptions(ValidationException.class, IllegalArgumentException.class);
        var delegatingErrorHandler = new CommonDelegatingErrorHandler(defaultErrorHandler);

        // Обработчик DeserializationException: кидаем в отдельный DLT
        var deserializationExceptionRecoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate, (cr, e) -> new TopicPartition(cr.topic() + "-deserialization-dlt", cr.partition())
        );
        deserializationExceptionRecoverer.setLogRecoveryRecord(true);
        DefaultErrorHandler deserializationErrorHandler = new DefaultErrorHandler(deserializationExceptionRecoverer);
        delegatingErrorHandler.addDelegate(DeserializationException.class, deserializationErrorHandler);

        return delegatingErrorHandler;
    }
}
