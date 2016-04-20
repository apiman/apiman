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
package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.exceptions.PolicyNotFoundException;
import io.apiman.gateway.engine.policy.IPolicy;
import io.apiman.gateway.engine.policy.IPolicyFactory;
import io.apiman.gateway.engine.policy.PolicyFactoryImpl;
import io.apiman.gateway.engine.util.PassthroughPolicy;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link IPolicyFactory} functionality, in particular ensuring classes are
 * discovered, loaded, instantiated and configured as expected.
 * 
 * @author Marc Savy <msavy@redhat.com>
 */
@SuppressWarnings("nls")
public class PolicyLoadTest {
    
    private IPolicyFactory policyFactory;

    @Before
    public void setup() {
        policyFactory = new PolicyFactoryImpl(Collections.emptyMap());
    }

    @Test
    public void testPolicyLoad() {
        policyFactory.loadPolicy(PassthroughPolicy.QUALIFIED_NAME, new IAsyncResultHandler<IPolicy>() {
            @Override
            public void handle(IAsyncResult<IPolicy> result) {
                Assert.assertEquals("Should classload one Policy via class:CanonicalName", getNumPolicies(), 1);
            }
        });
        
        policyFactory.loadPolicy(PassthroughPolicy.QUALIFIED_NAME, new IAsyncResultHandler<IPolicy>() {
            @Override
            public void handle(IAsyncResult<IPolicy> result) {
                Assert.assertNotNull("Should return a valid async result", result);
                Assert.assertTrue(result.isSuccess());
                Assert.assertNotNull("Should return a new IPolicy instance", result.getResult());
            }
        });
        
        policyFactory.loadPolicy("class:org.example.NotFound", new IAsyncResultHandler<IPolicy>() {
            @Override
            public void handle(IAsyncResult<IPolicy> result) {
                Assert.assertNotNull("Should return a valid async result", result);
                Assert.assertTrue(result.isError());
                Assert.assertNotNull("Should return an exception", result.getError());
                Assert.assertEquals(PolicyNotFoundException.class.getName(), result.getError().getClass().getName());
            }
        });
    }

    /**
     * @return the number of policies in the factory
     */
    @SuppressWarnings("rawtypes")
    private int getNumPolicies() {
        try {
            Field field = policyFactory.getClass().getDeclaredField("policyCache");
            field.setAccessible(true);
            return ((Map) field.get(policyFactory)).size();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
