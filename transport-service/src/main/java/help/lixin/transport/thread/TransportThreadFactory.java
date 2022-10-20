package help.lixin.transport.thread;

import java.util.concurrent.ThreadFactory;

public class TransportThreadFactory implements ThreadFactory {
    @Override
    public Thread newThread(final Runnable r) {
        return new Thread(r);
    }
}