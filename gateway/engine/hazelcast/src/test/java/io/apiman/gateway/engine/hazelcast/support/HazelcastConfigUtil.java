/*
 * Copyright 2018 Pete Cornish
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.gateway.engine.hazelcast.support;

import com.hazelcast.config.*;

/**
 * @author Pete Cornish
 */
public final class HazelcastConfigUtil {
    private HazelcastConfigUtil() {
    }

    /**
     * @return a Hazelcast configuration with network options disabled.
     */
    public static Config buildConfigWithDisabledNetwork() {
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
