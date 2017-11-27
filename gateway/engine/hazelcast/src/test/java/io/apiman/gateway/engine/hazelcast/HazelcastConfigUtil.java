package io.apiman.gateway.engine.hazelcast;

import com.hazelcast.config.*;

/**
 * @author Pete Cornish
 */
final class HazelcastConfigUtil {
    private HazelcastConfigUtil() {
    }

    /**
     * @return a Hazelcast configuration with network options disabled.
     */
    static Config buildConfigWithDisabledNetwork() {
        return new Config() {{
            getNetworkConfig().setJoin(new JoinConfig() {{
                setMulticastConfig(new MulticastConfig() {{
                    setEnabled(false);
                }});
                setTcpIpConfig(new TcpIpConfig() {{
                    setEnabled(false);
                }});
                setAwsConfig(new AwsConfig() {{
                    setEnabled(false);
                }});
            }});
        }};
    }
}
