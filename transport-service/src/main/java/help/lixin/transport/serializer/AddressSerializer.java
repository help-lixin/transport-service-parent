package help.lixin.transport.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import help.lixin.transport.util.Address;

/**
 * Address serializer.
 */
public class AddressSerializer extends com.esotericsoftware.kryo.Serializer<Address> {
    @Override
    public void write(final Kryo kryo, final Output output, final Address address) {
        output.writeString(address.host());
        output.writeInt(address.port());
    }

    @Override
    public Address read(Kryo kryo, Input input, Class<Address> type) {
        final String host = input.readString();
        final int port = input.readInt();
        return Address.from(host, port);
    }
}
