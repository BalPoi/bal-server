package by.bal.server.api.kafka.config;

import by.bal.server.api.kafka.Man;
import by.bal.server.api.kafka.Pet;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Headers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Конфигурация, которая настраивает сопоставление топиков определённым java классам для десериализации содержимого
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "bal-server.api.kafka.mode", havingValue = "normal")
@Slf4j
public class KafkaTopicValueTypeMappingConfig {
    private static final String KAFKA_VALUE_TYPE_METHOD_NOT_FOUND =
            "Не удалось найти метод для мапинга Kafka топика на DTO класс для JsonDeserializer.";

    // TODO: Т.к. названия топиков стендозависимы, то нужно заменить хардкод на получение топиков из пропертей
    private static final Map<String, JavaType> MAPPING = Map.of(
            "bal-topic", TypeFactory.defaultInstance().constructType(Man.class),
            "bal-topic-pet", TypeFactory.defaultInstance().constructType(Pet.class)
    );

    /**
     * Метод для проперти JsonDeserializer'а консюмеров {@link JsonDeserializer#VALUE_TYPE_METHOD}
     */
    public static JavaType kafkaTopicValueTypeMapping(String topic, byte[] data, Headers headers) {
        return MAPPING.get(topic);
    }

    /**
     * Должен находить метод {@link KafkaTopicValueTypeMappingConfig#kafkaTopicValueTypeMapping(String, byte[], Headers)}
     * и формировать строку вида "classCanonicalName.methodName" для проперти {@link JsonDeserializer#VALUE_TYPE_METHOD}
     */
    public static String getValueTypeMethod() {
        // Класс, в котором мы ищем подходящий метод
        Class<KafkaTopicValueTypeMappingConfig> configClass = KafkaTopicValueTypeMappingConfig.class;

        String classCanonicalName = configClass.getCanonicalName();
        String methodName = null;

        for (Method method : configClass.getDeclaredMethods()) {
            if (method.getReturnType() == JavaType.class
                && Modifier.isStatic(method.getModifiers())
                && Modifier.isPublic(method.getModifiers())) {
                methodName = method.getName();
                break;
            }
        }

        if (methodName == null) {
            log.error(KAFKA_VALUE_TYPE_METHOD_NOT_FOUND);
            throw new RuntimeException(KAFKA_VALUE_TYPE_METHOD_NOT_FOUND);
        }

        return classCanonicalName + "." + methodName;
    }
}
