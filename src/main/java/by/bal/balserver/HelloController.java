package by.bal.balserver;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    String hello() {
        Foo bar = new Foo("Bar", 15);
        System.out.println(bar.getName());
        System.out.println(bar.getAge());
        return "Hello world!";
    }
}
