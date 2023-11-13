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

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

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
import io.apiman.test.policies.TestingPolicy;

/**
 * TimeRestrictedAccess rule tests
 */
@TestingPolicy(BasicAuthenticationPolicy.class)
@SuppressWarnings({ "nls" })
public class TimeRestrictedAccessPolicyTest extends PolicyTestBase {

    private final String path = "/admin";
    private ObjectMapper mapper;

    @Before
    public void init() {
        mapper = new ObjectMapper();
        // Enable various Java 8 and library data structures to be serialized
        mapper.findAndRegisterModules();
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
        rule.setTimeEnd(OffsetDateTime.now(ZoneOffset.UTC));
        rule.setTimeStart(OffsetDateTime.now(ZoneOffset.UTC));
        elements.add(rule);
        configObj.setRules(elements);

        List<TimeRestrictedAccess> expectedConfiguration;
        config = mapper.writeValueAsString(configObj);
        parsedConfig = policy.parseConfiguration(config);
        assertNotNull(parsedConfig.getRules());
        expectedConfiguration = configObj.getRules();
        List<TimeRestrictedAccess> rules = parsedConfig.getRules();
        TimeRestrictedAccess expected = expectedConfiguration.get(0);
        TimeRestrictedAccess actual = rules.get(0);
        assertEquals(expected.getDayEnd(), actual.getDayEnd());
        assertEquals(expected.getDayStart(), actual.getDayStart());
        assertEquals(expected.getPathPattern(), actual.getPathPattern());
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

        rule.setDayEnd(7);
        rule.setDayStart(1);
        rule.setTimeEnd(OffsetDateTime.now(ZoneOffset.UTC).plusHours(2));
        rule.setTimeStart(OffsetDateTime.now(ZoneOffset.UTC).minusHours(2));
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

        // Fail requests by day
        DayOfWeek dayOfWeek = Instant.now().atZone(ZoneOffset.UTC).getDayOfWeek();
        rule.setDayStart(dayOfWeek.plus(1).getValue());
        rule.setDayEnd(dayOfWeek.plus(2).getValue());
        rule.setPathPattern(path);
        rule.setTimeEnd(OffsetDateTime.now(ZoneOffset.UTC).plusHours(1));
        rule.setTimeStart(OffsetDateTime.now(ZoneOffset.UTC));
        request.setDestination(path);
        config = updateConfig(policy, configObj);

        policy.apply(request, context, config, chain);

        // Fail requests by time
        rule.setDayStart(1);
        rule.setDayEnd(7);
        rule.setTimeEnd(OffsetDateTime.now(ZoneOffset.UTC).plusHours(2));
        rule.setTimeStart(OffsetDateTime.now(ZoneOffset.UTC).plusHours(1));
        config = updateConfig(policy, configObj);
        policy.apply(request, context, config, chain);
        Mockito.verify(chain, Mockito.times(2)).doApply(request);
        Mockito.verify(chain, Mockito.times(2)).doFailure(failure);
    }

    private Object updateConfig(TimeRestrictedAccessPolicy policy, TimeRestrictedAccessConfig configObj)
            throws JsonProcessingException {
        String json = mapper.writeValueAsString(configObj);
        Object config = policy.parseConfiguration(json);
        return config;
    }

}
