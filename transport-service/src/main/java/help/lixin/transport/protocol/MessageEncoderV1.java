package help.lixin.transport.protocol;

import help.lixin.transport.util.Address;
import io.netty.buffer.ByteBuf;

import java.net.InetAddress;

public class MessageEncoderV1 extends AbstractMessageEncoder {
    MessageEncoderV1(final Address address) {
        super(address);
    }

    @Override
    protected void encodeAddress(final ProtocolMessage message, final ByteBuf buffer) {
        final InetAddress senderIp = address.address();
        final byte[] senderIpBytes = senderIp.getAddress();
        buffer.writeByte(senderIpBytes.length);
        buffer.writeBytes(senderIpBytes);
        buffer.writeInt(address.port());
    }

    @Override
    protected void encodeMessage(final ProtocolMessage message, final ByteBuf buffer) {
        buffer.writeByte(message.type().id());
        writeLong(buffer, message.id());

        final byte[] payload = message.payload();
        writeInt(buffer, payload.length);
        buffer.writeBytes(payload);
    }

    @Override
    protected void encodeRequest(final ProtocolRequest request, final ByteBuf out) {
        writeString(out, request.subject());
    }

    @Override
    protected void encodeReply(final ProtocolReply reply, final ByteBuf out) {
        out.writeByte(reply.status().id());
    }
}
