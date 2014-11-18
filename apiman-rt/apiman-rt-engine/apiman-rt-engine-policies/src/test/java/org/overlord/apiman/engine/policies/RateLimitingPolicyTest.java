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
package org.overlord.apiman.engine.policies;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.overlord.apiman.engine.policies.config.RateLimitingConfig;
import org.overlord.apiman.engine.policies.config.rates.RateLimitingGranularity;
import org.overlord.apiman.engine.policies.config.rates.RateLimitingPeriod;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.PolicyFailure;
import org.overlord.apiman.rt.engine.beans.PolicyFailureType;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceContract;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.components.IPolicyFailureFactoryComponent;
import org.overlord.apiman.rt.engine.components.IRateLimiterComponent;
import org.overlord.apiman.rt.engine.impl.InMemoryRateLimiterComponent;
import org.overlord.apiman.rt.engine.policy.IPolicyChain;
import org.overlord.apiman.rt.engine.policy.IPolicyContext;

/**
 * Unit test.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings({ "nls", "unchecked" })
public class RateLimitingPolicyTest {

    /**
     * Test method for {@link org.overlord.apiman.engine.policies.RateLimitingPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testParseConfiguration() {
        RateLimitingPolicy policy = new RateLimitingPolicy();
        
        // Empty config test
        String config = "{}";
        Object parsed = policy.parseConfiguration(config);
        Assert.assertNotNull(parsed);
        Assert.assertEquals(RateLimitingConfig.class, parsed.getClass());
        RateLimitingConfig parsedConfig = (RateLimitingConfig) parsed;
        Assert.assertNull(parsedConfig.getUserHeader());
        Assert.assertNull(parsedConfig.getGranularity());
        Assert.assertNull(parsedConfig.getPeriod());
        
        // Sample real config
        config = "{\r\n" + 
                "  \"limit\" : 100,\r\n" + 
                "  \"granularity\" : \"User\",\r\n" + 
                "  \"period\" : \"Day\",\r\n" + 
                "  \"userHeader\" : \"X-Authenticated-Identity\"\r\n" + 
                "}";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (RateLimitingConfig) parsed;
        Assert.assertNotNull(parsedConfig.getUserHeader());
        Assert.assertNotNull(parsedConfig.getGranularity());
        Assert.assertNotNull(parsedConfig.getLimit());
        Assert.assertNotNull(parsedConfig.getPeriod());

        Assert.assertEquals("X-Authenticated-Identity", parsedConfig.getUserHeader());
        Assert.assertEquals(RateLimitingGranularity.User, parsedConfig.getGranularity());
        Assert.assertEquals(100, parsedConfig.getLimit());
        Assert.assertEquals(RateLimitingPeriod.Day, parsedConfig.getPeriod());
    }

    /**
     * Test method for {@link org.overlord.apiman.engine.policies.RateLimitingPolicy#parseConfiguration(java.lang.String)}.
     */
    @Test
    public void testApply() {
        RateLimitingPolicy policy = new RateLimitingPolicy();
        String json = "{\r\n" + 
                "  \"limit\" : 10,\r\n" + 
                "  \"granularity\" : \"User\",\r\n" + 
                "  \"period\" : \"Minute\",\r\n" + 
                "  \"userHeader\" : \"X-Identity\"\r\n" + 
                "}";
        Object config = policy.parseConfiguration(json);
        policy.setConfiguration(config);
        ServiceRequest request = new ServiceRequest();
        request.setContract(createTestContract());
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/");
        request.getHeaders().put("X-Identity", "sclause"); //$NON-NLS-2$
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(new IPolicyFailureFactoryComponent() {
            @Override
            public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                return failure;
            }
        });
        Mockito.when(context.getComponent(IRateLimiterComponent.class)).thenReturn(new InMemoryRateLimiterComponent());
        IPolicyChain<ServiceRequest> chain = null;
        
        for (int count = 0; count < 10; count++) {
            chain = Mockito.mock(IPolicyChain.class);
            policy.apply(request, context, chain);
            Mockito.verify(chain).doApply(request);
        }
        
        // Failure - only allow 10 per minute!
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, chain);
        Mockito.verify(chain).doFailure(failure);
    }

    /**
     * @return a test service contract
     */
    private ServiceContract createTestContract() {
        Service service = new Service();
        service.setOrganizationId("ServiceOrg");
        service.setServiceId("Service");
        service.setVersion("1.0");
        Application app = new Application();
        app.setOrganizationId("AppOrg");
        app.setApplicationId("App");
        app.setVersion("1.0");
        return new ServiceContract("12345", service, app, null);
    }

}
