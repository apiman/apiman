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

package io.apiman.manager.api.rest.impl;

import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.manager.api.beans.actions.ActionBean;
import io.apiman.manager.api.beans.apps.ApplicationStatus;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.services.ServiceGatewayBean;
import io.apiman.manager.api.beans.services.ServiceStatus;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.core.IApplicationValidator;
import io.apiman.manager.api.core.IServiceValidator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.contract.IActionResource;
import io.apiman.manager.api.rest.contract.IOrganizationResource;
import io.apiman.manager.api.rest.contract.exceptions.ActionException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ServiceVersionNotFoundException;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.i18n.Messages;
import io.apiman.manager.api.rest.impl.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Implementation of the Action API.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ActionResourceImpl implements IActionResource {

    @Inject IStorage storage;
    @Inject IStorageQuery query;
    @Inject
    private IGatewayLinkFactory gatewayLinkFactory;
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
     * @see io.apiman.manager.api.rest.contract.IActionResource#performAction(io.apiman.manager.api.beans.actions.ActionBean)
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
            case unregisterApplication:
                unregisterApplication(action);
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
            versionBean = orgs.getServiceVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
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
        
        // Publish the service to all relevant gateways
        try {
            Set<ServiceGatewayBean> gateways = versionBean.getGateways();
            if (gateways == null) {
                throw new PublishingException("No gateways specified for service!"); //$NON-NLS-1$
            }
            for (ServiceGatewayBean serviceGatewayBean : gateways) {
                IGatewayLink gatewayLink = createGatewayLink(serviceGatewayBean.getGatewayId());
                gatewayLink.publishService(gatewaySvc);
                gatewayLink.close();
            }
        } catch (PublishingException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError")); //$NON-NLS-1$
        }
        
        versionBean.setStatus(ServiceStatus.Published);
        try {
            storage.beginTx();
            storage.update(versionBean);
            storage.createAuditEntry(AuditUtils.servicePublished(versionBean, securityContext));
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError")); //$NON-NLS-1$
        }
    }

    /**
     * Creates a gateway link given a gateway id.
     * @param gatewayId
     */
    private IGatewayLink createGatewayLink(String gatewayId) throws PublishingException {
        try {
            storage.beginTx();
            GatewayBean gateway = storage.get(gatewayId, GatewayBean.class);
            if (gateway == null) {
                throw new GatewayNotFoundException();
            }
            IGatewayLink link = gatewayLinkFactory.create(gateway);
            storage.commitTx();
            return link;
        } catch (GatewayNotFoundException e) {
            storage.rollbackTx();
            throw e;
        } catch (Exception e) {
            storage.rollbackTx();
            throw new PublishingException(e.getMessage(), e);
        }
    }

    /**
     * Retires a service that is currently published to the Gateway.
     * @param action
     */
    private void retireService(ActionBean action) throws ActionException {
        if (!securityContext.hasPermission(PermissionType.svcEdit, action.getOrganizationId()))
            throw ExceptionFactory.notAuthorizedException();

        ServiceVersionBean versionBean = null;
        try {
            versionBean = orgs.getServiceVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (ServiceVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ServiceNotFound")); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - service must be Ready.
        if (versionBean.getStatus() != ServiceStatus.Published) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidServiceStatus")); //$NON-NLS-1$
        }

        Service gatewaySvc = new Service();
        gatewaySvc.setOrganizationId(versionBean.getService().getOrganizationId());
        gatewaySvc.setServiceId(versionBean.getService().getId());
        gatewaySvc.setVersion(versionBean.getVersion());
        
        // Publish the service to all relevant gateways
        try {
            Set<ServiceGatewayBean> gateways = versionBean.getGateways();
            if (gateways == null) {
                throw new PublishingException("No gateways specified for service!"); //$NON-NLS-1$
            }
            for (ServiceGatewayBean serviceGatewayBean : gateways) {
                IGatewayLink gatewayLink = createGatewayLink(serviceGatewayBean.getGatewayId());
                gatewayLink.retireService(gatewaySvc);
                gatewayLink.close();
            }
        } catch (PublishingException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError")); //$NON-NLS-1$
        }
        
        versionBean.setStatus(ServiceStatus.Retired);
        try {
            storage.beginTx();
            storage.update(versionBean);
            storage.createAuditEntry(AuditUtils.serviceRetired(versionBean, securityContext));
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError")); //$NON-NLS-1$
        }
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
            versionBean = orgs.getAppVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
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

        // Next, register the application with *all* relevant gateways.  This is done by 
        // looking up all referenced services and getting the gateway information for them.
        // Each of those gateways must be told about the application.
        try {
            Map<String, IGatewayLink> links = new HashMap<String, IGatewayLink>();
            for (Contract contract : application.getContracts()) {
                ServiceVersionBean svb = query.getServiceVersion(contract.getServiceOrgId(), contract.getServiceId(), contract.getServiceVersion());
                Set<ServiceGatewayBean> gateways = svb.getGateways();
                if (gateways == null) {
                    throw new PublishingException("No gateways specified for service: " + svb.getService().getName()); //$NON-NLS-1$
                }
                for (ServiceGatewayBean serviceGatewayBean : gateways) {
                    if (!links.containsKey(serviceGatewayBean.getGatewayId())) {
                        IGatewayLink gatewayLink = createGatewayLink(serviceGatewayBean.getGatewayId());
                        links.put(serviceGatewayBean.getGatewayId(), gatewayLink);
                    }
                }
            }
            for (IGatewayLink gatewayLink : links.values()) {
                gatewayLink.registerApplication(application);
                gatewayLink.close();
            }
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError"), e); //$NON-NLS-1$
        } catch (PublishingException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError"), e); //$NON-NLS-1$
        }
        
        versionBean.setStatus(ApplicationStatus.Registered);
        
        try {
            storage.beginTx();
            storage.update(versionBean);
            storage.createAuditEntry(AuditUtils.applicationRegistered(versionBean, securityContext));
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
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
    private void unregisterApplication(ActionBean action) throws ActionException {
        if (!securityContext.hasPermission(PermissionType.appEdit, action.getOrganizationId()))
            throw ExceptionFactory.notAuthorizedException();

        ApplicationVersionBean versionBean = null;
        List<ContractSummaryBean> contractBeans = null;
        try {
            versionBean = orgs.getAppVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (ApplicationVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ApplicationNotFound")); //$NON-NLS-1$
        }
        try {
            contractBeans = query.getApplicationContracts(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ApplicationNotFound"), e); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - application must be Ready.
        if (versionBean.getStatus() != ApplicationStatus.Registered) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidApplicationStatus")); //$NON-NLS-1$
        }

        Application application = new Application();
        application.setOrganizationId(versionBean.getApplication().getOrganizationId());
        application.setApplicationId(versionBean.getApplication().getId());
        application.setVersion(versionBean.getVersion());

        // Next, unregister the application from *all* relevant gateways.  This is done by 
        // looking up all referenced services and getting the gateway information for them.
        // Each of those gateways must be told about the application.
        try {
            Map<String, IGatewayLink> links = new HashMap<String, IGatewayLink>();
            for (ContractSummaryBean contractBean : contractBeans) {
                ServiceVersionBean svb = query.getServiceVersion(contractBean.getServiceOrganizationId(),
                        contractBean.getServiceId(), contractBean.getServiceVersion());
                Set<ServiceGatewayBean> gateways = svb.getGateways();
                if (gateways == null) {
                    throw new PublishingException("No gateways specified for service: " + svb.getService().getName()); //$NON-NLS-1$
                }
                for (ServiceGatewayBean serviceGatewayBean : gateways) {
                    if (!links.containsKey(serviceGatewayBean.getGatewayId())) {
                        IGatewayLink gatewayLink = createGatewayLink(serviceGatewayBean.getGatewayId());
                        links.put(serviceGatewayBean.getGatewayId(), gatewayLink);
                    }
                }
            }
            for (IGatewayLink gatewayLink : links.values()) {
                gatewayLink.unregisterApplication(application);
                gatewayLink.close();
            }
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError"), e); //$NON-NLS-1$
        } catch (PublishingException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError"), e); //$NON-NLS-1$
        }
        
        versionBean.setStatus(ApplicationStatus.Retired);

        try {
            storage.beginTx();
            storage.update(versionBean);
            storage.createAuditEntry(AuditUtils.applicationUnregistered(versionBean, securityContext));
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError")); //$NON-NLS-1$
        }
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

    /**
     * @return the gatewayLinkFactory
     */
    public IGatewayLinkFactory getGatewayLinkFactory() {
        return gatewayLinkFactory;
    }

    /**
     * @param gatewayLinkFactory the gatewayLinkFactory to set
     */
    public void setGatewayLinkFactory(IGatewayLinkFactory gatewayLinkFactory) {
        this.gatewayLinkFactory = gatewayLinkFactory;
    }
        
}
