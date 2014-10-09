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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.overlord.apiman.rt.engine.policy.AbstractPolicy;
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
        policyFactory.loadPolicyClass(PassthroughPolicy.QUALIFIED_NAME, "{a:b}");
        Assert.assertEquals("Should classload one Policy via class:CanonicalName", policyFactory.size(), 1);
        
        AbstractPolicy policyInstance = policyFactory.newPolicy(PassthroughPolicy.QUALIFIED_NAME);
        Assert.assertNotNull("Should return a new IPolicy instance", policyInstance);
        Assert.assertEquals("Should return correct parsed configuration", "{a:b}", (String)policyInstance.getConfig());
    }
}
