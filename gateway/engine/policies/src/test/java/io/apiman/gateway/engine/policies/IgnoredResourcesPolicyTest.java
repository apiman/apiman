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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IPolicyFailureFactoryComponent;
import io.apiman.gateway.engine.policies.config.IgnoredResource;
import io.apiman.gateway.engine.policies.config.IgnoredResourcesConfig;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test
 *
 * @author rubenrm1@gmail.com
 *
 */
@SuppressWarnings({ "unchecked", "nls" })
public class IgnoredResourcesPolicyTest {

    private static ObjectMapper mapper;
    private static String firstPath = "/invoices/.+/items/.+";
    private static String secondPath = "/items/.+";

    @Before
    public void init() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void testParseConfiguration() throws Exception {
        IgnoredResourcesPolicy policy = new IgnoredResourcesPolicy();
        String config = "{}";
        Object parsed = policy.parseConfiguration(config);
        assertNotNull(parsed);
        assertEquals(IgnoredResourcesConfig.class, parsed.getClass());

        IgnoredResourcesConfig parsedConfig = (IgnoredResourcesConfig) parsed;
        assertNotNull(parsedConfig.getRules());
        assertTrue(parsedConfig.getRules().isEmpty());

        IgnoredResourcesConfig configObj = new IgnoredResourcesConfig();
        ArrayList<IgnoredResource> elements = new ArrayList<>(2);
        elements.add(createResource(IgnoredResource.VERB_MATCH_ALL, firstPath));
        configObj.setRules(elements);

        // Single Path
        assertConfigurationParseResults(policy, configObj);

        elements.add(createResource(IgnoredResource.VERB_MATCH_ALL, secondPath));
        configObj.setRules(elements);
        assertConfigurationParseResults(policy, configObj);
    }

    @Test
    public void testApply() throws Exception {
        IgnoredResourcesPolicy policy = new IgnoredResourcesPolicy();
        IgnoredResourcesConfig configObj = new IgnoredResourcesConfig();
        ArrayList<IgnoredResource> elements = new ArrayList<>(2);
        elements.add(createResource(IgnoredResource.VERB_MATCH_ALL, firstPath));
        elements.add(createResource(IgnoredResource.VERB_MATCH_ALL, secondPath));
        configObj.setRules(elements);
        String json = mapper.writeValueAsString(configObj);
        Object config = policy.parseConfiguration(json);

        ApiRequest request = new ApiRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/invoices/1");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        IPolicyChain<ApiRequest> chain = Mockito.mock(IPolicyChain.class);

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
        Mockito.verify(chain, Mockito.never()).doFailure(Mockito.<PolicyFailure> any());

        final PolicyFailure failure = createFailurePolicyObject(context);
        chain = Mockito.mock(IPolicyChain.class);
        request.setDestination("/invoices/23/items/43");
        policy.apply(request, context, config, chain);

        // Failure
        request.setDestination("/items/43");
        policy.apply(request, context, config, chain);
        Mockito.verify(chain, Mockito.times(2)).doFailure(failure);
    }

    @Test
    public void testApplyWithType() throws Exception {
        IPolicyChain<ApiRequest> chain = Mockito.mock(IPolicyChain.class);
        String verb = "GET";
        ApiRequest request = requestWithVerb(chain, verb);

        Mockito.verify(chain, Mockito.times(1)).doApply(request);
        Mockito.verify(chain, Mockito.times(2)).doFailure(Mockito.<PolicyFailure> any());
    }

    @Test
    public void testApplyWithDifferentType() throws Exception {
        IPolicyChain<ApiRequest> chain = Mockito.mock(IPolicyChain.class);
        String verb = "TRACE";
        ApiRequest request = requestWithVerb(chain, verb);

        Mockito.verify(chain, Mockito.times(3)).doApply(request);
        Mockito.verify(chain, Mockito.never()).doFailure(Mockito.<PolicyFailure> any());
    }

    private ApiRequest requestWithVerb(IPolicyChain<ApiRequest> chain, String verb)
            throws IOException, JsonGenerationException, JsonMappingException
    {
        IgnoredResourcesPolicy policy = new IgnoredResourcesPolicy();
        IgnoredResourcesConfig configObj = new IgnoredResourcesConfig();
        ArrayList<IgnoredResource> elements = new ArrayList<>(2);

        elements.add(createResource(verb, firstPath));
        elements.add(createResource(verb, secondPath));
        configObj.setRules(elements);
        String json = mapper.writeValueAsString(configObj);
        Object config = policy.parseConfiguration(json);

        ApiRequest request = new ApiRequest();
        request.setType("GET");
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination("/invoices/1");
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        createFailurePolicyObject(context);

        // Success
        policy.apply(request, context, config, chain);

        // Fail
        request.setDestination("/invoices/23/items/43");
        policy.apply(request, context, config, chain);

        // Fail
        request.setDestination("/items/43");
        policy.apply(request, context, config, chain);
        return request;
    }

    private void assertConfigurationParseResults(IgnoredResourcesPolicy policy,
            IgnoredResourcesConfig configObj) throws Exception {
        IgnoredResourcesConfig parsedConfig;
        List<IgnoredResource> expectedConfiguration;
        String config = mapper.writeValueAsString(configObj);
        parsedConfig = policy.parseConfiguration(config);
        assertNotNull(parsedConfig.getRules());
        expectedConfiguration = configObj.getRules();
        assertEquals(expectedConfiguration, parsedConfig.getRules());
    }

    private IgnoredResource createResource(String verb, String path) {
        IgnoredResource resource = new IgnoredResource();
        resource.setPathPattern(path);
        resource.setVerb(verb);
        return resource;
    }

    private PolicyFailure createFailurePolicyObject(IPolicyContext context) {
        // Failure
        final PolicyFailure failure = new PolicyFailure();
        Mockito.when(context.getComponent(IPolicyFailureFactoryComponent.class))
                .thenReturn(new IPolicyFailureFactoryComponent() {
                    @Override
                    public PolicyFailure createFailure(PolicyFailureType type, int failureCode,
                            String message) {
                        return failure;
                    }
                });
        return failure;
    }

}
