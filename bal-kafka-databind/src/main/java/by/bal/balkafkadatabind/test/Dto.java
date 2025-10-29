package by.bal.balkafkadatabind.test;

import lombok.Builder;

@Builder
public record Dto(
        Long id,
        String data
) {
}
