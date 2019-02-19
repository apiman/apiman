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
package io.apiman.gateway.engine.redis.support;

import org.redisson.config.Config;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public final class TestRedisUtil {
    private TestRedisUtil() {
    }

    public static GenericContainer buildRedisContainer() {
        return new GenericContainer("redis:3.0.6")
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort());
    }

    public static Config buildComponentConfig(GenericContainer redis) {
        final String address = "redis://" + redis.getContainerIpAddress() + ":" + redis.getMappedPort(6379);
        final Config config = new Config();
        config.useSingleServer().setAddress(address);
        return config;
    }
}
