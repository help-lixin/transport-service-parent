package help.lixin.transport.connection;

import help.lixin.transport.HandlerRegistry;
import help.lixin.transport.protocol.ProtocolReply;
import help.lixin.transport.protocol.ProtocolRequest;

import java.util.Optional;

public   final class LocalServerConnection extends AbstractServerConnection {
    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    private volatile LocalClientConnection clientConnection;

    LocalServerConnection(final HandlerRegistry handlers, final LocalClientConnection clientConnection) {
        super(handlers);
        this.clientConnection = clientConnection;
    }

    @Override
    public void reply(final ProtocolRequest message, final ProtocolReply.Status status, final Optional<byte[]> payload) {
        final LocalClientConnection clientConnection = this.clientConnection;
        if (clientConnection != null) {
            clientConnection.dispatch(new ProtocolReply(message.id(), payload.orElse(EMPTY_PAYLOAD), status));
        }
    }
}
