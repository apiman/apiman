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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.overlord.apiman.dt.api.beans.actions.ActionBean;
import org.overlord.apiman.dt.api.beans.apps.ApplicationStatus;
import org.overlord.apiman.dt.api.beans.apps.ApplicationVersionBean;
import org.overlord.apiman.dt.api.beans.idm.PermissionType;
import org.overlord.apiman.dt.api.beans.policies.PolicyBean;
import org.overlord.apiman.dt.api.beans.policies.PolicyType;
import org.overlord.apiman.dt.api.beans.services.ServiceStatus;
import org.overlord.apiman.dt.api.beans.services.ServiceVersionBean;
import org.overlord.apiman.dt.api.beans.summary.ContractSummaryBean;
import org.overlord.apiman.dt.api.core.IApplicationValidator;
import org.overlord.apiman.dt.api.core.IIdmStorage;
import org.overlord.apiman.dt.api.core.IServiceValidator;
import org.overlord.apiman.dt.api.core.IStorage;
import org.overlord.apiman.dt.api.core.IStorageQuery;
import org.overlord.apiman.dt.api.core.exceptions.StorageException;
import org.overlord.apiman.dt.api.gateway.IGatewayLink;
import org.overlord.apiman.dt.api.rest.contract.IActionResource;
import org.overlord.apiman.dt.api.rest.contract.IOrganizationResource;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ActionException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import org.overlord.apiman.dt.api.rest.impl.i18n.Messages;
import org.overlord.apiman.dt.api.rest.impl.util.ExceptionFactory;
import org.overlord.apiman.dt.api.security.ISecurityContext;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Contract;
import org.overlord.apiman.rt.engine.beans.Policy;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;

