package help.lixin.transport;

import static org.assertj.core.api.Assertions.assertThat;

import help.lixin.transport.config.MessagingConfig;
import help.lixin.transport.util.Address;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class NettyMessagingServiceCompressionTest {
    @Test
    public void shouldSendAndReceiveMessage() {
        final var config = new MessagingConfig().setShutdownQuietPeriod(Duration.ofMillis(50)).setCompressionAlgorithm(MessagingConfig.CompressionAlgorithm.GZIP.NONE);

        final var senderAddress = Address.from("127.0.0.1:1025");
        final var senderNetty = (MessagingService) new NettyMessagingService("test", senderAddress, config).start().join();

        final var receiverAddress = Address.from("127.0.0.1:1026");
        final var receiverNetty = (MessagingService) new NettyMessagingService("test", receiverAddress, config).start().join();

        final String subject = "subject";
        final String requestString = "message";
        final String responseString = "success";
        receiverNetty.registerHandler(subject, (m, payload) -> {
            final String message = new String(payload);
            assertThat(message).isEqualTo(requestString);
            return CompletableFuture.completedFuture(responseString.getBytes());
        });

        // when
        final CompletableFuture<byte[]> response = senderNetty.sendAndReceive(receiverAddress, subject, requestString.getBytes());

        // then
        final var result = response.join();
        assertThat(new String(result)).isEqualTo(responseString);

        // teardown
        senderNetty.stop();
        receiverNetty.stop();
    }
}