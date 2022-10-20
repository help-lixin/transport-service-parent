package help.lixin.transport;

import help.lixin.transport.connection.ServerConnection;
import help.lixin.transport.protocol.ProtocolRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class HandlerRegistry {
    private final Map<String, BiConsumer<ProtocolRequest, ServerConnection>> handlers =
            new ConcurrentHashMap<>();

    /**
     * Registers a message type handler.
     *
     * @param type    the message type
     * @param handler the message handler
     */
    public void register(final String type, final BiConsumer<ProtocolRequest, ServerConnection> handler) {
        handlers.put(type, handler);
    }

    /**
     * Unregisters a message type handler.
     *
     * @param type the message type
     */
    public void unregister(final String type) {
        handlers.remove(type);
    }

    /**
     * Looks up a message type handler.
     *
     * @param type the message type
     * @return the message handler or {@code null} if no handler of the given type is registered
     */
    public BiConsumer<ProtocolRequest, ServerConnection> get(final String type) {
        return handlers.get(type);
    }
}
