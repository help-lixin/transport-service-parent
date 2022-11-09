package help.lixin.transport;

import help.lixin.transport.config.MessagingConfig;
import help.lixin.transport.gossip.Member;
import help.lixin.transport.gossip.MembershipConfig;
import help.lixin.transport.gossip.Node;
import help.lixin.transport.util.Address;
import help.lixin.transport.util.Version;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class GossipMemberTwoTest {
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

        Address address5002 = Address.from("127.0.0.1", 50002);
        Address address5003 = Address.from("127.0.0.1", 50003);

        MessagingService messagingService5002 = new NettyMessagingService(cluster, address5002, messagingConfig);

        BroadcastService managedBroadcastService5002 = new NettyBroadcastService.Builder()
                .withEnabled(true)
                .withLocalAddress(address5002)
                .withGroupAddress(groupAddress)
                .build();

        // 构建一批种了节点
        Collection<Node> bootstrapLocations = buildBootstrapNodes(50004);

        Member localMember2 = buildMember(50002);
        BootstrapService bootstrapService2 = new DefaultBootstrapService(
                messagingService5002.start().join(),
                managedBroadcastService5002.start().join());
        ClusterMembershipService clusterService2 = new DefaultClusterMembershipService(
                localMember2,
                Version.from("1.0.0"),
                new DefaultNodeDiscoveryService(bootstrapService2, localMember2, new BootstrapDiscoveryProvider(bootstrapLocations)),
                bootstrapService2,
                new MembershipConfig());

        CompletableFuture.allOf(new CompletableFuture[]{clusterService2.start()}).join();
        while (true) {
            TimeUnit.SECONDS.sleep(10);
            System.out.println(clusterService2.getMembers());
        }
    }
}
