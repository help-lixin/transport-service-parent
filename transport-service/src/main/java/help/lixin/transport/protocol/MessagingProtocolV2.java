package help.lixin.transport.protocol;

import help.lixin.transport.util.Address;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessagingProtocolV2 implements MessagingProtocol {
    private final Address address;

    MessagingProtocolV2(final Address address) {
        this.address = address;
    }

    @Override
    public ProtocolVersion version() {
        return ProtocolVersion.V2;
    }

    @Override
    public MessageToByteEncoder<Object> newEncoder() {
        return new MessageEncoderV2(address);
    }

    @Override
    public ByteToMessageDecoder newDecoder() {
        return new MessageDecoderV2();
    }
}
