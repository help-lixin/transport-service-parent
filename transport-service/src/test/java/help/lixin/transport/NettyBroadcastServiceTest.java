package help.lixin.transport;

import help.lixin.transport.util.Address;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class NettyBroadcastServiceTest {

    private static final Logger LOGGER = getLogger(NettyBroadcastServiceTest.class);

    BroadcastService netty1;
    BroadcastService netty2;

    Address localAddress1;
    Address localAddress2;
    Address groupAddress;

    @Test
    public void testBroadcast() throws Exception {
        netty1.addListener("test", bytes -> {
            System.out.println("revice:" + new String(bytes));
        });
        // 广播
        netty2.broadcast("test", "hello world!!!!".getBytes());
        TimeUnit.SECONDS.sleep(30);
    }

    @Before
    public void setUp() throws Exception {
        localAddress1 = Address.from("127.0.0.1", 5001);
        localAddress2 = Address.from("127.0.0.1", 5001);
        groupAddress = Address.from("230.0.0.1", 1234);

        netty1 = (BroadcastService) NettyBroadcastService.builder()
                .withLocalAddress(localAddress1)
                .withGroupAddress(groupAddress)
                .build()
                .start()
                .join();

        netty2 = (BroadcastService) NettyBroadcastService.builder()
                .withLocalAddress(localAddress2)
                .withGroupAddress(groupAddress)
                .build()
                .start()
                .join();
    }

    @After
    public void tearDown() throws Exception {
        if (netty1 != null) {
            try {
                netty1.stop().join();
            } catch (Exception e) {
                LOGGER.warn("Failed stopping netty1", e);
            }
        }

        if (netty2 != null) {
            try {
                netty2.stop().join();
            } catch (Exception e) {
                LOGGER.warn("Failed stopping netty2", e);
            }
        }
    }
}
