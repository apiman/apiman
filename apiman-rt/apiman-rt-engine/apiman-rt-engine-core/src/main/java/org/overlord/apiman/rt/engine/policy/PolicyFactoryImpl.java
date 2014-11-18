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
    private Map<String, Class<IPolicy>> canonicalCache = new HashMap<String, Class<IPolicy>>();

    /**
     * Constructor.
     */
    public PolicyFactoryImpl() {
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicyFactory#newPolicy(java.lang.String)
     */
    @Override
    public IPolicy newPolicy(String qualifiedName) throws PolicyNotFoundException {
        Class<IPolicy> policyClass = loadPolicyClass(qualifiedName);
        IPolicy newInstance = createPolicy(policyClass);
        return newInstance;
    }

    /**
     * Loads a policy class.  Caches the loaded class for efficiency.
     * @param policyImpl
     * @throws PolicyNotFoundException
     */
    public Class<IPolicy> loadPolicyClass(String policyImpl) throws PolicyNotFoundException {
        if (policyImpl == null) {
            throw new PolicyNotFoundException(policyImpl);
        }

        // Not synchronized - don't care if we create 2 or 3 of these, it's not worth
        // the synchronization overhead to protect against that.
        if (canonicalCache.containsKey(policyImpl)) {
            return canonicalCache.get(policyImpl);
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

            @SuppressWarnings("unchecked")
            Class<IPolicy> policyClass = (Class<IPolicy>) c;
            canonicalCache.put(policyImpl, policyClass);
            return policyClass;
        } else {
            throw new PolicyNotFoundException(policyImpl);
        }
    }

    /**
     * Creates a policy from the given class.  The class must have a no-arg constructor.
     * @param clazz
     */
    private IPolicy createPolicy(Class<IPolicy> clazz) {
        try {
            IPolicy policy = (IPolicy) clazz.newInstance();
            return policy;
        } catch (Exception e) {
            throw new RuntimeException("Error instantiating policy class: " + clazz, e); //$NON-NLS-1$
        }
    }

}
