package com.hazelcast.kubernetes;

import com.hazelcast.cluster.Address;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.logging.ILogger;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.hazelcast.spi.discovery.SimpleDiscoveryNode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * Copied from {@link DnsEndpointResolver}
 */
public class CustomHazelcastKubernetesDiscoveryStrategyEndpointResolver extends HazelcastKubernetesDiscoveryStrategy.EndpointResolver {
    // executor service for dns lookup calls
    private static final ExecutorService DNS_LOOKUP_SERVICE = Executors.newCachedThreadPool();

    private final String serviceDns;
    private final int port;
    private final int serviceDnsTimeout;

    CustomHazelcastKubernetesDiscoveryStrategyEndpointResolver(ILogger logger, String serviceDns, int port, int serviceDnsTimeout) {
        super(logger);
        this.serviceDns = serviceDns;
        this.port = port;
        this.serviceDnsTimeout = serviceDnsTimeout;
    }

    List<DiscoveryNode> resolve() {
        try {
            return lookup();
        } catch (TimeoutException e) {
            logger.warning(String.format("DNS lookup for serviceDns '%s' failed: DNS resolution timeout", serviceDns));
            logger.log(Level.SEVERE, "fail");
            return Collections.emptyList();
        } catch (UnknownHostException e) {
            logger.warning(String.format("DNS lookup for serviceDns '%s' failed: unknown host", serviceDns));
            return Collections.emptyList();
        } catch (Exception e) {
            logger.warning(String.format("DNS lookup for serviceDns '%s' failed", serviceDns), e);
            return Collections.emptyList();
        }
    }

    private List<DiscoveryNode> lookup()
        throws UnknownHostException, InterruptedException, ExecutionException, TimeoutException {
        Set<String> addresses = new HashSet<String>();

        Future<InetAddress[]> future = DNS_LOOKUP_SERVICE.submit(new Callable<InetAddress[]>() {
            @Override
            public InetAddress[] call() throws Exception {
                return getAllInetAddresses();
            }
        });

        try {
            for (InetAddress address : future.get(serviceDnsTimeout, TimeUnit.SECONDS)) {
                if (addresses.add(address.getHostAddress()) && logger.isFinestEnabled()) {
                    logger.finest("Found node service with address: " + address);
                }
            }
        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownHostException) {
                throw (UnknownHostException) e.getCause();
            } else {
                throw e;
            }
        } catch (TimeoutException e) {
            // cancel DNS lookup
            future.cancel(true);
            throw e;
        }

        if (addresses.size() == 0) {
            logger.warning("Could not find any service for serviceDns '" + serviceDns + "'");
            return Collections.emptyList();
        }

        List<DiscoveryNode> result = new ArrayList<DiscoveryNode>();
        for (String address : addresses) {
            result.add(new SimpleDiscoveryNode(new Address(address, getHazelcastPort(port))));
        }
        return result;
    }

    /**
     * Do the actual lookup
     *
     * @return array of resolved inet addresses
     * @throws UnknownHostException
     */
    private InetAddress[] getAllInetAddresses() throws UnknownHostException {
        return InetAddress.getAllByName(serviceDns);
    }

    private static int getHazelcastPort(int port) {
        if (port > 0) {
            return port;
        }
        return NetworkConfig.DEFAULT_PORT;
    }
}
