package by.bal.balserver.api.rest;

import by.bal.balserver.api.PersonDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
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
