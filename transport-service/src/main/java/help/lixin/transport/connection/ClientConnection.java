package help.lixin.transport.connection;

import help.lixin.transport.protocol.ProtocolReply;
import help.lixin.transport.protocol.ProtocolRequest;

import java.util.concurrent.CompletableFuture;

public  interface ClientConnection extends Connection<ProtocolReply> {

    /**
     * Sends a message to the other side of the connection.
     *
     * @param message the message to send
     * @return a completable future to be completed once the message has been sent
     */
    CompletableFuture<Void> sendAsync(ProtocolRequest message);

    /**
     * Sends a message to the other side of the connection, awaiting a reply.
     *
     * @param message the message to send
     * @return a completable future to be completed once a reply is received or the request times out
     */
    CompletableFuture<byte[]> sendAndReceive(ProtocolRequest message);

    /**
     * Closes the connection.
     */
    @Override
    default void close() {
    }
}
