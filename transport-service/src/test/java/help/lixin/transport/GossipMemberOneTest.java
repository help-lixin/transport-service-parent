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

public class GossipMemberOneTest {
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

        Address address5001 = Address.from("127.0.0.1", 50001);

        MessagingService messagingService5001 = new NettyMessagingService(cluster, address5001, messagingConfig);


        BroadcastService managedBroadcastService5001 = new NettyBroadcastService.Builder()
                .withEnabled(true)
                .withLocalAddress(address5001)
                .withGroupAddress(groupAddress)
                .build();
        // 构建一批种了节点
        Collection<Node> bootstrapLocations = buildBootstrapNodes(50004);

        Member localMember1 = buildMember(50001);
        BootstrapService bootstrapService1 = new DefaultBootstrapService(
                messagingService5001.start().join(),
                managedBroadcastService5001.start().join());
        ClusterMembershipService clusterService1 = new DefaultClusterMembershipService(
                localMember1,
                Version.from("1.0.0"),
                new DefaultNodeDiscoveryService(bootstrapService1, localMember1, new BootstrapDiscoveryProvider(bootstrapLocations)),
                bootstrapService1,
                new MembershipConfig());
        CompletableFuture.allOf(new CompletableFuture[]{clusterService1.start()}).join();

        while (true) {
            TimeUnit.SECONDS.sleep(10);
            System.out.println(clusterService1.getMembers());
        }
    }
}
