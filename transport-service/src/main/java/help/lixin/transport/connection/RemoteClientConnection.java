package help.lixin.transport.connection;

import help.lixin.transport.protocol.ProtocolRequest;
import io.netty.channel.Channel;

import java.util.concurrent.CompletableFuture;

public final class RemoteClientConnection extends AbstractClientConnection {
    private final Channel channel;

    public RemoteClientConnection(final Channel channel) {
        this.channel = channel;
    }

    @Override
    public CompletableFuture<Void> sendAsync(final ProtocolRequest message) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        channel
                .writeAndFlush(message)
                .addListener(
                        channelFuture -> {
                            if (!channelFuture.isSuccess()) {
                                future.completeExceptionally(channelFuture.cause());
                            } else {
                                future.complete(null);
                            }
                        });
        return future;
    }

    @Override
    public CompletableFuture<byte[]> sendAndReceive(final ProtocolRequest message) {
        final CompletableFuture<byte[]> responseFuture = awaitResponseForRequestWithId(message.id());
        channel
                .writeAndFlush(message)
                .addListener(
                        channelFuture -> {
                            if (!channelFuture.isSuccess()) {
                                responseFuture.completeExceptionally(channelFuture.cause());
                            }
                        });
        return responseFuture;
    }

    @Override
    public String toString() {
        return "RemoteClientConnection{channel=" + channel + "}";
    }
}
