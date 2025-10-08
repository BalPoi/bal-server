package by.bal.server.api;

import java.util.List;
import java.util.Map;

public record PersonDto(
        String name,
        Integer age,
        Float height,
        Boolean marred,
        List<String> petsNames,
        Map<String, String> tags
) {
}
