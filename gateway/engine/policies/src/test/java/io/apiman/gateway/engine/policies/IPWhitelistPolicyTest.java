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

import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.config.IPListConfig;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls" })
public class IPWhitelistPolicyTest {

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.IPWhitelistPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testParseConfiguration() {
        IPWhitelistPolicy policy = new IPWhitelistPolicy();

        // Empty config test
        String config = "{}";
        Object parsed = policy.parseConfiguration(config);
        Assert.assertNotNull(parsed);
        Assert.assertEquals(IPListConfig.class, parsed.getClass());
        IPListConfig parsedConfig = (IPListConfig) parsed;
        Assert.assertNotNull(parsedConfig.getIpList());
        Assert.assertTrue(parsedConfig.getIpList().isEmpty());

        // Single IP address
        config = "{" +
                "  \"httpHeader\" : null," +
                "  \"responseCode\" : 403," +
                "  \"ipList\" : [" +
                "    \"1.2.3.4\"" +
                "  ]" +
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (IPListConfig) parsed;
        Assert.assertNotNull(parsedConfig.getIpList());
        Assert.assertEquals(1, parsedConfig.getIpList().size());
        Assert.assertNull(parsedConfig.getHttpHeader());
        Assert.assertEquals("1.2.3.4", parsedConfig.getIpList().iterator().next());
        Assert.assertEquals(403, parsedConfig.getResponseCode());

        // Multiple IP addresses
        config = "{" +
                "  \"httpHeader\" : \"X-Forwarded-For\"," +
                "  \"ipList\" : [" +
                "    \"1.2.3.4\"," +
                "    \"3.4.5.6\"," +
                "    \"10.0.0.11\"" +
                "  ]" +
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (IPListConfig) parsed;
        Assert.assertNotNull(parsedConfig.getIpList());
        Assert.assertEquals("X-Forwarded-For", parsedConfig.getHttpHeader());
        Assert.assertEquals(3, parsedConfig.getIpList().size());
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.IPWhitelistPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testApply() {
        IPWhitelistPolicy policy = new IPWhitelistPolicy();
        String json = "{" +
                "  \"ipList\" : [" +
                "    \"1.2.3.4\"," +
                "    \"3.4.5.6\"," +
                "    \"10.0.0.11\"" +
                "  ]" +
                "}";
        Object config = policy.parseConfiguration(json);
        ApiRequest request = new ApiRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        IPolicyChain<ApiRequest> chain = Mockito.mock(IPolicyChain.class);

        // Success
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doApply(request);

        // Failure
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(new IPolicyFailureFactoryComponent() {
            @Override
            public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                return failure;
            }
        });
        chain = Mockito.mock(IPolicyChain.class);
        request.setRemoteAddr("9.8.7.6");
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.IPWhitelistPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testApplyWithWildcards() {
        IPWhitelistPolicy policy = new IPWhitelistPolicy();
        String json = "{" +
                "  \"ipList\" : [" +
                "    \"10.0.*.*\"" +
                "  ]" +
                "}";
        Object config = policy.parseConfiguration(json);
        ApiRequest request = new ApiRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setDestination("/");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        IPolicyChain<ApiRequest> chain = Mockito.mock(IPolicyChain.class);

        // Success
        request.setRemoteAddr("10.0.87.33");
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doApply(request);

        request.setRemoteAddr("10.0.12.19");
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doApply(request);

        // Failure
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(new IPolicyFailureFactoryComponent() {
            @Override
            public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                return failure;
            }
        });
        chain = Mockito.mock(IPolicyChain.class);
        request.setRemoteAddr("9.8.7.6");
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);
    }

}
