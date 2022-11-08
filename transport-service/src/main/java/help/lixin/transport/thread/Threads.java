package help.lixin.transport.thread;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;

import java.util.concurrent.ThreadFactory;

public final class Threads {

    public static ThreadFactory namedThreads(final String pattern, final Logger log) {
        return new ThreadFactoryBuilder()
                .setNameFormat(pattern)
                .setThreadFactory(new TransportThreadFactory())
                .setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception on " + t.getName(), e))
                .build();
    }
}
