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
package org.overlord.apiman.engine.policies;

import org.codehaus.jackson.map.ObjectMapper;
import org.overlord.apiman.rt.engine.beans.exceptions.ConfigurationParseException;
import org.overlord.apiman.rt.engine.policy.AbstractPolicy;

/**
 * A base class for policy impls that use jackson to parse configuration info.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractMappedPolicy<C> extends AbstractPolicy {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    protected C configuration;
    
    /**
     * Constructor.
     */
    public AbstractMappedPolicy() {
    }

    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#parseConfiguration(java.lang.String)
     */
    @Override
    public C parseConfiguration(String jsonConfiguration) throws ConfigurationParseException {
        try {
            return mapper.reader(getConfigurationClass()).readValue(jsonConfiguration);
        } catch (Exception e) {
            throw new ConfigurationParseException(e);
        }
    }

    /**
     * @return the class to use for JSON configuration deserialization
     */
    protected abstract Class<C> getConfigurationClass();
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#setConfiguration(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setConfiguration(Object config) {
        this.configuration = (C) config;
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.policy.IPolicy#getConfiguration()
     */
    @Override
    public C getConfiguration() {
        return this.configuration;
    }

}
