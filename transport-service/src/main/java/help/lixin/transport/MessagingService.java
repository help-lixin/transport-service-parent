package help.lixin.transport;

import help.lixin.transport.util.Address;

import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface MessagingService {

    Address address();

    Collection<Address> bindingAddresses();

    default CompletableFuture<Void> sendAsync(
            final Address address, final String type, final byte[] payload) {
        return sendAsync(address, type, payload, true);
    }

    CompletableFuture<Void> sendAsync(
            Address address, String type, byte[] payload, boolean keepAlive);

    default CompletableFuture<byte[]> sendAndReceive(
            final Address address, final String type, final byte[] payload) {
        return sendAndReceive(address, type, payload, true);
    }

    CompletableFuture<byte[]> sendAndReceive(
            Address address, String type, byte[] payload, boolean keepAlive);

    default CompletableFuture<byte[]> sendAndReceive(
            final Address address, final String type, final byte[] payload, final Executor executor) {
        return sendAndReceive(address, type, payload, true, executor);
    }


    CompletableFuture<byte[]> sendAndReceive(
            Address address, String type, byte[] payload, boolean keepAlive, Executor executor);

    default CompletableFuture<byte[]> sendAndReceive(
            final Address address, final String type, final byte[] payload, final Duration timeout) {
        return sendAndReceive(address, type, payload, true, timeout);
    }

    CompletableFuture<byte[]> sendAndReceive(
            Address address, String type, byte[] payload, boolean keepAlive, Duration timeout);

    CompletableFuture<byte[]> sendAndReceive(
            Address address,
            String type,
            byte[] payload,
            boolean keepAlive,
            Duration timeout,
            Executor executor);

    void registerHandler(String type, BiConsumer<Address, byte[]> handler, Executor executor);

    void registerHandler(String type, BiFunction<Address, byte[], byte[]> handler, Executor executor);

    void registerHandler(String type, BiFunction<Address, byte[], CompletableFuture<byte[]>> handler);

    void unregisterHandler(String type);

    boolean isRunning();

    CompletableFuture<MessagingService> start();

    CompletableFuture<Void> stop();
}
