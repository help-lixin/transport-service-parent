package help.lixin.transport.connection;

import help.lixin.transport.protocol.ProtocolReply;
import help.lixin.transport.protocol.ProtocolRequest;

import java.util.Optional;

public   interface ServerConnection extends Connection<ProtocolRequest> {

    void reply(ProtocolRequest message, ProtocolReply.Status status, Optional<byte[]> payload);

    default void close() {
    }
}
