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
package io.apiman.manager.ui.server.servlets;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.AbstractConfiguration;

/**
 * A simple system properties configuration.
 *
 * @author eric.wittmann@redhat.com
 */
public class SystemPropertiesConfiguration extends AbstractConfiguration {

    /**
     * Constructor.
     */
    public SystemPropertiesConfiguration() {
    }

    /**
     * @see org.apache.commons.configuration.Configuration#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * @see org.apache.commons.configuration.Configuration#containsKey(java.lang.String)
     */
    @Override
    public boolean containsKey(String key) {
        return System.getProperties().containsKey(key);
    }

    /**
     * @see org.apache.commons.configuration.Configuration#getProperty(java.lang.String)
     */
    @Override
    public Object getProperty(String key) {
        return System.getProperties().getProperty(key);
    }

    /**
     * @see org.apache.commons.configuration.Configuration#getKeys()
     */
    @Override
    public Iterator<String> getKeys() {
        Set<String> keys = new HashSet<String>();
        Set<Object> keySet = System.getProperties().keySet();
        for (Object object : keySet) {
            keys.add(String.valueOf(object));
        }
        return keys.iterator();
    }

    /**
     * @see org.apache.commons.configuration.AbstractConfiguration#addPropertyDirect(java.lang.String, java.lang.Object)
     */
    @Override
    protected void addPropertyDirect(String key, Object value) {
        System.getProperties().setProperty(key, String.valueOf(value));
    }
}
