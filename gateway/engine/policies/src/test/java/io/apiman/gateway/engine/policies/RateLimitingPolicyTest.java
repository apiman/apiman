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

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.components.IRateLimiterComponent;
import io.apiman.gateway.engine.impl.InMemoryRateLimiterComponent;
import io.apiman.gateway.engine.policies.config.RateLimitingConfig;
import io.apiman.gateway.engine.policies.config.rates.RateLimitingGranularity;
import io.apiman.gateway.engine.policies.config.rates.RateLimitingPeriod;
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
public class RateLimitingPolicyTest {

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.RateLimitingPolicy#parseConfiguration(java.lang.String)}.
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
                "  \"headerRemaining\" : \"X-Rate-Remaining\",\r\n" +
                "  \"headerLimit\" : \"X-Rate-Limit\",\r\n" +
                "  \"headerReset\" : \"X-Rate-Reset\",\r\n" +
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

        Assert.assertEquals("X-Rate-Limit", parsedConfig.getHeaderLimit());
        Assert.assertEquals("X-Rate-Remaining", parsedConfig.getHeaderRemaining());
        Assert.assertEquals("X-Rate-Reset", parsedConfig.getHeaderReset());
    }

    /**
     * Test method for {@link io.apiman.gateway.engine.policies.RateLimitingPolicy#parseConfiguration(java.lang.String)}.
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
        ApiRequest request = new ApiRequest();
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
        IPolicyChain<ApiRequest> chain = null;

        for (int count = 0; count < 10; count++) {
            chain = Mockito.mock(IPolicyChain.class);
            policy.apply(request, context, config, chain);
            Mockito.verify(chain).doApply(request);
        }

        // Failure - only allow 10 per minute!
        chain = Mockito.mock(IPolicyChain.class);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain).doFailure(failure);
    }

    /**
     * @return a test contract
     */
    private ApiContract createTestContract() {
        Api api = new Api();
        api.setOrganizationId("ApiOrg");
        api.setApiId("Api");
        api.setVersion("1.0");
        Client app = new Client();
        app.setOrganizationId("AppOrg");
        app.setClientId("App");
        app.setVersion("1.0");
        return new ApiContract("12345", api, app, "Gold", null);
    }

}
