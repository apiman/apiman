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
package org.overlord.apiman.rt.engine.osgi.policy;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.overlord.apiman.rt.engine.beans.exceptions.PolicyNotFoundException;
import org.overlord.apiman.rt.engine.policy.IPolicy;
import org.overlord.apiman.rt.engine.policy.IPolicyFactory;

/**
 * A version of the policy factory that works in an OSGi environment.
 * 
 * @author eric.wittmann@redhat.com
 */
public class OSGIPolicyFactory implements IPolicyFactory {

    /**
     * Constructor.
     */
    public OSGIPolicyFactory() {
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicyFactory#newPolicy(java.lang.String)
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public IPolicy newPolicy(String policyImpl) throws PolicyNotFoundException {
        if (policyImpl == null) {
            throw new PolicyNotFoundException(policyImpl);
        }

        // Handle the various policyImpl formats. Valid formats include:
        // class:fullyQualifiedClassname - should be in the OSGi registry mapped to IPolicy
        try {
            if (policyImpl.startsWith("class:")) { //$NON-NLS-1$
                String classname = policyImpl.substring(6);
                Bundle bundle = FrameworkUtil.getBundle(OSGIPolicyFactory.class);
                if (bundle != null) {
                    if (bundle.getState() == Bundle.RESOLVED) {
                        bundle.start();
                    }
                    BundleContext context = bundle.getBundleContext();
                    if (context != null) {
                        ServiceReference[] serviceReferences = context.getServiceReferences(IPolicy.class.getName(), null);
                        if (serviceReferences != null) {
                            for (ServiceReference serviceReference : serviceReferences) {
                                IPolicy service = (IPolicy) context.getService(serviceReference);
                                if (service.getClass().getName().equals(classname)) {
                                    return service;
                                }
                            }
                        }
                    }
                }

                throw new PolicyNotFoundException(classname);
            } else {
                throw new PolicyNotFoundException(policyImpl);
            }
        } catch (Exception e) {
            throw new PolicyNotFoundException(policyImpl, e);
        }
    }

}