/**
 * Implementation of the Action API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ActionResourceImpl implements IActionResource {

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject IIdmStorage idmStorage;
    @Inject IGatewayLink gatewayLink;
    
    @Inject IOrganizationResource orgs;
    
    @Inject IServiceValidator serviceValidator;
    @Inject IApplicationValidator applicationValidator;

    @Inject ISecurityContext securityContext;

    /**
     * Constructor.
     */
    public ActionResourceImpl() {
    }

    /**
     * @see org.overlord.apiman.dt.api.rest.contract.IActionResource#performAction(org.overlord.apiman.dt.api.beans.actions.ActionBean)
     */
    @Override
    public void performAction(ActionBean action) throws ActionException {
        switch (action.getType()) {
            case publishService:
                publishService(action);
                return;
            case retireService:
                retireService(action);
                return;
            case registerApplication:
                registerApplication(action);
                return;
            case deregisterApplication:
                deregisterApplication(action);
                return;
            default:
                throw ExceptionFactory.actionException("Action type not supported: " + action.getType().toString()); //$NON-NLS-1$
        }
    }

    /**
     * Publishes a service to the gateway.
     * @param action
     */
    private void publishService(ActionBean action) throws ActionException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, action.getOrganizationId()))
            throw ExceptionFactory.notAuthorizedException();

        ServiceVersionBean versionBean = null;
        try {
            versionBean = getOrgs().getServiceVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (ServiceVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ServiceNotFound")); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - service must be Ready.
        try {
            if (!serviceValidator.isReady(versionBean)) {
                throw ExceptionFactory.actionException(Messages.i18n.format("InvalidServiceStatus")); //$NON-NLS-1$
            }
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidServiceStatus"), e); //$NON-NLS-1$
        }

        Service gatewaySvc = new Service();
        gatewaySvc.setEndpoint(versionBean.getEndpoint());
        gatewaySvc.setEndpointType(versionBean.getEndpointType().toString());
        gatewaySvc.setOrganizationId(versionBean.getService().getOrganizationId());
        gatewaySvc.setServiceId(versionBean.getService().getId());
        gatewaySvc.setVersion(versionBean.getVersion());
        
        try {
            gatewayLink.publishService(gatewaySvc);
        } catch (PublishingException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError")); //$NON-NLS-1$
        }
        
        versionBean.setStatus(ServiceStatus.Published);
        try {
            storage.update(versionBean);
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError")); //$NON-NLS-1$
        }
    }

    /**
     * Retires a service that is currently published to the Gateway.
     * @param action
     */
    private void retireService(ActionBean action) throws ActionException {
        // TODO Auto-generated method stub
        throw ExceptionFactory.actionException("Not yet implemented."); //$NON-NLS-1$
    }

    /**
     * Registers an application (along with all of its contracts) to the gateway.
     * @param action
     */
    private void registerApplication(ActionBean action) throws ActionException {
        if (!securityContext.hasPermission(PermissionType.appEdit, action.getOrganizationId()))
            throw ExceptionFactory.notAuthorizedException();

        ApplicationVersionBean versionBean = null;
        List<ContractSummaryBean> contractBeans = null;
        try {
            versionBean = getOrgs().getAppVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (ApplicationVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ApplicationNotFound")); //$NON-NLS-1$
        }
        try {
            contractBeans = query.getApplicationContracts(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ApplicationNotFound"), e); //$NON-NLS-1$
        }
        
        // Validate that it's ok to perform this action - application must be Ready.
        try {
            if (!applicationValidator.isReady(versionBean)) {
                throw ExceptionFactory.actionException(Messages.i18n.format("InvalidApplicationStatus")); //$NON-NLS-1$
            }
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidApplicationStatus"), e); //$NON-NLS-1$
        }

        Application application = new Application();
        application.setOrganizationId(versionBean.getApplication().getOrganizationId());
        application.setApplicationId(versionBean.getApplication().getId());
        application.setVersion(versionBean.getVersion());
        
        Set<Contract> contracts = new HashSet<Contract>();
        for (ContractSummaryBean contractBean : contractBeans) {
            Contract contract = new Contract();
            contract.setApiKey(contractBean.getKey());
            contract.setServiceId(contractBean.getServiceId());
            contract.setServiceOrgId(contractBean.getServiceOrganizationId());
            contract.setServiceVersion(contractBean.getServiceVersion());
            contract.getPolicies().addAll(aggregateContractPolicies(contractBean));
            contracts.add(contract);
        }
        application.setContracts(contracts);
        
        try {
            gatewayLink.registerApplication(application);
        } catch (PublishingException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError")); //$NON-NLS-1$
        }
        
        versionBean.setStatus(ApplicationStatus.Registered);
        
        try {
            storage.update(versionBean);
        } catch (Exception e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError")); //$NON-NLS-1$
        }

    }

    /**
     * Aggregates the service, app, and plan policies into a single ordered list.
     * @param contractBean
     */
    private List<Policy> aggregateContractPolicies(ContractSummaryBean contractBean) {
        try {
            List<Policy> policies = new ArrayList<Policy>();
            PolicyType [] types = new PolicyType[3];
            types[0] = PolicyType.Application;
            types[1] = PolicyType.Plan;
            types[2] = PolicyType.Service;
            for (PolicyType pt : types) {
                String org, id, ver;
                switch (pt) {
                  case Application: {
                      org = contractBean.getAppOrganizationId();
                      id = contractBean.getAppId();
                      ver = contractBean.getAppVersion();
                      break;
                  }
                  case Plan: {
                      org = contractBean.getServiceOrganizationId();
                      id = contractBean.getPlanId();
                      ver = contractBean.getPlanVersion();
                      break;
                  }
                  case Service: {
                      org = contractBean.getServiceOrganizationId();
                      id = contractBean.getServiceId();
                      ver = contractBean.getServiceVersion();
                      break;
                  }
                  default: {
                      throw new RuntimeException("Missing case for switch!"); //$NON-NLS-1$
                  }
                }
                List<PolicyBean> appPolicies = query.getPolicies(org, id, ver, pt);
                for (PolicyBean policyBean : appPolicies) {
                    Policy policy = new Policy();
                    policy.setPolicyJsonConfig(policyBean.getConfiguration());
                    policy.setPolicyImpl(policyBean.getDefinition().getPolicyImpl());
                    policies.add(policy);
                }
            }
            return policies;
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PolicyPublishError", contractBean.getKey()), e); //$NON-NLS-1$
        }
    }

    /**
     * De-registers an application that is currently registered with the gateway.
     * @param action
     */
    private void deregisterApplication(ActionBean action) throws ActionException {
        // TODO Auto-generated method stub
        throw ExceptionFactory.actionException("Not yet implemented."); //$NON-NLS-1$
    }

    /**
     * @return the storage
     */
    public IStorage getStorage() {
        return storage;
    }

    /**
     * @param storage the storage to set
     */
    public void setStorage(IStorage storage) {
        this.storage = storage;
    }

    /**
     * @return the query
     */
    public IStorageQuery getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(IStorageQuery query) {
        this.query = query;
    }

    /**
     * @return the idmStorage
     */
    public IIdmStorage getIdmStorage() {
        return idmStorage;
    }

    /**
     * @param idmStorage the idmStorage to set
     */
    public void setIdmStorage(IIdmStorage idmStorage) {
        this.idmStorage = idmStorage;
    }

    /**
     * @return the gatewayLink
     */
    public IGatewayLink getGatewayLink() {
        return gatewayLink;
    }

    /**
     * @param gatewayLink the gatewayLink to set
     */
    public void setGatewayLink(IGatewayLink gatewayLink) {
        this.gatewayLink = gatewayLink;
    }

    /**
     * @return the serviceValidator
     */
    public IServiceValidator getServiceValidator() {
        return serviceValidator;
    }

    /**
     * @param serviceValidator the serviceValidator to set
     */
    public void setServiceValidator(IServiceValidator serviceValidator) {
        this.serviceValidator = serviceValidator;
    }

    /**
     * @return the applicationValidator
     */
    public IApplicationValidator getApplicationValidator() {
        return applicationValidator;
    }

    /**
     * @param applicationValidator the applicationValidator to set
     */
    public void setApplicationValidator(IApplicationValidator applicationValidator) {
        this.applicationValidator = applicationValidator;
    }

    /**
     * @return the securityContext
     */
    public ISecurityContext getSecurityContext() {
        return securityContext;
    }

    /**
     * @param securityContext the securityContext to set
     */
    public void setSecurityContext(ISecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    /**
     * @return the orgs
     */
    public IOrganizationResource getOrgs() {
        return orgs;
    }

    /**
     * @param orgs the orgs to set
     */
    public void setOrgs(IOrganizationResource orgs) {
        this.orgs = orgs;
    }
        
}
