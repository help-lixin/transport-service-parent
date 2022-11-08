package help.lixin.transport;

import static help.lixin.transport.thread.Threads.namedThreads;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import help.lixin.transport.config.MessagingConfig;
import help.lixin.transport.serializer.AddressSerializer;
import help.lixin.transport.serializer.Namespace;
import help.lixin.transport.serializer.Namespaces;
import help.lixin.transport.serializer.Serializer;
import help.lixin.transport.util.Address;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

public class NettyUnicastService implements ManagedUnicastService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyUnicastService.class);
    private static final Serializer SERIALIZER = Serializer.using(
            new Namespace.Builder()
                    .register(Namespaces.BASIC)
                    .nextId(Namespaces.BEGIN_USER_CUSTOM_ID)
                    .register(Message.class)
                    .register(new AddressSerializer(), Address.class)
                    .build());
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Address address;
    private final MessagingConfig config;
    private final Map<String, Map<BiConsumer<Address, byte[]>, Executor>> listeners = Maps.newConcurrentMap();
    private final AtomicBoolean started = new AtomicBoolean();
    private EventLoopGroup group;
    private DatagramChannel channel;
    private final int preamble;

    public NettyUnicastService(final String clusterId, final Address address, final MessagingConfig config) {
        this.address = address;
        this.config = config;
        preamble = clusterId.hashCode();
    }

    @Override
    public void unicast(final Address address, final String subject, final byte[] payload) {
        if (!started.get()) {
            LOGGER.debug("Failed sending unicast message, unicast service was not started.");
            return;
        }

        final InetAddress resolvedAddress = address.address();
        if (resolvedAddress == null) {
            LOGGER.debug("Failed sending unicast message (destination address {} cannot be resolved)", address);
            return;
        }

        final Message message = new Message(this.address, subject, payload);
        final byte[] bytes = SERIALIZER.encode(message);
        final ByteBuf buf = channel.alloc().buffer(Integer.BYTES + Integer.BYTES + bytes.length);
        buf.writeInt(preamble);
        buf.writeInt(bytes.length).writeBytes(bytes);
        channel.writeAndFlush(new DatagramPacket(buf, new InetSocketAddress(resolvedAddress, address.port())));
    }

    @Override
    public synchronized void addListener(final String subject, final BiConsumer<Address, byte[]> listener, final Executor executor) {
        listeners.computeIfAbsent(subject, s -> Maps.newConcurrentMap()).put(listener, executor);
    }

    @Override
    public synchronized void removeListener(final String subject, final BiConsumer<Address, byte[]> listener) {
        final Map<BiConsumer<Address, byte[]>, Executor> listeners = this.listeners.get(subject);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                this.listeners.remove(subject);
            }
        }
    }

    private CompletableFuture<Void> bootstrap() {
        final Bootstrap serverBootstrap = new Bootstrap().group(group).channel(NioDatagramChannel.class).handler(new SimpleChannelInboundHandler<DatagramPacket>() {
            @Override
            protected void channelRead0(final ChannelHandlerContext context, final DatagramPacket packet) throws Exception {
                handleReceivedPacket(packet);
            }
        }).option(ChannelOption.RCVBUF_ALLOCATOR, new DefaultMaxBytesRecvByteBufAllocator()).option(ChannelOption.SO_BROADCAST, true).option(ChannelOption.SO_REUSEADDR, true);

        return bind(serverBootstrap);
    }

    private void handleReceivedPacket(final DatagramPacket packet) {
        final int preambleReceived = packet.content().readInt();
        if (preambleReceived != preamble) {
            log.warn("Received unicast message from {} which is outside of the cluster. Ignoring the message.", packet.sender());
            return;
        }
        final byte[] payload = new byte[packet.content().readInt()];
        packet.content().readBytes(payload);
        final Message message = SERIALIZER.decode(payload);
        final Map<BiConsumer<Address, byte[]>, Executor> subjectListeners = listeners.get(message.subject());
        if (subjectListeners != null) {
            subjectListeners.forEach((consumer, executor) -> executor.execute(() -> consumer.accept(message.source(), message.payload())));
        }
    }

    /**
     * Binds the given bootstrap to the appropriate interfaces.
     *
     * @param bootstrap the bootstrap to bind
     * @return a future to be completed once the bootstrap has been bound to all interfaces
     */
    private CompletableFuture<Void> bind(final Bootstrap bootstrap) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final int port = config.getPort() != null ? config.getPort() : address.port();
        if (config.getInterfaces().isEmpty()) {
            bind(bootstrap, Lists.newArrayList("0.0.0.0").iterator(), port, future);
        } else {
            bind(bootstrap, config.getInterfaces().iterator(), port, future);
        }
        return future;
    }

    /**
     * Recursively binds the given bootstrap to the given interfaces.
     *
     * @param bootstrap the bootstrap to bind
     * @param ifaces    an iterator of interfaces to which to bind
     * @param port      the port to which to bind
     * @param future    the future to completed once the bootstrap has been bound to all provided
     *                  interfaces
     */
    private void bind(final Bootstrap bootstrap, final Iterator<String> ifaces, final int port, final CompletableFuture<Void> future) {
        if (ifaces.hasNext()) {
            final String iface = ifaces.next();
            bootstrap.bind(iface, port).addListener((ChannelFutureListener) f -> {
                if (f.isSuccess()) {
                    log.info("UDP server listening for connections on {}:{}", iface, port);
                    channel = (DatagramChannel) f.channel();
                    bind(bootstrap, ifaces, port, future);
                } else {
                    log.warn("Failed to bind TCP server to port {}:{} due to {}", iface, port, f.cause());
                    future.completeExceptionally(f.cause());
                }
            });
        } else {
            future.complete(null);
        }
    }

    public CompletableFuture<UnicastService> start() {
        group = new NioEventLoopGroup(0, namedThreads("netty-unicast-event-nio-client-%d", log));
        return bootstrap().thenRun(() -> started.set(true)).thenApply(v -> this);
    }

    public boolean isRunning() {
        return started.get();
    }

    public CompletableFuture<Void> stop() {
        if (!started.compareAndSet(true, false)) {
            return CompletableFuture.completedFuture(null);
        }

        if (channel != null) {
            final CompletableFuture<Void> future = new CompletableFuture<>();
            channel.close().addListener(f -> group.shutdownGracefully().addListener(f2 -> {
                future.complete(null);
            }));
            channel = null;
            return future;
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Internal unicast service message.
     */
    static final class Message {
        private final Address source;
        private final String subject;
        private final byte[] payload;

        Message(final Address source, final String subject, final byte[] payload) {
            this.source = source;
            this.subject = subject;
            this.payload = payload;
        }

        Address source() {
            return source;
        }

        String subject() {
            return subject;
        }

        byte[] payload() {
            return payload;
        }
    }
}
