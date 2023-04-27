package com.hazelcast.kubernetes;

import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.AbstractDiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.partitiongroup.PartitionGroupMetaData;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Copied from {@link HazelcastKubernetesDiscoveryStrategy}
 */
public class CustomHazelcastKubernetesDiscoveryStrategy extends AbstractDiscoveryStrategy {
    private final KubernetesClient client;
    private final HazelcastKubernetesDiscoveryStrategy.EndpointResolver endpointResolver;
    private KubernetesConfig config;

    private final Map<String, String> memberMetadata = new HashMap<>();

    public CustomHazelcastKubernetesDiscoveryStrategy(ILogger logger, Map<String, Comparable> properties) {
        super(logger, properties);

        config = new KubernetesConfig(properties);
        logger.info(config.toString());

        client = buildKubernetesClient(config);

        if (KubernetesConfig.DiscoveryMode.DNS_LOOKUP.equals(config.getMode())) {
            endpointResolver = new CustomHazelcastKubernetesDiscoveryStrategyEndpointResolver(logger, config.getServiceDns(), config.getServicePort(),
                config.getServiceDnsTimeout());
        } else {
            endpointResolver = new KubernetesApiEndpointResolver(logger, config.getServiceName(), config.getServicePort(),
                config.getServiceLabelName(), config.getServiceLabelValue(),
                config.getPodLabelName(), config.getPodLabelValue(),
                config.isResolveNotReadyAddresses(), client);
        }

        logger.info("Kubernetes Discovery activated with mode: " + config.getMode().name());
    }

    private static KubernetesClient buildKubernetesClient(KubernetesConfig config) {
        return new KubernetesClient(config.getNamespace(), config.getKubernetesMasterUrl(), config.getKubernetesApiToken(),
            config.getKubernetesCaCertificate(), config.getKubernetesApiRetries(), config.getExposeExternallyMode(),
            config.isUseNodeNameAsExternalAddress(), config.getServicePerPodLabelName(), config.getServicePerPodLabelValue());
    }

    public void start() {
        endpointResolver.start();
    }

    @Override
    public Map<String, String> discoverLocalMetadata() {
        if (memberMetadata.isEmpty()) {
            memberMetadata.put(PartitionGroupMetaData.PARTITION_GROUP_ZONE, discoverZone());
            memberMetadata.put("hazelcast.partition.group.node", discoverNodeName());
        }
        return memberMetadata;
    }

    /**
     * Discovers the availability zone in which the current Hazelcast member is running.
     * <p>
     * Note: ZONE_AWARE is available only for the Kubernetes API Mode.
     */
    private String discoverZone() {
        if (KubernetesConfig.DiscoveryMode.KUBERNETES_API.equals(config.getMode())) {
            try {
                String zone = client.zone(podName());
                if (zone != null) {
                    getLogger().info(String.format("Kubernetes plugin discovered availability zone: %s", zone));
                    return zone;
                }
            } catch (Exception e) {
                // only log the exception and the message, Hazelcast should still start
                getLogger().finest(e);
            }
            getLogger().info("Cannot fetch the current zone, ZONE_AWARE feature is disabled");
        }
        return "unknown";
    }

    /**
     * Discovers the name of the node which the current Hazelcast member pod is running on.
     * <p>
     * Note: NODE_AWARE is available only for the Kubernetes API Mode.
     */
    private String discoverNodeName() {
        if (KubernetesConfig.DiscoveryMode.KUBERNETES_API.equals(config.getMode())) {
            try {
                String nodeName = client.nodeName(podName());
                if (nodeName != null) {
                    getLogger().info(String.format("Kubernetes plugin discovered node name: %s", nodeName));
                    return nodeName;
                }
            } catch (Exception e) {
                // only log the exception and the message, Hazelcast should still start
                getLogger().finest(e);
            }
            getLogger().warning("Cannot fetch name of the node, NODE_AWARE feature is disabled");
        }
        return "unknown";
    }

    private String podName() throws UnknownHostException {
        String podName = System.getenv("POD_NAME");
        if (podName == null) {
            podName = System.getenv("HOSTNAME");
        }
        if (podName == null) {
            podName = InetAddress.getLocalHost().getHostName();
        }
        return podName;
    }

    @Override
    public Iterable<DiscoveryNode> discoverNodes() {
        return endpointResolver.resolve();
    }

    public void destroy() {
        endpointResolver.destroy();
    }
}

