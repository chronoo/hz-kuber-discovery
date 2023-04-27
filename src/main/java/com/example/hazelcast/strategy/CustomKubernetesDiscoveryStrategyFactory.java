package com.example.hazelcast.strategy;

import com.hazelcast.kubernetes.CustomHazelcastKubernetesDiscoveryStrategy;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;

import java.util.Map;

public class CustomKubernetesDiscoveryStrategyFactory extends HazelcastKubernetesDiscoveryStrategyFactory {
    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return CustomHazelcastKubernetesDiscoveryStrategy.class;
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode, ILogger logger, Map<String, Comparable> properties) {
        return new CustomHazelcastKubernetesDiscoveryStrategy(logger, properties);
    }
}
