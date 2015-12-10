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

import io.apiman.gateway.api.rest.contract.ISystemResource;
import io.apiman.gateway.engine.beans.SystemStatus;

/**
 * Implementation of the System API.
 *
 * @author eric.wittmann@redhat.com
 */
public class SystemResourceImpl extends AbstractResourceImpl implements ISystemResource {

    /**
     * Constructor.
     */
    public SystemResourceImpl() {
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.ISystemResource#getStatus()
     */
    @Override
    public SystemStatus getStatus() {
        SystemStatus status = new SystemStatus();
        status.setId("apiman-gateway-api"); //$NON-NLS-1$
        status.setName("API Gateway REST API"); //$NON-NLS-1$
        status.setDescription("The API Gateway REST API is used by the API Manager to publish APIs and register clients.  You can use it directly if you wish, but if you are utilizing the API Manager then it's probably best to avoid invoking this API directly."); //$NON-NLS-1$
        status.setUp(true);
        status.setVersion(getEngine().getVersion());
        return status;
    }

}
