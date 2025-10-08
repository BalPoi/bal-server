package by.bal.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

@RestController
public class HelloController {

    @GetMapping
    Properties hello() {
        return System.getProperties();
    }
}
