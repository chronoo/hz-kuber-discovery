package com.example.hazelfallout;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryConfig;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.properties.PropertyDefinition;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.kubernetes.CustomStrat;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.DiscoveryStrategy;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class CustomHazelcastConfig {
    @Bean
    public HazelcastInstance config() {
        Config config = new Config();
        config.setProperty("hazelcast.discovery.enabled", "true");

        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);

        DiscoveryConfig discoveryConfig = joinConfig.getDiscoveryConfig();
        DiscoveryStrategyFactory factory = new CustomFactory2();
        DiscoveryStrategyConfig discoveryStrategyConfig = new DiscoveryStrategyConfig(factory);
        discoveryStrategyConfig.setProperties(Map.of("service-dns", "localhost1"));
        discoveryConfig.setDiscoveryStrategyConfigs(List.of(discoveryStrategyConfig));
        return Hazelcast.newHazelcastInstance(config);
    }
}

class CustomFactory2 implements DiscoveryStrategyFactory {
    DiscoveryStrategyFactory strat =  new HazelcastKubernetesDiscoveryStrategyFactory();

    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return CustomStrat.class;
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode, ILogger logger, Map<String, Comparable> properties) {
        return strat.newDiscoveryStrategy(discoveryNode, logger, properties);
    }

    @Override
    public Collection<PropertyDefinition> getConfigurationProperties() {
        return strat.getConfigurationProperties();
    }
}

class CustomFactory extends HazelcastKubernetesDiscoveryStrategyFactory {
    public CustomFactory() {
        super();
    }

    @Override
    public Class<? extends DiscoveryStrategy> getDiscoveryStrategyType() {
        return super.getDiscoveryStrategyType();
    }

    @Override
    public DiscoveryStrategy newDiscoveryStrategy(DiscoveryNode discoveryNode, ILogger logger, Map<String, Comparable> properties) {
        DiscoveryStrategy discoveryStrategy = super.newDiscoveryStrategy(discoveryNode, logger, properties);
        return discoveryStrategy;
    }

    @Override
    public Collection<PropertyDefinition> getConfigurationProperties() {
        return super.getConfigurationProperties();
    }

    @Override
    public boolean isAutoDetectionApplicable() {
        return super.isAutoDetectionApplicable();
    }

    @Override
    public DiscoveryStrategyLevel discoveryStrategyLevel() {
        return super.discoveryStrategyLevel();
    }
}
