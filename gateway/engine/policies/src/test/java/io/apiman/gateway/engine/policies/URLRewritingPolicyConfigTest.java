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

import io.apiman.gateway.engine.policies.config.URLRewritingConfig;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls" })
public class URLRewritingPolicyConfigTest {

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.URLRewritingPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testParseConfiguration() {
        URLRewritingPolicy policy = new URLRewritingPolicy();

        // Empty config test
        String config = "{}";
        Object parsed = policy.parseConfiguration(config);
        Assert.assertNotNull(parsed);
        Assert.assertEquals(URLRewritingConfig.class, parsed.getClass());
        URLRewritingConfig parsedConfig = (URLRewritingConfig) parsed;
        Assert.assertNull(parsedConfig.getFromRegex());
        Assert.assertNull(parsedConfig.getToReplacement());
        Assert.assertFalse(parsedConfig.isProcessBody());
        Assert.assertFalse(parsedConfig.isProcessHeaders());

        // Sample real config
        config = "{\n" +
                "  \"fromRegex\" : \"http://localhost:8080/path/to/api\",\n" +
                "  \"toReplacement\" : \"http://example.org:8888/my-api/api-path\",\n" +
                "  \"processBody\" : true,\n" +
                "  \"processHeaders\" : true\n" +
                "}";

        parsed = policy.parseConfiguration(config);
        parsedConfig = (URLRewritingConfig) parsed;
        Assert.assertEquals("http://localhost:8080/path/to/api", parsedConfig.getFromRegex());
        Assert.assertEquals("http://example.org:8888/my-api/api-path", parsedConfig.getToReplacement());
        Assert.assertTrue(parsedConfig.isProcessBody());
        Assert.assertTrue(parsedConfig.isProcessHeaders());
    }

}
