package help.lixin.transport.exception;

import java.io.IOException;

public class MessagingException extends IOException {

    public MessagingException() {
        super();
    }

    public MessagingException(final String message) {
        super(message);
    }

    public MessagingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception indicating no remote registered remote handler.
     */
    public static class NoRemoteHandler extends MessagingException {
        public NoRemoteHandler(String subject) {
            super(
                    String.format(
                            "No remote message handler registered for this message, subject %s", subject));
        }
    }

    /**
     * Exception indicating handler failure.
     */
    public static class RemoteHandlerFailure extends MessagingException {
        public RemoteHandlerFailure(String message) {
            super(String.format("Remote handler failed to handle message, cause: %s", message));
        }
    }

    /**
     * Exception indicating failure due to invalid message structure such as an incorrect preamble.
     */
    public static class ProtocolException extends MessagingException {
        public ProtocolException() {
            super("Failed to process message due to invalid message structure");
        }
    }
}
