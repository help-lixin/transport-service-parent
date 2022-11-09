package help.lixin.gossip;

import help.lixin.transport.*;
import help.lixin.transport.config.MessagingConfig;
import help.lixin.transport.util.Address;
import help.lixin.transport.util.Version;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class GossipMemberThreeTest {
    private Member buildMember(int memberId) {
        return Member.builder(String.valueOf(memberId))
                .withAddress("localhost", memberId)
                .build();
    }

    private Collection<Node> buildBootstrapNodes(int nodes) {
        return IntStream.range(50001, nodes + 1)
                .mapToObj(id -> Node.builder()
                        .withId(String.valueOf(id))
                        .withAddress(Address.from("localhost", id))
                        .build())
                .collect(Collectors.toList());
    }


    @Test
    public void testGossip() throws Exception {
        String cluster = "test";
        MessagingConfig messagingConfig = new MessagingConfig();
        // 广播地址
        Address groupAddress = Address.from("230.0.0.1", 1234);

        Address address5003 = Address.from("127.0.0.1", 50003);

        MessagingService messagingService5003 = new NettyMessagingService(cluster, address5003, messagingConfig);

        BroadcastService managedBroadcastService5003 = new NettyBroadcastService.Builder()
                .withEnabled(true)
                .withLocalAddress(address5003)
                .withGroupAddress(groupAddress)
                .build();


        // 构建一批种了节点
        Collection<Node> bootstrapLocations = buildBootstrapNodes(50004);

        Member localMember3 = buildMember(50003);
        BootstrapService bootstrapService3 = new DefaultBootstrapService(
                messagingService5003.start().join(),
                managedBroadcastService5003.start().join());
        ClusterMembershipService clusterService3 = new DefaultClusterMembershipService(
                localMember3,
                Version.from("1.0.1"),
                new DefaultNodeDiscoveryService(bootstrapService3, localMember3, new BootstrapDiscoveryProvider(bootstrapLocations)),
                bootstrapService3,
                new MembershipConfig());

        CompletableFuture.allOf(new CompletableFuture[]{
                clusterService3.start()}).join();

        while (true) {
            TimeUnit.SECONDS.sleep(10);
            System.out.println(clusterService3.getMembers());
        }
    }
}
