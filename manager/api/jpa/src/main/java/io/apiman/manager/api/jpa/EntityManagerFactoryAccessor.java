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
package io.apiman.manager.api.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Produces an instance of {@link EntityManagerFactory}.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class EntityManagerFactoryAccessor implements IEntityManagerFactoryAccessor {

    @Inject
    private IJpaProperties jpaProperties;

    private EntityManagerFactory emf;

    /**
     * Constructor.
     */
    public EntityManagerFactoryAccessor() {
    }

    @PostConstruct
    public void postConstruct() {
        Map<String, String> properties = new HashMap<>();

        // Get properties from apiman.properties
        Map<String, String> cp = jpaProperties.getAllHibernateProperties();
        if (cp != null) {
            properties.putAll(cp);
        }

        // Get two specific properties from the System (for backward compatibility only)
        String autoValue = System.getProperty("apiman.hibernate.hbm2ddl.auto", "validate"); //$NON-NLS-1$ //$NON-NLS-2$
        String dialect = System.getProperty("apiman.hibernate.dialect", "org.hibernate.dialect.H2Dialect"); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("hibernate.hbm2ddl.auto", autoValue); //$NON-NLS-1$
        properties.put("hibernate.dialect", dialect); //$NON-NLS-1$

        emf = Persistence.createEntityManagerFactory("apiman-manager-api-jpa", properties); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.jpa.IEntityManagerFactoryAccessor#getEntityManagerFactory()
     */
    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

}
