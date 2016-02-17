/*
 * Copyright 2016 JBoss Inc
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.policies.config.TimeRestrictedAccess;
import io.apiman.gateway.engine.policies.config.TimeRestrictedAccessConfig;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * TimeRestrictedAccess rule tests
 */
@SuppressWarnings({ "nls" })
public class TimeRestrictedAccessPolicyTest extends PolicyTestBase {

    private String path = "/admin";
    private ObjectMapper mapper;

    @Before
    public void init() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void testParseConfiguration() throws Exception {
        TimeRestrictedAccessPolicy policy = new TimeRestrictedAccessPolicy();
        String config = "{}";
        Object parsed = policy.parseConfiguration(config);
        assertNotNull(parsed);
        assertEquals(TimeRestrictedAccessConfig.class, parsed.getClass());

        TimeRestrictedAccessConfig parsedConfig = (TimeRestrictedAccessConfig) parsed;
        assertNotNull(parsedConfig.getRules());
        assertTrue(parsedConfig.getRules().isEmpty());

        TimeRestrictedAccessConfig configObj = new TimeRestrictedAccessConfig();
        ArrayList<TimeRestrictedAccess> elements = new ArrayList<>(2);
        TimeRestrictedAccess rule = new TimeRestrictedAccess();
        rule.setDayEnd(1);
        rule.setDayStart(4);
        rule.setPathPattern(path);
        rule.setTimeEnd(new Date());
        rule.setTimeStart(new Date());
        elements.add(rule);
        configObj.setRules(elements);

        List<TimeRestrictedAccess> expectedConfiguration;
        config = mapper.writeValueAsString(configObj);
        parsedConfig = policy.parseConfiguration(config);
        assertNotNull(parsedConfig.getRules());
        expectedConfiguration = configObj.getRules();
        assertEquals(expectedConfiguration, parsedConfig.getRules());
    }

    @Test
    public void testApply() throws Exception {
        TimeRestrictedAccessPolicy policy = new TimeRestrictedAccessPolicy();

        ApiRequest request = new ApiRequest();
        request.setApiKey("12345");
        request.setRemoteAddr("1.2.3.4");
        request.setDestination(path);
        IPolicyContext context = Mockito.mock(IPolicyContext.class);
        IPolicyChain<ApiRequest> chain = Mockito.mock(IPolicyChain.class);
        final PolicyFailure failure = createFailurePolicyObject(context);

        TimeRestrictedAccess rule = new TimeRestrictedAccess();
        ArrayList<TimeRestrictedAccess> elements = new ArrayList<>(2);
        TimeRestrictedAccessConfig configObj = new TimeRestrictedAccessConfig();
        elements.add(rule);
        configObj.setRules(elements);

        int dayOfWeek = new DateTime().getDayOfWeek();
        rule.setDayEnd(7);
        rule.setDayStart(dayOfWeek);
        rule.setTimeEnd(new DateTime().plusHours(1).toDate());
        rule.setTimeStart(new Date());
        rule.setPathPattern("PathNotListed");
        configObj.setRules(elements);
        Object config = updateConfig(policy, configObj);

        // Successful requests
        policy.apply(request, context, config, chain);
        rule.setPathPattern(path);
        config = updateConfig(policy, configObj);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain, Mockito.times(2)).doApply(request);
        Mockito.verify(chain, Mockito.never()).doFailure(Mockito.<PolicyFailure> any());

        chain = Mockito.mock(IPolicyChain.class);

        // Failed requests
        rule.setDayEnd(dayOfWeek + 1);
        rule.setDayStart(dayOfWeek - 1);
        rule.setPathPattern(path);
        rule.setTimeEnd(new DateTime().plusHours(1).toDate());
        rule.setTimeStart(new Date());
        request.setDestination(path);
        config = updateConfig(policy, configObj);

        policy.apply(request, context, config, chain);

        rule.setDayEnd(1);
        rule.setDayStart(7);
        rule.setTimeEnd(new DateTime().plusHours(2).toDate());
        rule.setTimeStart(new DateTime().plusHours(1).toDate());
        config = updateConfig(policy, configObj);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain, Mockito.times(2)).doFailure(failure);
    }

    private Object updateConfig(TimeRestrictedAccessPolicy policy, TimeRestrictedAccessConfig configObj)
            throws JsonProcessingException {
        String json = mapper.writeValueAsString(configObj);
        Object config = policy.parseConfiguration(json);
        return config;
    }

}
