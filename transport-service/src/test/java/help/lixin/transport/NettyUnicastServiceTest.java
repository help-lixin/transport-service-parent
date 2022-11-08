package help.lixin.transport;

import help.lixin.transport.config.MessagingConfig;
import help.lixin.transport.util.Address;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.slf4j.LoggerFactory.getLogger;

public class NettyUnicastServiceTest {

    private static final Logger LOGGER = getLogger(NettyUnicastServiceTest.class);

    ManagedUnicastService service1;
    ManagedUnicastService service2;

    Address address1;
    Address address2;

    @Test
    public void testUnicast() throws Exception {
        service1.addListener("test", (address, payload) -> {
            assertEquals(address2, address);
            assertArrayEquals("Hello world!".getBytes(), payload);
        });
        service2.unicast(address1, "test", "Hello world!".getBytes());
        TimeUnit.SECONDS.sleep(5);
    }


    @Before
    public void setUp() throws Exception {
        address1 = Address.from("127.0.0.1", 1025);
        address2 = Address.from("127.0.0.1", 1026);

        final String clusterId = "testClusterId";
        service1 = new NettyUnicastService(clusterId, address1, new MessagingConfig());
        service1.start().join();

        service2 = new NettyUnicastService(clusterId, address2, new MessagingConfig());
        service2.start().join();
    }

    @After
    public void tearDown() throws Exception {
        if (service1 != null) {
            try {
                service1.stop().join();
            } catch (final Exception e) {
                LOGGER.warn("Failed stopping netty1", e);
            }
        }

        if (service2 != null) {
            try {
                service2.stop().join();
            } catch (final Exception e) {
                LOGGER.warn("Failed stopping netty2", e);
            }
        }
    }
}
