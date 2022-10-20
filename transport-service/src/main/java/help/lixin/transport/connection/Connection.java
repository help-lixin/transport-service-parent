package help.lixin.transport.connection;

import help.lixin.transport.protocol.ProtocolMessage;

public  interface Connection<M extends ProtocolMessage> {

    void dispatch(M message);

    default void close() {
    }
}

