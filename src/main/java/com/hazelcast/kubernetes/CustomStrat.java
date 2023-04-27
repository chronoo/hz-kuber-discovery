package com.hazelcast.kubernetes;

import com.hazelcast.cluster.Member;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.partitiongroup.PartitionGroupStrategy;

import java.util.Collection;
import java.util.Map;

public class CustomStrat extends AbstractDiscoveryStrategy {
    private final HazelcastKubernetesDiscoveryStrategy strat;

    @Override
    public void destroy() {
        strat.destroy();
    }

    @Override
    public void start() {
        strat.start();
    }

    @Override
    public PartitionGroupStrategy getPartitionGroupStrategy() {
        return strat.getPartitionGroupStrategy();
    }

    @Override
    public PartitionGroupStrategy getPartitionGroupStrategy(Collection<? extends Member> allMembers) {
        return strat.getPartitionGroupStrategy(allMembers);
    }

    @Override
    public Map<String, String> discoverLocalMetadata() {
        return strat.discoverLocalMetadata();
    }

    public CustomStrat(ILogger logger, Map<String, Comparable> properties) {
        super(logger, properties);
        strat = new HazelcastKubernetesDiscoveryStrategy(logger, properties);
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        return strat.discoverNodes();
    }
}
