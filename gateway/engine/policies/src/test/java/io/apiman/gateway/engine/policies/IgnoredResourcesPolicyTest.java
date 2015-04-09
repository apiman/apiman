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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.config.IgnoredResourcesConfig;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test
 * 
 * @author rubenrm1@gmail.com
 *
 */
@SuppressWarnings({ "nls", "unchecked" })
public class IgnoredResourcesPolicyTest {

    @Test
    public void testParseConfiguration() {
        IgnoredResourcesPolicy policy = new IgnoredResourcesPolicy();

        String config = "{}";
        Object parsed = policy.parseConfiguration(config);
        assertNotNull(parsed);
        assertEquals(IgnoredResourcesConfig.class, parsed.getClass());

        IgnoredResourcesConfig parsedConfig = (IgnoredResourcesConfig) parsed;
        assertNotNull(parsedConfig.getPathsToIgnore());
        assertTrue(parsedConfig.getPathsToIgnore().isEmpty());

        // Single Path
        config = "{" +
                "  \"pathsToIgnore\" : [" +
                "    \"/invoices/.+/items/.+\"" +
                "  ]" +
                " }";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (IgnoredResourcesConfig) parsed;
        assertNotNull(parsedConfig.getPathsToIgnore());
        List<String> expectedConfiguration = Arrays.asList("/invoices/.+/items/.+");
        assertEquals(expectedConfiguration, parsedConfig.getPathsToIgnore());
        
        // Multiple Paths
        config = "{" +
                "  \"pathsToIgnore\" : [" +
                "    \"/invoices/.+/items/.+\"," +
                "    \"/items/.+\"" +
                "  ]" +
                " }";
        parsed = policy.parseConfiguration(config);
        parsedConfig = (IgnoredResourcesConfig) parsed;
        assertNotNull(parsedConfig.getPathsToIgnore());
        expectedConfiguration = new ArrayList<>();
        expectedConfiguration.add("/invoices/.+/items/.+");
        expectedConfiguration.add("/items/.+");
        assertEquals(expectedConfiguration, parsedConfig.getPathsToIgnore());
    }

    @Test
    public void testApply() {
        IgnoredResourcesPolicy policy = new IgnoredResourcesPolicy();

        String json = "{" +
                    "  \"pathsToIgnore\" : [" +
                    "    \"/invoices/.+/items/.+\"," +
                    "    \"/items/.+\"" +
                    "  ]" +
                    " }";
        Object config = policy.parseConfiguration(json);

        ServiceRequest request = new ServiceRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/invoices/1");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        IPolicyChain<ServiceRequest> chain = Mockito.mock(IPolicyChain.class);

        // Success
        policy.apply(request, context, config, chain);
        
        // Success
        request.setDestination("/invoices");
        policy.apply(request, context, config, chain);

        // Success
        request.setDestination("/invoices/items");
        policy.apply(request, context, config, chain);
  
        // Success
        request.setDestination("/invoices/items/13");
        policy.apply(request, context, config, chain);
        Mockito.verify(chain, Mockito.times(4)).doApply(request);
         
        // Failure
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class)).thenReturn(
                new IPolicyFailureFactoryComponent() {
                    @Override
                    public PolicyFailure createFailure(PolicyFailureType type, int failureCode, String message) {
                        return failure;
                    }
                });
        chain = Mockito.mock(IPolicyChain.class);
        request.setDestination("/invoices/23/items/43");
        policy.apply(request, context, config, chain);
        
        // Failure
        request.setDestination("/items/43");
        policy.apply(request, context, config, chain);
        Mockito.verify(chain, Mockito.times(2)).doFailure(failure);
    }
    
}
