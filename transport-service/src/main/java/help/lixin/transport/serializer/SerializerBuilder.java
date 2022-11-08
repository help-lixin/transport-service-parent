package help.lixin.transport.serializer;

import help.lixin.transport.util.Builder;

public class SerializerBuilder implements Builder<Serializer> {
    private final Namespace.Builder namespaceBuilder =
            new Namespace.Builder().register(Namespaces.BASIC).nextId(Namespaces.BEGIN_USER_CUSTOM_ID);

    @Override
    public Serializer build() {
        return Serializer.using(namespaceBuilder.build());
    }
}
