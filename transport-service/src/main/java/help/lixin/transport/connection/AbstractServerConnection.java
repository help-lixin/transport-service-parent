package help.lixin.transport.connection;

import help.lixin.transport.HandlerRegistry;
import help.lixin.transport.protocol.ProtocolReply;
import help.lixin.transport.protocol.ProtocolRequest;
import help.lixin.transport.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.BiConsumer;

public abstract class AbstractServerConnection implements ServerConnection {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final HandlerRegistry handlers;

    public AbstractServerConnection(final HandlerRegistry handlers) {
        this.handlers = handlers;
    }

    @Override
    public void dispatch(final ProtocolRequest message) {
        final String subject = message.subject();
        final BiConsumer<ProtocolRequest, ServerConnection> handler = handlers.get(subject);
        if (handler != null) {
            log.trace("Received message type {} from {}", subject, message.sender());
            handler.accept(message, this);
        } else {
            log.debug("No handler for message type {} from {}", subject, message.sender());

            byte[] subjectBytes = null;
            if (subject != null) {
                subjectBytes = StringUtil.getBytes(subject);
            }

            reply(message, ProtocolReply.Status.ERROR_NO_HANDLER, Optional.ofNullable(subjectBytes));
        }
    }
}
