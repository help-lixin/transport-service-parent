package help.lixin.transport.protocol;

import help.lixin.transport.util.Address;
import io.netty.buffer.ByteBuf;

class MessageEncoderV2 extends MessageEncoderV1 {
    MessageEncoderV2(final Address address) {
        super(address);
    }

    @Override
    protected void encodeAddress(final ProtocolMessage message, final ByteBuf buffer) {
        writeString(buffer, address.host());
        buffer.writeInt(address.port());
    }
}