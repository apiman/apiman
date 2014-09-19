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
package org.overlord.apiman.dt.api.fuse6.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.overlord.apiman.dt.api.jpa.IEntityManagerFactoryAccessor;

/**
 * Produces an instance of {@link EntityManagerFactory}.
 * 
 * @author eric.wittmann@redhat.com
 */
public class FuseEntityManagerFactoryAccessor implements IEntityManagerFactoryAccessor {

    private EntityManagerFactory emf;

    /**
     * Constructor.
     */
    public FuseEntityManagerFactoryAccessor() {
    }
    
    /**
     * @see org.overlord.apiman.dt.api.jpa.IEntityManagerFactoryAccessor#getEntityManagerFactory()
     */
    @Override
    public synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = createEntityManagerFactory();
        }
        return emf;
    }

    /**
     * Creates an entity manager factory.
     */
    public static EntityManagerFactory createEntityManagerFactory() {
        String autoValue = System.getProperty("hibernate.hbm2ddl.auto", "update"); //$NON-NLS-1$ //$NON-NLS-2$
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("hibernate.hbm2ddl.auto", autoValue); //$NON-NLS-1$

        Bundle bundle = FrameworkUtil.getBundle(FuseEntityManagerFactoryAccessor.class);
        BundleContext context = bundle.getBundleContext();
        ServiceReference serviceReference = context.getServiceReference(PersistenceProvider.class.getName());
        PersistenceProvider persistenceProvider = (PersistenceProvider) context.getService(serviceReference);

        return persistenceProvider.createEntityManagerFactory("apiman-dt-api-jpa", properties); //$NON-NLS-1$
    }

}
