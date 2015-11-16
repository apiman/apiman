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
import io.apiman.manager.api.beans.services.ServiceStatus;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.services.ServiceVersionStatusBean;
import io.apiman.manager.api.beans.services.StatusItemBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IApplicationValidator;
import io.apiman.manager.api.core.IServiceValidator;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.i18n.Messages;

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

        List<ContractSummaryBean> contracts = storageQuery.getApplicationContracts(application.getApplication().getOrganization().getId(), application
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
        if (service.getEndpointType() == null) {
            ready = false;
        }
        if (!service.isPublicService()) {
            if (service.getPlans() == null || service.getPlans().isEmpty()) {
                ready = false;
            }
        }
        if (service.getGateways() == null || service.getGateways().isEmpty()) {
            ready = false;
        }
        return ready;
    }
    
    /**
     * @see io.apiman.manager.api.core.IServiceValidator#getStatus(io.apiman.manager.api.beans.services.ServiceVersionBean, java.util.List)
     */
    @Override
    public ServiceVersionStatusBean getStatus(ServiceVersionBean service, List<PolicySummaryBean> policies) {
        ServiceVersionStatusBean status = new ServiceVersionStatusBean();
        status.setStatus(service.getStatus());
        
        // Why are we not yet "Ready"?
        if (service.getStatus() == ServiceStatus.Created || service.getStatus() == ServiceStatus.Ready) {
            // 1. Implementation endpoint + endpoint type
            /////////////////////////////////////////////
            StatusItemBean item = new StatusItemBean();
            item.setId("endpoint"); //$NON-NLS-1$
            item.setName(Messages.i18n.format("EntityValidator.endpoint.name")); //$NON-NLS-1$
            item.setDone(true);
            if (service.getEndpoint() == null || service.getEndpoint().trim().isEmpty() || service.getEndpointType() == null) {
                item.setDone(false);
                item.setRemediation(Messages.i18n.format("EntityValidator.endpoint.description")); //$NON-NLS-1$
            }
            status.getItems().add(item);

            // 2. Gateway selected
            item = new StatusItemBean();
            item.setId("gateways"); //$NON-NLS-1$
            item.setName(Messages.i18n.format("EntityValidator.gateways.name")); //$NON-NLS-1$
            item.setDone(true);
            if (service.getGateways() == null || service.getGateways().isEmpty()) {
                item.setDone(false);
                item.setRemediation(Messages.i18n.format("EntityValidator.gateways.description")); //$NON-NLS-1$
            }
            status.getItems().add(item);

            // 3. Public or at least one plan
            /////////////////////////////////
            item = new StatusItemBean();
            item.setId("plans"); //$NON-NLS-1$
            item.setName(Messages.i18n.format("EntityValidator.plans.name")); //$NON-NLS-1$
            item.setDone(true);
            if (!service.isPublicService()) {
                if (service.getPlans() == null || service.getPlans().isEmpty()) {
                    item.setDone(false);
                    item.setRemediation(Messages.i18n.format("EntityValidator.plans.description")); //$NON-NLS-1$
                }
            }
            status.getItems().add(item);

            // 4. At least one Policy (optional)
            ////////////////////////////////////
            item = new StatusItemBean();
            item.setId("policies"); //$NON-NLS-1$
            item.setName(Messages.i18n.format("EntityValidator.policies.name")); //$NON-NLS-1$
            item.setDone(true);
            item.setOptional(true);
            if (policies.isEmpty()) {
                item.setDone(false);
                item.setRemediation(Messages.i18n.format("EntityValidator.policies.description")); //$NON-NLS-1$
            }
            status.getItems().add(item);
        }
        
        return status;
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
