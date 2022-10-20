package help.lixin.transport.protocol;

public final class ProtocolReply extends ProtocolMessage {

    private final Status status;

    public ProtocolReply(final long id, final byte[] payload, final Status status) {
        super(id, payload);
        this.status = status;
    }

    @Override
    public Type type() {
        return Type.REPLY;
    }

    public Status status() {
        return status;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * Message status.
     */
    public enum Status {

        // NOTE: For backwards compatibility enum constant IDs should not be changed.

        /**
         * All ok.
         */
        OK(0),

        /**
         * Response status signifying no registered handler.
         */
        ERROR_NO_HANDLER(1),

        /**
         * Response status signifying an exception handling the message.
         */
        ERROR_HANDLER_EXCEPTION(2),

        /**
         * Response status signifying invalid message structure.
         */
        PROTOCOL_EXCEPTION(3);

        private final int id;

        Status(final int id) {
            this.id = id;
        }

        /**
         * Returns the unique status ID.
         *
         * @return the unique status ID.
         */
        public int id() {
            return id;
        }

        /**
         * Returns the status enum associated with the given ID.
         *
         * @param id the status ID.
         * @return the status enum for the given ID.
         */
        public static Status forId(final int id) {
            switch (id) {
                case 0:
                    return OK;
                case 1:
                    return ERROR_NO_HANDLER;
                case 2:
                    return ERROR_HANDLER_EXCEPTION;
                case 3:
                    return PROTOCOL_EXCEPTION;
                default:
                    throw new IllegalArgumentException("Unknown status ID " + id);
            }
        }
    }
}
