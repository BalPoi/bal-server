package by.bal.server.api.grpc;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import org.springframework.grpc.server.exception.GrpcExceptionHandler;
import org.springframework.stereotype.Component;

@Component
public class MyGrpcExceptionHandler implements GrpcExceptionHandler {
    @Override
    public StatusException handleException(Throwable exception) {
        if (exception instanceof IllegalArgumentException) {
            Metadata metadata = new Metadata();
            metadata.put(Metadata.Key.of("error-code", Metadata.ASCII_STRING_MARSHALLER), "INVALID_ARGUMENT");
            StatusException result = Status.INVALID_ARGUMENT.withDescription(exception.getMessage())
                                                            .asException(metadata);
            return result;
        }
        return null;
    }
}
