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
public class IPBlacklistPolicyTest {

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.IPBlacklistPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testParseConfiguration() {
        IPBlacklistPolicy policy = new IPBlacklistPolicy();
        
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
     * Test method for {@link io.apiman.gateway.engine.policies.IPBlacklistPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testApply() {
        IPBlacklistPolicy policy = new IPBlacklistPolicy();
        String json = "{" + 
                "  \"ipList\" : [" + 
                "    \"1.2.3.4\"," + 
                "    \"3.4.5.6\"," + 
                "    \"10.0.0.11\"" + 
                "  ]" + 
                "}";
        IPListConfig config = policy.parseConfiguration(json);
        ApiRequest request = new ApiRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(new IPolicyFailureFactoryComponent() {
            @Override
            public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                return failure;
            }
        });
        IPolicyChain<ApiRequest> chain = Mockito.mock(IPolicyChain.class);
        
        // Failure
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);
        
        // Success
        request.setRemoteAddr("9.8.7.6");
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doApply(request);
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.IPBlacklistPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testApplyWithWildcards() {
        IPBlacklistPolicy policy = new IPBlacklistPolicy();
        String json = "{" + 
                "  \"ipList\" : [" + 
                "    \"10.0.*.*\"" + 
                "  ]" + 
                "}";
        IPListConfig config = policy.parseConfiguration(json);
        ApiRequest request = new ApiRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setDestination("/");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(new IPolicyFailureFactoryComponent() {
            @Override
            public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                return failure;
            }
        });
        IPolicyChain<ApiRequest> chain = Mockito.mock(IPolicyChain.class);
        
        // Failures
        request.setRemoteAddr("10.0.123.87");
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);

        request.setRemoteAddr("10.0.97.1");
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);

        // Successes
        request.setRemoteAddr("9.8.7.6");
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doApply(request);

        request.setRemoteAddr("10.10.15.123");
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doApply(request);

    }

}
