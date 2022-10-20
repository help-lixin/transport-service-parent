package help.lixin.transport.protocol;

public abstract class ProtocolMessage {

    private final long id;
    private final byte[] payload;

    protected ProtocolMessage(final long id, final byte[] payload) {
        this.id = id;
        this.payload = payload;
    }

    public abstract Type type();

    public boolean isRequest() {
        return type() == Type.REQUEST;
    }

    public boolean isReply() {
        return type() == Type.REPLY;
    }

    public long id() {
        return id;
    }

    public byte[] payload() {
        return payload;
    }

    /**
     * Internal message type.
     */
    public enum Type {
        REQUEST(1),
        REPLY(2);

        private final int id;

        Type(final int id) {
            this.id = id;
        }

        /**
         * Returns the unique message type ID.
         *
         * @return the unique message type ID.
         */
        public int id() {
            return id;
        }

        /**
         * Returns the message type enum associated with the given ID.
         *
         * @param id the type ID.
         * @return the type enum for the given ID.
         */
        public static Type forId(final int id) {
            switch (id) {
                case 1:
                    return REQUEST;
                case 2:
                    return REPLY;
                default:
                    throw new IllegalArgumentException("Unknown status ID " + id);
            }
        }
    }
}
