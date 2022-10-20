package help.lixin.transport.protocol;

import help.lixin.transport.util.Address;

import java.util.stream.Stream;

public enum ProtocolVersion {
    V1(1) {
        @Override
        public MessagingProtocol createProtocol(final Address address) {
            return new MessagingProtocolV1(address);
        }
    },
    V2(2) {
        @Override
        public MessagingProtocol createProtocol(final Address address) {
            return new MessagingProtocolV2(address);
        }
    };

    private final short version;

    ProtocolVersion(final int version) {
        this.version = (short) version;
    }

    /**
     * Returns the protocol version for the given version number.
     *
     * @param version the version number for which to return the protocol version
     * @return the protocol version for the given version number
     */
    public static ProtocolVersion valueOf(final int version) {
        return Stream.of(values()).filter(v -> v.version() == version).findFirst().orElse(null);
    }

    /**
     * Returns the latest protocol version.
     *
     * @return the latest protocol version
     */
    public static ProtocolVersion latest() {
        return values()[values().length - 1];
    }

    /**
     * Returns the version number.
     *
     * @return the version number
     */
    public short version() {
        return version;
    }

    /**
     * Creates a new protocol instance.
     *
     * @param address the protocol address
     * @return a new protocol instance
     */
    public abstract MessagingProtocol createProtocol(Address address);
}
