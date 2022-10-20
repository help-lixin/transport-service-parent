package help.lixin.transport.protocol;

import help.lixin.transport.util.Address;

public final class ProtocolRequest extends ProtocolMessage {
    private final Address sender;
    private final String subject;

    public ProtocolRequest(
            final long id, final Address sender, final String subject, final byte[] payload) {
        super(id, payload);
        this.sender = sender;
        this.subject = subject;
    }

    @Override
    public Type type() {
        return Type.REQUEST;
    }

    public String subject() {
        return subject;
    }

    public Address sender() {
        return sender;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
