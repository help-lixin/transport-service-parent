package help.lixin.transport.serializer;

import static org.slf4j.LoggerFactory.getLogger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.SerializerFactory.CompatibleFieldSerializerFactory;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer.CompatibleFieldSerializerConfig;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.util.Pool;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;

public class CompatibleKryoPool extends Pool<Kryo> {

    private static int FLOATING_ID = -1;

    private static final Logger LOGGER = getLogger(CompatibleKryoPool.class);
    private final String friendlyName;
    private final ClassLoader classLoader;
    private final List<Namespace.RegistrationBlock> registeredBlocks;

    public CompatibleKryoPool(
            final String friendlyName,
            final ClassLoader classLoader,
            final List<Namespace.RegistrationBlock> registeredBlocks) {
        super(true, true);

        this.friendlyName = friendlyName;
        this.classLoader = classLoader;
        this.registeredBlocks = registeredBlocks;
    }

    @Override
    protected Kryo create() {
        final Kryo kryo = new Kryo();

        // These settings allow unknown data to be skipped (for forward compatibility)
        // https://github.com/EsotericSoftware/kryo#compatiblefieldserializer
        final CompatibleFieldSerializerConfig serializerConfig = new CompatibleFieldSerializerConfig();
        serializerConfig.setChunkedEncoding(true);
        serializerConfig.setReadUnknownFieldData(false);
        kryo.setDefaultSerializer(new CompatibleFieldSerializerFactory(serializerConfig));
        kryo.setRegistrationRequired(true);
        kryo.setClassLoader(classLoader);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

        for (final Namespace.RegistrationBlock block : registeredBlocks) {
            int id = block.begin();
            if (id == FLOATING_ID) {
                id = kryo.getNextRegistrationId();
            }
            for (final Pair<Class<?>[], Serializer<?>> entry : block.types()) {
                register(kryo, entry.getLeft(), entry.getRight(), id++);
            }
        }
        kryo.reset();
        return kryo;
    }

    private void register(
            final Kryo kryo, final Class<?>[] types, final Serializer<?> serializer, final int id) {
        final Registration existing = kryo.getRegistration(id);
        if (existing != null && !contains(types, existing)) {
            LOGGER.error(
                    "{}: Failed to register {} as {}. {} was already registered with that id.",
                    friendlyName,
                    types,
                    id,
                    existing.getType());

            throw new IllegalStateException(
                    String.format(
                            "Failed to register %s as %s, %s was already registered.",
                            Arrays.toString(types), id, existing.getType()));
        }

        for (final Class<?> type : types) {
            Registration r = null;
            if (serializer == null) {
                r = kryo.register(type, id);
            } else if (type.isInterface()) {
                kryo.addDefaultSerializer(type, serializer);
            } else {
                r = kryo.register(type, serializer, id);
            }
            if (r != null && r.getId() != id) {
                LOGGER.trace("Skipping {}: {} already registered as {}.", r.getType(), r.getId(), id);
            }
        }
    }

    private boolean contains(final Class<?>[] types, final Registration existing) {
        boolean matches = false;
        for (final Class<?> type : types) {
            if (existing.getType() == type) {
                matches = true;
                break;
            }
        }
        return matches;
    }
}
