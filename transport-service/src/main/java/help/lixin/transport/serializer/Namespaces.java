package help.lixin.transport.serializer;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multisets;
import com.google.common.collect.Sets;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public final class Namespaces {

    public static final Namespace BASIC =
            new Namespace.Builder()
                    .nextId(Namespace.FLOATING_ID)
                    .register(byte[].class)
                    .register(Collections.unmodifiableSet(Collections.emptySet()).getClass())
                    .register(HashMap.class)
                    .register(ConcurrentHashMap.class)
                    .register(CopyOnWriteArraySet.class)
                    .register(
                            ArrayList.class,
                            LinkedList.class,
                            HashSet.class,
                            LinkedHashSet.class,
                            ArrayDeque.class)
                    .register(HashMultiset.class)
                    .register(Multisets.immutableEntry("", 0).getClass())
                    .register(Sets.class)
                    .register(Maps.immutableEntry("a", "b").getClass())
                    .register(Collections.singletonList(1).getClass())
                    .register(Duration.class)
                    .register(Collections.emptySet().getClass())
                    .register(Optional.class)
                    .register(Collections.emptyList().getClass())
                    .register(Collections.singleton(Object.class).getClass())
                    .register(Properties.class)
                    .register(int[].class)
                    .register(long[].class)
                    .register(short[].class)
                    .register(double[].class)
                    .register(float[].class)
                    .register(char[].class)
                    .register(String[].class)
                    .register(boolean[].class)
                    .name("BASIC")
                    .build();

    public static final int BEGIN_USER_CUSTOM_ID = 500;

    private Namespaces() {
    }
}
