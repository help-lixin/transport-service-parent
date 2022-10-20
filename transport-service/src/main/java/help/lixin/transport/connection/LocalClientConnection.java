package help.lixin.transport.connection;

import help.lixin.transport.HandlerRegistry;
import help.lixin.transport.protocol.ProtocolRequest;

import java.util.concurrent.CompletableFuture;

public  final class LocalClientConnection extends AbstractClientConnection {
    private final LocalServerConnection serverConnection;

    public LocalClientConnection(final HandlerRegistry handlers) {
        serverConnection = new LocalServerConnection(handlers, this);
    }

    @Override
    public CompletableFuture<Void> sendAsync(final ProtocolRequest message) {
        serverConnection.dispatch(message);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<byte[]> sendAndReceive(final ProtocolRequest message) {
        final CompletableFuture<byte[]> future = awaitResponseForRequestWithId(message.id());
        serverConnection.dispatch(message);
        return future;
    }

    @Override
    public void close() {
        super.close();
        serverConnection.close();
    }

    @Override
    public String toString() {
        return "LocalClientConnection{}";
    }
}