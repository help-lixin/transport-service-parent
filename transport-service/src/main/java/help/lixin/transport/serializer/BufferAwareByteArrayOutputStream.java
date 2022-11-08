package help.lixin.transport.serializer;

import java.io.ByteArrayOutputStream;

/** Exposes protected byte array length in {@link ByteArrayOutputStream}. */
final class BufferAwareByteArrayOutputStream extends ByteArrayOutputStream {

    BufferAwareByteArrayOutputStream(final int size) {
        super(size);
    }

    int getBufferSize() {
        return buf.length;
    }
}
