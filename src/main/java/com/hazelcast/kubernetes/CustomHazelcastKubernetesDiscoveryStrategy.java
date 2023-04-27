package com.hazelcast.kubernetes;

import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;

import java.util.Map;

/**
 * Copied from {@link HazelcastKubernetesDiscoveryStrategy}
 */
public class CustomHazelcastKubernetesDiscoveryStrategy extends AbstractDiscoveryStrategy {
    private final HazelcastKubernetesDiscoveryStrategy discoveryStrategy;
    private final HazelcastKubernetesDiscoveryStrategy.EndpointResolver endpointResolver;

    public CustomHazelcastKubernetesDiscoveryStrategy(ILogger logger, Map<String, Comparable> properties) {
        super(logger, properties);
        discoveryStrategy = new HazelcastKubernetesDiscoveryStrategy(logger, properties);
        KubernetesConfig config = new KubernetesConfig(properties);
        logger.info(config.toString());

        endpointResolver = new CustomHazelcastKubernetesDiscoveryStrategyEndpointResolver(logger, config.getServiceDns(), config.getServicePort(),
                config.getServiceDnsTimeout());

        logger.info("Kubernetes Discovery activated with mode: " + config.getMode().name());
    }

    public void start() {
        endpointResolver.start();
    }

    @Override
    public Map<String, String> discoverLocalMetadata() {
        return discoveryStrategy.discoverLocalMetadata();
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        return endpointResolver.resolve();
    }

    public void destroy() {
        endpointResolver.destroy();
    }
}

