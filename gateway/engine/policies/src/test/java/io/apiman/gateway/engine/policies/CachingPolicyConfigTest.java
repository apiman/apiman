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

import io.apiman.gateway.engine.policies.config.CachingConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls" })
public class CachingPolicyConfigTest {

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.CachingPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testParseConfiguration() {
        CachingPolicy policy = new CachingPolicy();

        // Empty config test
        String config = "{}";
        Object parsed = policy.parseConfiguration(config);
        Assert.assertNotNull(parsed);
        Assert.assertEquals(CachingConfig.class, parsed.getClass());
        CachingConfig parsedConfig = (CachingConfig) parsed;
        Assert.assertEquals(0, parsedConfig.getTtl());

        // Sample real config
        config = "{\n" +
                "  \"ttl\" : 12345\n" +
                "}";

        parsed = policy.parseConfiguration(config);
        parsedConfig = (CachingConfig) parsed;
        Assert.assertEquals(12345L, parsedConfig.getTtl());
    }

}
