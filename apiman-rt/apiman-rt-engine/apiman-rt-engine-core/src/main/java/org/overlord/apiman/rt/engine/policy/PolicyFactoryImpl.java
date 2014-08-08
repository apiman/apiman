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
package org.overlord.apiman.rt.engine.policy;

import java.util.HashMap;
import java.util.Map;

import org.overlord.apiman.rt.engine.beans.exceptions.PolicyNotFoundException;

/**
 * An implementation of the {@link IPolicyFactory} interface.
 *
 * @author eric.wittmann@redhat.com
 */
public class PolicyFactoryImpl implements IPolicyFactory {
    
    private Map<String, IPolicy> policyCache = new HashMap<String, IPolicy>();

    /**
     * Constructor.
     */
    public PolicyFactoryImpl() {
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicyFactory#getPolicy(java.lang.String)
     */
    @Override
    public IPolicy getPolicy(String policyImpl) throws PolicyNotFoundException {
        if (policyImpl == null) {
            throw new PolicyNotFoundException(policyImpl);
        }
        
        // Not synchronized - don't care if we create 2 or 3 of these, it's not worth
        // the synchronization overhead to protect against that.
        if (policyCache.containsKey(policyImpl)) {
            return policyCache.get(policyImpl);
        }
        
        // Handle the various policyImpl formats.  Valid formats include:
        //   class:fullyQualifiedClassname - the class is expected to be on the classpath
        if (policyImpl.startsWith("class:")) { //$NON-NLS-1$
            String classname = policyImpl.substring(6);
            Class<?> c = null;
            try {
                c = Class.forName(classname);
            } catch (ClassNotFoundException e) {
            }
            try {
                c = getClass().getClassLoader().loadClass(classname);
            } catch (ClassNotFoundException e) {
            }
            try {
                c = Thread.currentThread().getContextClassLoader().loadClass(classname);
            } catch (ClassNotFoundException e) {
            }
            
            if (c == null) {
                throw new PolicyNotFoundException(classname);
            }
            
            try {
                IPolicy policy = (IPolicy) c.newInstance();
                policyCache.put(policyImpl, policy);
                return policy;
            } catch (Exception e) {
                throw new RuntimeException("Error loading policy class: " + classname, e); //$NON-NLS-1$
            }
        } else {
            throw new PolicyNotFoundException(policyImpl);
        }
    }

}
