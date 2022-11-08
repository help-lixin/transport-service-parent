package help.lixin.transport;

import com.google.common.util.concurrent.MoreExecutors;
import help.lixin.transport.util.Address;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

public interface UnicastService {

    void unicast(Address address, String subject, byte[] message);

    default void addListener(final String subject, final BiConsumer<Address, byte[]> listener) {
        addListener(subject, listener, MoreExecutors.directExecutor());
    }


    void addListener(String subject, BiConsumer<Address, byte[]> listener, Executor executor);


    void removeListener(String subject, BiConsumer<Address, byte[]> listener);
}
