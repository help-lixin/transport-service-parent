package help.lixin.transport.protocol;

import help.lixin.transport.util.Address;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessagingProtocolV1 implements MessagingProtocol {
    private final Address address;

    MessagingProtocolV1(final Address address) {
        this.address = address;
    }

    @Override
    public ProtocolVersion version() {
        return ProtocolVersion.V1;
    }

    @Override
    public MessageToByteEncoder<Object> newEncoder() {
        return new MessageEncoderV1(address);
    }

    @Override
    public ByteToMessageDecoder newDecoder() {
        return new MessageDecoderV1();
    }
}
