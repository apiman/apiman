/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.manager.api.war;

import io.apiman.manager.api.core.config.ApiManagerConfig;
import io.apiman.manager.api.jpa.IJpaProperties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

/**
 * Configuration object for the API Manager.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class WarApiManagerConfig extends ApiManagerConfig implements IJpaProperties {

    /**
     * Constructor.
     */
    public WarApiManagerConfig() {
    }

    /**
     * @see io.apiman.manager.api.jpa.IJpaProperties#getAllHibernateProperties()
     */
    @Override
    public Map<String, String> getAllHibernateProperties() {
        Map<String, String> rval = new HashMap<>();
        @SuppressWarnings("unchecked")
        Iterator<String> keys = getConfig().getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.startsWith("apiman.hibernate.")) { //$NON-NLS-1$
                String value = getConfig().getString(key);
                key = key.substring("apiman.".length()); //$NON-NLS-1$
                rval.put(key, value);
            }
        }
        return rval;
    }

}
