package com.example.hazelcast.strategy;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CustomHazelcastConfig {
    @Bean
    public HazelcastInstance config() {
        Config config = new Config();
        config.setProperty("hazelcast.discovery.enabled", "true");

        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getDiscoveryConfig()
            .addDiscoveryStrategyConfig(
            new DiscoveryStrategyConfig(
                new CustomKubernetesDiscoveryStrategyFactory(),
                Map.of("service-dns", "localhost1")
            )
        );
        return Hazelcast.newHazelcastInstance(config);
    }
}

