package by.bal.server.api.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@ConditionalOnBooleanProperty(name = "bal-server.api.rest.enabled", matchIfMissing = true)
@RequestMapping("/rx")
@Slf4j
public class MyRxRestController {

    @GetMapping
    Mono<String> hello() {
        return Mono.just("Hello, RxREST!");
    }

    @PostMapping("/person")
    Mono<PersonDto> person(@RequestBody Mono<PersonDto> personDto) {
        return personDto
                .doOnNext(dto -> log.info("POST /person; Body: {}", dto))
                .doOnError(error -> log.error("Error processing DTO: {}", error.getMessage()));
    }

}
