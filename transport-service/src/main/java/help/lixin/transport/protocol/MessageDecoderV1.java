package help.lixin.transport.protocol;

import help.lixin.transport.util.Address;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetAddress;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;

public class MessageDecoderV1 extends AbstractMessageDecoder {

    private DecoderState currentState = DecoderState.READ_SENDER_IP;
    private InetAddress senderIp;
    private int senderPort;
    private Address senderAddress;
    private ProtocolMessage.Type type;
    private long messageId;
    private int contentLength;
    private byte[] content;
    private int subjectLength;

    @Override
    @SuppressWarnings("squid:S128") // suppress switch fall through warning
    protected void decode(
            final ChannelHandlerContext context, final ByteBuf buffer, final List<Object> out)
            throws Exception {

        switch (currentState) {
            case READ_SENDER_IP:
                if (buffer.readableBytes() < Byte.BYTES) {
                    return;
                }
                buffer.markReaderIndex();
                final int octetsLength = buffer.readByte();
                if (buffer.readableBytes() < octetsLength) {
                    buffer.resetReaderIndex();
                    return;
                }

                final byte[] octets = new byte[octetsLength];
                buffer.readBytes(octets);
                senderIp = InetAddress.getByAddress(octets);
                currentState = DecoderState.READ_SENDER_PORT;
            case READ_SENDER_PORT:
                if (buffer.readableBytes() < Integer.BYTES) {
                    return;
                }
                senderPort = buffer.readInt();
                senderAddress = new Address(senderIp.getHostName(), senderPort, senderIp);
                currentState = DecoderState.READ_TYPE;
            case READ_TYPE:
                if (buffer.readableBytes() < Byte.BYTES) {
                    return;
                }
                type = ProtocolMessage.Type.forId(buffer.readByte());
                currentState = DecoderState.READ_MESSAGE_ID;
            case READ_MESSAGE_ID:
                try {
                    messageId = readLong(buffer);
                } catch (final Escape e) {
                    return;
                }
                currentState = DecoderState.READ_CONTENT_LENGTH;
            case READ_CONTENT_LENGTH:
                try {
                    contentLength = readInt(buffer);
                } catch (final Escape e) {
                    return;
                }
                currentState = DecoderState.READ_CONTENT;
            case READ_CONTENT:
                if (buffer.readableBytes() < contentLength) {
                    return;
                }
                if (contentLength > 0) {
                    // TODO: Perform a sanity check on the size before allocating
                    content = new byte[contentLength];
                    buffer.readBytes(content);
                } else {
                    content = EMPTY_PAYLOAD;
                }

                switch (type) {
                    case REQUEST:
                        currentState = DecoderState.READ_SUBJECT_LENGTH;
                        break;
                    case REPLY:
                        currentState = DecoderState.READ_STATUS;
                        break;
                    default:
                        checkState(false, "Must not be here");
                }
                break;
            default:
                break;
        }

        switch (type) {
            case REQUEST:
                switch (currentState) {
                    case READ_SUBJECT_LENGTH:
                        if (buffer.readableBytes() < Short.BYTES) {
                            return;
                        }
                        subjectLength = buffer.readShort();
                        currentState = DecoderState.READ_SUBJECT;
                    case READ_SUBJECT:
                        if (buffer.readableBytes() < subjectLength) {
                            return;
                        }
                        final String subject = readString(buffer, subjectLength);
                        final ProtocolRequest message =
                                new ProtocolRequest(messageId, senderAddress, subject, content);
                        out.add(message);
                        currentState = DecoderState.READ_TYPE;
                        break;
                    default:
                        break;
                }
                break;
            case REPLY:
                switch (currentState) {
                    case READ_STATUS:
                        if (buffer.readableBytes() < Byte.BYTES) {
                            return;
                        }
                        final ProtocolReply.Status status = ProtocolReply.Status.forId(buffer.readByte());
                        final ProtocolReply message = new ProtocolReply(messageId, content, status);
                        out.add(message);
                        currentState = DecoderState.READ_TYPE;
                        break;
                    default:
                        break;
                }
                break;
            default:
                checkState(false, "Must not be here");
        }
    }

    /**
     * V1 decoder state.
     */
    enum DecoderState {
        READ_TYPE,
        READ_MESSAGE_ID,
        READ_SENDER_IP,
        READ_SENDER_PORT,
        READ_SUBJECT_LENGTH,
        READ_SUBJECT,
        READ_STATUS,
        READ_CONTENT_LENGTH,
        READ_CONTENT
    }
}

