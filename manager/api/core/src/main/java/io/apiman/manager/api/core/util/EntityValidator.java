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
package io.apiman.manager.api.core.util;

import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.core.IApplicationValidator;
import io.apiman.manager.api.core.IServiceValidator;
import io.apiman.manager.api.core.IStorageQuery;

import java.util.List;

import javax.inject.Inject;

/**
 * Validates the state of various entities, including services and applications.
 *
 * @author eric.wittmann@redhat.com
 */
public class EntityValidator implements IServiceValidator, IApplicationValidator {
    
    @Inject
    private IStorageQuery storageQuery;
    
    /**
     * Constructor.
     */
    public EntityValidator() {
    }

    /**
     * @see io.apiman.manager.api.core.IApplicationValidator#isReady(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public boolean isReady(ApplicationVersionBean application) throws Exception {
        boolean hasContracts = true;
        
        List<ContractSummaryBean> contracts = storageQuery.getApplicationContracts(application.getApplication().getOrganizationId(), application
                .getApplication().getId(), application.getVersion());
        if (contracts.isEmpty()) {
            hasContracts = false;
        }
        
        return isReady(application, hasContracts);
    }
    
    /**
     * @see io.apiman.manager.api.core.IApplicationValidator#isReady(io.apiman.manager.api.beans.apps.ApplicationVersionBean, boolean)
     */
    @Override
    public boolean isReady(ApplicationVersionBean application, boolean hasContracts) throws Exception {
        boolean ready = hasContracts;
        return ready;
    }

    /**
     * @see io.apiman.manager.api.core.IServiceValidator#isReady(io.apiman.manager.api.beans.services.ServiceVersionBean)
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

    /**
     * @return the storageQuery
     */
    public IStorageQuery getStorageQuery() {
        return storageQuery;
    }

    /**
     * @param storageQuery the storageQuery to set
     */
    public void setStorageQuery(IStorageQuery storageQuery) {
        this.storageQuery = storageQuery;
    }

}
