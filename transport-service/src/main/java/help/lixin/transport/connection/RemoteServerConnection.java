package help.lixin.transport.connection;

import help.lixin.transport.HandlerRegistry;
import help.lixin.transport.protocol.ProtocolReply;
import help.lixin.transport.protocol.ProtocolRequest;
import io.netty.channel.Channel;

import java.util.Optional;

public   final class RemoteServerConnection extends AbstractServerConnection {
    private static final byte[] EMPTY_PAYLOAD = new byte[0];

    private final Channel channel;

    public RemoteServerConnection(final HandlerRegistry handlers, final Channel channel) {
        super(handlers);
        this.channel = channel;
    }

    @Override
    public void reply(
            final ProtocolRequest message,
            final ProtocolReply.Status status,
            final Optional<byte[]> payload) {
        final ProtocolReply response =
                new ProtocolReply(message.id(), payload.orElse(EMPTY_PAYLOAD), status);
        channel.writeAndFlush(response, channel.voidPromise());
    }
}
