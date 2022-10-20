package help.lixin.transport.connection;

import com.google.common.collect.Maps;
import help.lixin.transport.connection.ClientConnection;
import help.lixin.transport.exception.MessagingException;
import help.lixin.transport.protocol.ProtocolReply;
import help.lixin.transport.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractClientConnection implements ClientConnection {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // since all messages go through the same entry point, we keep a map of message IDs -> response
    // futures to allow dynamic dispatch of messages to the right response future
    private final Map<Long, CompletableFuture<byte[]>> responseFutures = Maps.newConcurrentMap();

    @Override
    public void dispatch(final ProtocolReply message) {
        final CompletableFuture<byte[]> responseFuture = responseFutures.remove(message.id());
        if (responseFuture != null) {
            if (message.status() == ProtocolReply.Status.OK) {
                responseFuture.complete(message.payload());
            } else if (message.status() == ProtocolReply.Status.ERROR_NO_HANDLER) {
                final String subject = extractMessage(message);
                responseFuture.completeExceptionally(new MessagingException.NoRemoteHandler(subject));
            } else if (message.status() == ProtocolReply.Status.ERROR_HANDLER_EXCEPTION) {
                final String exceptionMessage = extractMessage(message);
                responseFuture.completeExceptionally(
                        new MessagingException.RemoteHandlerFailure(exceptionMessage));
            } else if (message.status() == ProtocolReply.Status.PROTOCOL_EXCEPTION) {
                responseFuture.completeExceptionally(new MessagingException.ProtocolException());
            }
        } else {
            log.debug(
                    "Received a reply for message id:[{}] but was unable to locate the request handle",
                    message.id());
        }
    }

    private String extractMessage(final ProtocolReply message) {
        final byte[] payload = message.payload();
        String exceptionMessage = null;

        if (payload != null && payload.length > 0) {
            exceptionMessage = StringUtil.fromBytes(payload);
        }
        return exceptionMessage;
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            for (final CompletableFuture<byte[]> responseFuture : responseFutures.values()) {
                responseFuture.completeExceptionally(
                        new ConnectException(String.format("Connection %s was closed", this)));
            }
        }
    }

    /**
     * Registers a request to await a response. The future returned is already set up to remove itself
     * from the registry to ensure cleanup.
     *
     * <p>Will return the same future if there already exists one for a given ID.
     *
     * @param id the request ID
     * @return the response future for the given request ID
     */
    protected CompletableFuture<byte[]> awaitResponseForRequestWithId(final long id) {
        final CompletableFuture<byte[]> responseFuture =
                responseFutures.computeIfAbsent(id, ignored -> new CompletableFuture<>());
        responseFuture.whenComplete((result, error) -> responseFutures.remove(id));

        return responseFuture;
    }
}
