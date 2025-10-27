package by.bal.server.api.kafka;

import lombok.Builder;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

@Builder
public record Pet(
        String name,
        Integer age,
        String type
) {
    private static final Random RANDOM = new Random();
    private static final List<String> NAMES = List.of("Барсик", "Зорька", "Кеша", "Мурка", "Пушистик", "Рекс", "Снежок", "Шарик", "Сёма", "Error");
    private static final List<String> TYPES = List.of("Кот", "Кролик", "Морская свинка", "Попугай", "Рыбка", "Собака", "Хомяк", "Черепаха");

    public static Stream<Pet> generate() {
        return Stream.generate(() -> Pet.builder()
                                        .name(rand(NAMES))
                                        .type(rand(TYPES))
                                        .age(rand(1, 16))
                                        .build()
        );
    }

    private static int rand(int origin, int bound) {
        return RANDOM.nextInt(origin, bound);
    }

    private static String rand(List<String> strings) {
        return strings.get(RANDOM.nextInt(strings.size()));
    }
}
