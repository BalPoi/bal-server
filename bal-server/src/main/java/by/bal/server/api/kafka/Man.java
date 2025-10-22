package by.bal.server.api.kafka;

import lombok.Builder;
import lombok.Data;

@Builder
public record Man(
        String name,
        Integer age
) {
}
