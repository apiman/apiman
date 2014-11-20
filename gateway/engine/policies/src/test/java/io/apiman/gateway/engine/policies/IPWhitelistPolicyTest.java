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
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.IPWhitelistPolicy;
import io.apiman.gateway.engine.policies.config.IPWhitelistConfig;
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
@SuppressWarnings({ "nls", "unchecked" })
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
        Assert.assertEquals(IPWhitelistConfig.class, parsed.getClass());
        IPWhitelistConfig parsedConfig = (IPWhitelistConfig) parsed;
        Assert.assertNotNull(parsedConfig.getIpList());
        Assert.assertTrue(parsedConfig.getIpList().isEmpty());
        
        // Single IP address
        config = "{" + 
                "  \"ipList\" : [" + 
                "    \"1.2.3.4\"" + 
                "  ]" + 
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (IPWhitelistConfig) parsed;
        Assert.assertNotNull(parsedConfig.getIpList());
        Assert.assertEquals(1, parsedConfig.getIpList().size());
        Assert.assertEquals("1.2.3.4", parsedConfig.getIpList().iterator().next());

        // Multiple IP addresses
        config = "{" + 
                "  \"ipList\" : [" + 
                "    \"1.2.3.4\"," + 
                "    \"3.4.5.6\"," + 
                "    \"10.0.0.11\"" + 
                "  ]" + 
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (IPWhitelistConfig) parsed;
        Assert.assertNotNull(parsedConfig.getIpList());
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
        ServiceRequest request = new ServiceRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        IPolicyChain<ServiceRequest> chain = Mockito.mock(IPolicyChain.class);
        
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

}
