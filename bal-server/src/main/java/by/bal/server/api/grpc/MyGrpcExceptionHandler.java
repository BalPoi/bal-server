package by.bal.server.api.grpc;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBooleanProperty(name = "bal-server.api.grpc.enabled", matchIfMissing = true)
@Slf4j
public class MyGrpcExceptionHandler implements GrpcExceptionHandler {
    @Override
    public StatusException handleException(Throwable e) {
        log.error("Caught exception {}: {}", e.getClass().getSimpleName(), e.getLocalizedMessage());
        if (e instanceof IllegalArgumentException) {
            Metadata metadata = new Metadata();
            metadata.put(Metadata.Key.of("error-code", Metadata.ASCII_STRING_MARSHALLER), "INVALID_ARGUMENT");
            return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asException(metadata);
        }
        return null;
    }
}
