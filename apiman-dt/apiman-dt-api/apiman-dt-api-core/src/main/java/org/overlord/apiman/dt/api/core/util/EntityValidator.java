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
package org.overlord.apiman.dt.api.core.util;

import java.util.List;

import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ContractSummaryBean;
import org.overlord.apiman.dt.api.core.IApplicationValidator;
import org.overlord.apiman.dt.api.core.IServiceValidator;
import org.overlord.apiman.dt.api.core.IStorageQuery;

/**
 * Validates the state of various entities, including services and applications.
 *
 * @author eric.wittmann@redhat.com
 */
public class EntityValidator implements IServiceValidator, IApplicationValidator {
    
    @Inject IStorageQuery storageQuery;
    
    /**
     * Constructor.
     */
    public EntityValidator() {
    }

    /**
     * @see org.overlord.apiman.dt.api.core.IApplicationValidator#isReady(org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public boolean isReady(ApplicationVersionBean application) throws Exception {
        boolean ready = true;
        
        List<ContractSummaryBean> contracts = storageQuery.getApplicationContracts(application.getApplication().getOrganizationId(), application
                .getApplication().getId(), application.getVersion());
        if (contracts.isEmpty()) {
            ready = false;
        }
        
        return ready;
    }

    /**
     * @see org.overlord.apiman.dt.api.core.IServiceValidator#isReady(org.overlord.apiman.dt.api.beans.services.ServiceVersionBean)
     */
    @Override
    public boolean isReady(ServiceVersionBean service) {
        boolean ready = true;
        if (service.getEndpoint() == null || service.getEndpoint().trim().length() == 0) {
            ready = false;
        }
        if (service.getPlans().isEmpty()) {
            ready = false;
        }
        return ready;
    }

}
