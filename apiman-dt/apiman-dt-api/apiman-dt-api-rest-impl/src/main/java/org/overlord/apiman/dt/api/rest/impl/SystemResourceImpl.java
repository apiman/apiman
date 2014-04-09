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

package org.overlord.apiman.dt.api.rest.impl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.system.SystemStatusBean;
import org.overlord.apiman.dt.api.config.Version;
import org.overlord.apiman.dt.api.persist.IStorage;
import org.overlord.apiman.dt.api.rest.contract.ISystemResource;

/**
 * Implementation of the System API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class SystemResourceImpl implements ISystemResource {
    
    @Inject IStorage storage;
    @Inject Version version;
    
    /**
     * Constructor.
     */
    public SystemResourceImpl() {
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.ISystemResource#getStatus()
     */
    @Override
    public SystemStatusBean getStatus() {
        SystemStatusBean rval = new SystemStatusBean();
        rval.setUp(storage != null);
        if (version != null) {
            rval.setVersion(version.getVersionString());
        }
        return rval;
    }
}
