package by.bal.server.api.rest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rest")
@Slf4j
public class MyRestController {

    @GetMapping
    String hello() {
        return "Hello, REST!";
    }

    @PostMapping("/person")
    PersonDto person(@RequestBody PersonDto personDto) {
        log.info("POST /person; Body: {}", personDto);
        return personDto;
    }

}
