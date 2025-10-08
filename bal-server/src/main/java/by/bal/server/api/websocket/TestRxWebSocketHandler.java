package by.bal.server.api.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@WebSocketMapping("/ws/test")
@Slf4j
public class TestRxWebSocketHandler implements WebSocketHandler {

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("New WebSocket session: {}", session.getId());

        Flux<String> outSource = Flux.interval(Duration.ofSeconds(1))
                                     .map(String::valueOf)
                                     .doOnNext(it -> log.info("OUT: {}", it));
        Mono<Void> out = session.send(outSource.map(session::textMessage));

        Mono<Void> in = session.receive()
                               .map(WebSocketMessage::getPayloadAsText)
                               .doOnNext(it -> log.info("IN: {}", it))
                               .doFinally(signal -> log.info("WebSocket session closes: {}", signal))
                               .then();

        return Mono.zip(in, out).then();
    }

}
