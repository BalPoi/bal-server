package by.bal.server.balkafkastreams;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Printed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@Slf4j
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Bean
    public KStream<String, String> kStream(StreamsBuilder kStreamBuilder) {
        KStream<String, String> stream = kStreamBuilder.stream("bal-stream-topic");
        log.info(stream.toString());
        stream.peek((k, v) -> log.info("[{}]: {}", k, v), Named.as("PEEKING"))
              .map((k, v) -> KeyValue.pair(k, v.toUpperCase()), Named.as("UPPER_CASING"))
              .to("bal-stream-topic-uppercase");

        // stream.print(Printed.toSysOut());

        // Fluent KStream API
        return stream;
    }
}
