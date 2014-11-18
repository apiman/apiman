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
package org.overlord.apiman.rt.engine.impl;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.overlord.apiman.rt.engine.policy.IPolicy;
import org.overlord.apiman.rt.engine.policy.IPolicyFactory;
import org.overlord.apiman.rt.engine.policy.PolicyFactoryImpl;

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
        policyFactory = new PolicyFactoryImpl();
    }

    @Test
    public void testPolicyLoad() {
        policyFactory.newPolicy(PassthroughPolicy.QUALIFIED_NAME);
        Assert.assertEquals("Should classload one Policy via class:CanonicalName", getNumPolicies(), 1);
        
        IPolicy policyInstance = policyFactory.newPolicy(PassthroughPolicy.QUALIFIED_NAME);
        Assert.assertNotNull("Should return a new IPolicy instance", policyInstance);
    }

    /**
     * @return the number of policies in the factory
     */
    @SuppressWarnings("rawtypes")
    private int getNumPolicies() {
        try {
            Field field = policyFactory.getClass().getDeclaredField("canonicalCache");
            field.setAccessible(true);
            return ((Map) field.get(policyFactory)).size();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
