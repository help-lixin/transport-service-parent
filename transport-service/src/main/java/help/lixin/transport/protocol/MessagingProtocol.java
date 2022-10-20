package help.lixin.transport.protocol;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public interface MessagingProtocol {

    /**
     * Returns the protocol version.
     *
     * @return the protocol version
     */
    ProtocolVersion version();

    /**
     * Returns a new message encoder.
     *
     * @return a new message encoder
     */
    MessageToByteEncoder<Object> newEncoder();

    /**
     * Returns a new message decoder.
     *
     * @return a new message decoder
     */
    ByteToMessageDecoder newDecoder();
}
