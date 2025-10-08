package by.bal.server.api.websocket;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebSocketMapping {

    String value() default "";
}
