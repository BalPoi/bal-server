package by.bal.balkafkadatabind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BalKafkaDatabindApplication {

    public static void main(String[] args) {
        SpringApplication.run(BalKafkaDatabindApplication.class, args);
    }

}
