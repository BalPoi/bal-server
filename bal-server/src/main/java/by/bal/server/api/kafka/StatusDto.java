package by.bal.server.api.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
public record StatusDto(
        String message,
        Boolean status
) {
}
