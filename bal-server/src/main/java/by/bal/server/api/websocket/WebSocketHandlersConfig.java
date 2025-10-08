package by.bal.server.api.websocket;

import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Configuration
class WebSocketHandlersConfig {

    @Bean
    public HandlerMapping webSocketHandlerMapping(@Nonnull List<WebSocketHandler> webSocketHandlers) {
        HashMap<String, WebSocketHandler> mappings = new HashMap<>();

        for (WebSocketHandler handler : webSocketHandlers) {
            Class<? extends WebSocketHandler> clazz = handler.getClass();
            Optional.ofNullable(clazz.getAnnotation(WebSocketMapping.class))
                    .map(WebSocketMapping::value)
                    .ifPresent(value -> mappings.put(value, handler));
        }

        int order = -1; // before annotated controllers
        return new SimpleUrlHandlerMapping(mappings, order);
    }
}
