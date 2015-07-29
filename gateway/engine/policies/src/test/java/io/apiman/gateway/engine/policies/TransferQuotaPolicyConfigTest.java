/*
 * Copyright 2014 JBoss Inc
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
package io.apiman.gateway.engine.policies;

import io.apiman.gateway.engine.policies.config.TransferDirectionType;
import io.apiman.gateway.engine.policies.config.TransferQuotaConfig;
import io.apiman.gateway.engine.policies.config.rates.RateLimitingGranularity;
import io.apiman.gateway.engine.policies.config.rates.RateLimitingPeriod;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls" })
public class TransferQuotaPolicyConfigTest {

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.TransferQuotaPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testParseConfiguration() {
        TransferQuotaPolicy policy = new TransferQuotaPolicy();

        // Empty config test
        String config = "{}";
        Object parsed = policy.parseConfiguration(config);
        Assert.assertNotNull(parsed);
        Assert.assertEquals(TransferQuotaConfig.class, parsed.getClass());
        TransferQuotaConfig parsedConfig = (TransferQuotaConfig) parsed;
        Assert.assertNull(parsedConfig.getUserHeader());
        Assert.assertNull(parsedConfig.getGranularity());
        Assert.assertNull(parsedConfig.getPeriod());
        Assert.assertNull(parsedConfig.getHeaderLimit());
        Assert.assertNull(parsedConfig.getHeaderRemaining());
        Assert.assertNull(parsedConfig.getHeaderReset());

        // Sample real config
        config = "{\r\n" +
                "  \"limit\" : 123456789123456789,\r\n" +
                "  \"direction\" : \"both\",\r\n" +
                "  \"granularity\" : \"User\",\r\n" +
                "  \"period\" : \"Day\",\r\n" +
                "  \"headerRemaining\" : \"X-Foo-Remaining\",\r\n" +
                "  \"headerLimit\" : \"X-Foo-Limit\",\r\n" +
                "  \"headerReset\" : \"X-Foo-Reset\",\r\n" +
                "  \"userHeader\" : \"X-Authenticated-Identity\"\r\n" +
                "}";

        parsed = policy.parseConfiguration(config);
        parsedConfig = (TransferQuotaConfig) parsed;
        Assert.assertNotNull(parsedConfig.getUserHeader());
        Assert.assertNotNull(parsedConfig.getGranularity());
        Assert.assertNotNull(parsedConfig.getLimit());
        Assert.assertNotNull(parsedConfig.getPeriod());

        Assert.assertEquals("X-Authenticated-Identity", parsedConfig.getUserHeader());
        Assert.assertEquals(TransferDirectionType.both, parsedConfig.getDirection());
        Assert.assertEquals(RateLimitingGranularity.User, parsedConfig.getGranularity());
        Assert.assertEquals(123456789123456789L, parsedConfig.getLimit());
        Assert.assertEquals(RateLimitingPeriod.Day, parsedConfig.getPeriod());

        Assert.assertEquals("X-Foo-Limit", parsedConfig.getHeaderLimit());
        Assert.assertEquals("X-Foo-Remaining", parsedConfig.getHeaderRemaining());
        Assert.assertEquals("X-Foo-Reset", parsedConfig.getHeaderReset());
    }

}
