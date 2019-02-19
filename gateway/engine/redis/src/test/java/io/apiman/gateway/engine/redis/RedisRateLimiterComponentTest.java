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
package io.apiman.gateway.engine.redis;

import io.apiman.gateway.engine.rates.RateBucketPeriod;
import io.apiman.gateway.engine.redis.common.RedisClientManager;
import io.apiman.gateway.engine.redis.support.TestRedisUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link RedisRateLimiterComponent}.
 *
 * @author Pete Cornish
 */
public class RedisRateLimiterComponentTest {
    @Rule
    public GenericContainer redis = TestRedisUtil.buildRedisContainer();

    private RedisRateLimiterComponent component;

    @Before
    public void setUp() {
        RedisClientManager.DEFAULT_MANAGER.reset();
        RedisClientManager.DEFAULT_MANAGER.setOverrideConfig(TestRedisUtil.buildComponentConfig(redis));
        component = new RedisRateLimiterComponent(emptyMap());
    }

    @Test
    public void updateBucket_LimitNotReached() {
        component.accept("bucketId", RateBucketPeriod.Hour, 10, 1, result -> {
            assertFalse("The bucket should be updated successfully", result.isError());
            assertEquals("The remaining count should be correct", 9, result.getResult().getRemaining());
            assertTrue(result.getResult().isAccepted());
        });
    }

    @Test
    public void updateBucket_LimitReached() {
        component.accept("bucketId", RateBucketPeriod.Hour, 10, 10, result -> {
            assertFalse("The bucket should be updated successfully", result.isError());
        });

        // should now be at limit
        component.accept("bucketId", RateBucketPeriod.Hour, 10, 1, result -> {
            assertFalse("The bucket should be updated successfully", result.isError());
            assertEquals("The remaining count should be correct", -1, result.getResult().getRemaining());
            assertFalse(result.getResult().isAccepted());
        });
    }
}
