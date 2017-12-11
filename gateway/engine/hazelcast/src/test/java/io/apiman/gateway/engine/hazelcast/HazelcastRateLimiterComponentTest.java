package io.apiman.gateway.engine.hazelcast;

import com.hazelcast.config.Config;
import io.apiman.gateway.engine.hazelcast.config.HazelcastInstanceManager;
import io.apiman.gateway.engine.rates.RateBucketPeriod;
import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link HazelcastRateLimiterComponent}.
 *
 * @author Pete Cornish
 */
public class HazelcastRateLimiterComponentTest {
    private HazelcastRateLimiterComponent component;

    @Before
    public void setUp() throws Exception {
        final Config config = HazelcastConfigUtil.buildConfigWithDisabledNetwork();
        HazelcastInstanceManager.DEFAULT_MANAGER.setOverrideConfig(config);

        component = new HazelcastRateLimiterComponent(emptyMap());
    }

    @Test
    public void updateBucket_LimitNotReached() throws Exception {
        component.accept("bucketId", RateBucketPeriod.Hour, 10, 1, result -> {
            assertFalse("The bucket should be updated successfully", result.isError());
            assertEquals("The remaining count should be correct", 9, result.getResult().getRemaining());
            assertTrue(result.getResult().isAccepted());
        });
    }

    @Test
    public void updateBucket_LimitReached() throws Exception {
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
