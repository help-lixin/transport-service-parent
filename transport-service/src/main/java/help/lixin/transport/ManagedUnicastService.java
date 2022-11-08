package help.lixin.transport;

import java.util.concurrent.CompletableFuture;

public interface ManagedUnicastService extends UnicastService {
    CompletableFuture<UnicastService> start();

    boolean isRunning();

    CompletableFuture<Void> stop();
}

