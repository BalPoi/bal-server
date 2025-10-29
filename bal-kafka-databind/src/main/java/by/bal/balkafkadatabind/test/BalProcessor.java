package by.bal.balkafkadatabind.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

// @Component
@Slf4j
public class BalProcessor implements Function<String, String> {
    @Override
    public String apply(String s) {
        log.info("[IN ] {}", s);
        String upperCase = s.toUpperCase();
        log.info("[OUT] {}", upperCase);
        return upperCase;
    }
}
