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

package io.apiman.gateway.api.rest.impl;

import io.apiman.gateway.api.rest.contract.IApplicationResource;
import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;


/**
 * Implementation of the Application API.
 * 
 * @author eric.wittmann@redhat.com
 */
public class ApplicationResourceImpl extends AbstractResourceImpl implements IApplicationResource {

    /**
     * Constructor.
     */
    public ApplicationResourceImpl() {
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IApplicationResource#register(io.apiman.gateway.engine.beans.Application)
     */
    @Override
    public void register(Application application) throws RegistrationException, NotAuthorizedException {
        getEngine().registerApplication(application);
    }
    
    /**
     * @see io.apiman.gateway.api.rest.contract.IApplicationResource#unregister(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void unregister(String organizationId, String applicationId, String version)
            throws RegistrationException, NotAuthorizedException {
        getEngine().unregisterApplication(organizationId, applicationId, version);
    }
    
}
