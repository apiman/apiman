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

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.manager.api.beans.actions.ActionBean;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.apps.ApplicationStatus;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IApiValidator;
import io.apiman.manager.api.core.IApplicationValidator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.contract.IActionResource;
import io.apiman.manager.api.rest.contract.IOrganizationResource;
import io.apiman.manager.api.rest.contract.exceptions.ActionException;
import io.apiman.manager.api.rest.contract.exceptions.ApiVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.ApplicationVersionNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.GatewayNotFoundException;
import io.apiman.manager.api.rest.contract.exceptions.PlanVersionNotFoundException;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.impl.i18n.Messages;
import io.apiman.manager.api.rest.impl.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.ArrayList;
import java.util.Date;
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
    @Inject IGatewayLinkFactory gatewayLinkFactory;
    @Inject IOrganizationResource orgs;

    @Inject IApiValidator apiValidator;
    @Inject IApplicationValidator applicationValidator;

    @Inject ISecurityContext securityContext;

    @Inject @ApimanLogger(ActionResourceImpl.class) IApimanLogger log;

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
            case publishAPI:
                publishApi(action);
                return;
            case retireAPI:
                retireApi(action);
                return;
            case registerApplication:
                registerApplication(action);
                return;
            case unregisterApplication:
                unregisterApplication(action);
                return;
            case lockPlan:
                lockPlan(action);
                return;
            default:
                throw ExceptionFactory.actionException("Action type not supported: " + action.getType().toString()); //$NON-NLS-1$
        }
    }

    /**
     * Publishes an API to the gateway.
     * @param action
     */
    private void publishApi(ActionBean action) throws ActionException {
        if (!securityContext.hasPermission(PermissionType.apiAdmin, action.getOrganizationId()))
            throw ExceptionFactory.notAuthorizedException();

        ApiVersionBean versionBean = null;
        try {
            versionBean = orgs.getApiVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (ApiVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ApiNotFound")); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - API must be Ready.
        if (versionBean.getStatus() != ApiStatus.Ready) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidApiStatus")); //$NON-NLS-1$
        }

        Api gatewayApi = new Api();
        gatewayApi.setEndpoint(versionBean.getEndpoint());
        gatewayApi.setEndpointType(versionBean.getEndpointType().toString());
        if (versionBean.getEndpointContentType() != null) {
            gatewayApi.setEndpointContentType(versionBean.getEndpointContentType().toString());
        }
        gatewayApi.setEndpointProperties(versionBean.getEndpointProperties());
        gatewayApi.setOrganizationId(versionBean.getApi().getOrganization().getId());
        gatewayApi.setApiId(versionBean.getApi().getId());
        gatewayApi.setVersion(versionBean.getVersion());
        gatewayApi.setPublicAPI(versionBean.isPublicAPI());
        boolean hasTx = false;
        try {
            if (versionBean.isPublicAPI()) {
                List<Policy> policiesToPublish = new ArrayList<>();
                List<PolicySummaryBean> apiPolicies = query.getPolicies(action.getOrganizationId(),
                        action.getEntityId(), action.getEntityVersion(), PolicyType.Api);
                storage.beginTx();
                hasTx = true;
                for (PolicySummaryBean policySummaryBean : apiPolicies) {
                    PolicyBean apiPolicy = storage.getPolicy(PolicyType.Api, action.getOrganizationId(),
                            action.getEntityId(), action.getEntityVersion(), policySummaryBean.getId());
                    Policy policyToPublish = new Policy();
                    policyToPublish.setPolicyJsonConfig(apiPolicy.getConfiguration());
                    policyToPublish.setPolicyImpl(apiPolicy.getDefinition().getPolicyImpl());
                    policiesToPublish.add(policyToPublish);
                }
                gatewayApi.setApiPolicies(policiesToPublish);
            }
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError"), e); //$NON-NLS-1$
        } finally {
            if (hasTx) {
                storage.rollbackTx();
            }
        }

        // Publish the API to all relevant gateways
        try {
            storage.beginTx();
            Set<ApiGatewayBean> gateways = versionBean.getGateways();
            if (gateways == null) {
                throw new PublishingException("No gateways specified for API!"); //$NON-NLS-1$
            }
            for (ApiGatewayBean apiGatewayBean : gateways) {
                IGatewayLink gatewayLink = createGatewayLink(apiGatewayBean.getGatewayId());
                gatewayLink.publishApi(gatewayApi);
                gatewayLink.close();
            }

            versionBean.setStatus(ApiStatus.Published);
            versionBean.setPublishedOn(new Date());

            ApiBean api = storage.getApi(action.getOrganizationId(), action.getEntityId());
            if (api == null) {
                throw new PublishingException("Error: could not find API - " + action.getOrganizationId() + "=>" + action.getEntityId()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (api.getNumPublished() == null) {
                api.setNumPublished(1);
            } else {
                api.setNumPublished(api.getNumPublished() + 1);
            }

            storage.updateApi(api);
            storage.updateApiVersion(versionBean);
            storage.createAuditEntry(AuditUtils.apiPublished(versionBean, securityContext));
            storage.commitTx();
        } catch (PublishingException e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError"), e); //$NON-NLS-1$
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("PublishError"), e); //$NON-NLS-1$
        }

        log.debug(String.format("Successfully published API %s on specified gateways: %s", //$NON-NLS-1$
                versionBean.getApi().getName(), versionBean.getApi()));
    }

    /**
     * Creates a gateway link given a gateway id.
     * @param gatewayId
     */
    private IGatewayLink createGatewayLink(String gatewayId) throws PublishingException {
        try {
            GatewayBean gateway = storage.getGateway(gatewayId);
            if (gateway == null) {
                throw new GatewayNotFoundException();
            }
            IGatewayLink link = gatewayLinkFactory.create(gateway);
            return link;
        } catch (GatewayNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new PublishingException(e.getMessage(), e);
        }
    }

    /**
     * Retires an API that is currently published to the Gateway.
     * @param action
     */
    private void retireApi(ActionBean action) throws ActionException {
        if (!securityContext.hasPermission(PermissionType.apiAdmin, action.getOrganizationId()))
            throw ExceptionFactory.notAuthorizedException();

        ApiVersionBean versionBean = null;
        try {
            versionBean = orgs.getApiVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (ApiVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ApiNotFound")); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - API must be Published.
        if (versionBean.getStatus() != ApiStatus.Published) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidApiStatus")); //$NON-NLS-1$
        }

        Api gatewayApi = new Api();
        gatewayApi.setOrganizationId(versionBean.getApi().getOrganization().getId());
        gatewayApi.setApiId(versionBean.getApi().getId());
        gatewayApi.setVersion(versionBean.getVersion());

        // Retire the API from all relevant gateways
        try {
            storage.beginTx();
            Set<ApiGatewayBean> gateways = versionBean.getGateways();
            if (gateways == null) {
                throw new PublishingException("No gateways specified for API!"); //$NON-NLS-1$
            }
            for (ApiGatewayBean apiGatewayBean : gateways) {
                IGatewayLink gatewayLink = createGatewayLink(apiGatewayBean.getGatewayId());
                gatewayLink.retireApi(gatewayApi);
                gatewayLink.close();
            }

            versionBean.setStatus(ApiStatus.Retired);
            versionBean.setRetiredOn(new Date());

            ApiBean api = storage.getApi(action.getOrganizationId(), action.getEntityId());
            if (api == null) {
                throw new PublishingException("Error: could not find API - " + action.getOrganizationId() + "=>" + action.getEntityId()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (api.getNumPublished() == null || api.getNumPublished() == 0) {
                api.setNumPublished(0);
            } else {
                api.setNumPublished(api.getNumPublished() - 1);
            }

            storage.updateApi(api);
            storage.updateApiVersion(versionBean);
            storage.createAuditEntry(AuditUtils.apiRetired(versionBean, securityContext));
            storage.commitTx();
        } catch (PublishingException e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("RetireError"), e); //$NON-NLS-1$
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("RetireError"), e); //$NON-NLS-1$
        }

        log.debug(String.format("Successfully retired API %s on specified gateways: %s", //$NON-NLS-1$
                versionBean.getApi().getName(), versionBean.getApi()));
    }

    /**
     * Registers an application (along with all of its contracts) to the gateway.
     * @param action
     */
    private void registerApplication(ActionBean action) throws ActionException {
        if (!securityContext.hasPermission(PermissionType.appAdmin, action.getOrganizationId()))
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

        // Validate that it's ok to perform this action - application must be Ready or Registered.
        if (versionBean.getStatus() == ApplicationStatus.Registered) {
            Date modOn = versionBean.getModifiedOn();
            Date publishedOn = versionBean.getPublishedOn();
            int c = modOn.compareTo(publishedOn);
            if (c <= 0) {
                throw ExceptionFactory.actionException(Messages.i18n.format("ApplicationReRegisterNotRequired")); //$NON-NLS-1$
            }
        }

        Application application = new Application();
        application.setOrganizationId(versionBean.getApplication().getOrganization().getId());
        application.setApplicationId(versionBean.getApplication().getId());
        application.setVersion(versionBean.getVersion());

        Set<Contract> contracts = new HashSet<>();
        for (ContractSummaryBean contractBean : contractBeans) {
            Contract contract = new Contract();
            contract.setApiKey(contractBean.getApikey());
            contract.setPlan(contractBean.getPlanId());
            contract.setApiId(contractBean.getApiId());
            contract.setApiOrgId(contractBean.getApiOrganizationId());
            contract.setApiVersion(contractBean.getApiVersion());
            contract.getPolicies().addAll(aggregateContractPolicies(contractBean));
            contracts.add(contract);
        }
        application.setContracts(contracts);

        // Next, register the application with *all* relevant gateways.  This is done by
        // looking up all referenced APIs and getting the gateway information for them.
        // Each of those gateways must be told about the application.
        try {
            storage.beginTx();
            Map<String, IGatewayLink> links = new HashMap<>();
            for (Contract contract : application.getContracts()) {
                ApiVersionBean svb = storage.getApiVersion(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
                Set<ApiGatewayBean> gateways = svb.getGateways();
                if (gateways == null) {
                    throw new PublishingException("No gateways specified for API: " + svb.getApi().getName()); //$NON-NLS-1$
                }
                for (ApiGatewayBean apiGatewayBean : gateways) {
                    if (!links.containsKey(apiGatewayBean.getGatewayId())) {
                        IGatewayLink gatewayLink = createGatewayLink(apiGatewayBean.getGatewayId());
                        links.put(apiGatewayBean.getGatewayId(), gatewayLink);
                    }
                }
            }
            for (IGatewayLink gatewayLink : links.values()) {
                gatewayLink.registerApplication(application);
                gatewayLink.close();
            }
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("RegisterError"), e); //$NON-NLS-1$
        }

        versionBean.setStatus(ApplicationStatus.Registered);
        versionBean.setPublishedOn(new Date());

        try {
            storage.beginTx();
            storage.updateApplicationVersion(versionBean);
            storage.createAuditEntry(AuditUtils.applicationRegistered(versionBean, securityContext));
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("RegisterError"), e); //$NON-NLS-1$
        }

        log.debug(String.format("Successfully registered Application %s on specified gateways: %s", //$NON-NLS-1$
                versionBean.getApplication().getName(), versionBean.getApplication()));
    }

    /**
     * Aggregates the API, app, and plan policies into a single ordered list.
     * @param contractBean
     */
    private List<Policy> aggregateContractPolicies(ContractSummaryBean contractBean) {
        try {
            List<Policy> policies = new ArrayList<>();
            PolicyType [] types = new PolicyType[] {
                    PolicyType.Application, PolicyType.Plan, PolicyType.Api
            };
            for (PolicyType policyType : types) {
                String org, id, ver;
                switch (policyType) {
                  case Application: {
                      org = contractBean.getAppOrganizationId();
                      id = contractBean.getAppId();
                      ver = contractBean.getAppVersion();
                      break;
                  }
                  case Plan: {
                      org = contractBean.getApiOrganizationId();
                      id = contractBean.getPlanId();
                      ver = contractBean.getPlanVersion();
                      break;
                  }
                  case Api: {
                      org = contractBean.getApiOrganizationId();
                      id = contractBean.getApiId();
                      ver = contractBean.getApiVersion();
                      break;
                  }
                  default: {
                      throw new RuntimeException("Missing case for switch!"); //$NON-NLS-1$
                  }
                }
                List<PolicySummaryBean> appPolicies = query.getPolicies(org, id, ver, policyType);
                storage.beginTx();
                try {
                    for (PolicySummaryBean policySummaryBean : appPolicies) {
                        PolicyBean policyBean = storage.getPolicy(policyType, org, id, ver, policySummaryBean.getId());
                        Policy policy = new Policy();
                        policy.setPolicyJsonConfig(policyBean.getConfiguration());
                        policy.setPolicyImpl(policyBean.getDefinition().getPolicyImpl());
                        policies.add(policy);
                    }
                } finally {
                    storage.commitTx();
                }
            }
            return policies;
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PolicyPublishError", contractBean.getApikey()), e); //$NON-NLS-1$
        }
    }

    /**
     * De-registers an application that is currently registered with the gateway.
     * @param action
     */
    private void unregisterApplication(ActionBean action) throws ActionException {
        if (!securityContext.hasPermission(PermissionType.appAdmin, action.getOrganizationId()))
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
        application.setOrganizationId(versionBean.getApplication().getOrganization().getId());
        application.setApplicationId(versionBean.getApplication().getId());
        application.setVersion(versionBean.getVersion());

        // Next, unregister the application from *all* relevant gateways.  This is done by
        // looking up all referenced APIs and getting the gateway information for them.
        // Each of those gateways must be told about the application.
        try {
            storage.beginTx();
            Map<String, IGatewayLink> links = new HashMap<>();
            for (ContractSummaryBean contractBean : contractBeans) {
                ApiVersionBean svb = storage.getApiVersion(contractBean.getApiOrganizationId(),
                        contractBean.getApiId(), contractBean.getApiVersion());
                Set<ApiGatewayBean> gateways = svb.getGateways();
                if (gateways == null) {
                    throw new PublishingException("No gateways specified for API: " + svb.getApi().getName()); //$NON-NLS-1$
                }
                for (ApiGatewayBean apiGatewayBean : gateways) {
                    if (!links.containsKey(apiGatewayBean.getGatewayId())) {
                        IGatewayLink gatewayLink = createGatewayLink(apiGatewayBean.getGatewayId());
                        links.put(apiGatewayBean.getGatewayId(), gatewayLink);
                    }
                }
            }
            storage.commitTx();
            for (IGatewayLink gatewayLink : links.values()) {
                gatewayLink.unregisterApplication(application);
                gatewayLink.close();
            }
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("UnregisterError"), e); //$NON-NLS-1$
        }

        versionBean.setStatus(ApplicationStatus.Retired);
        versionBean.setRetiredOn(new Date());

        try {
            storage.beginTx();
            storage.updateApplicationVersion(versionBean);
            storage.createAuditEntry(AuditUtils.applicationUnregistered(versionBean, securityContext));
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("UnregisterError"), e); //$NON-NLS-1$
        }

        log.debug(String.format("Successfully registered Application %s on specified gateways: %s", //$NON-NLS-1$
                versionBean.getApplication().getName(), versionBean.getApplication()));
    }

    /**
     * Locks the plan.
     * @param action
     */
    private void lockPlan(ActionBean action) throws ActionException {
        if (!securityContext.hasPermission(PermissionType.planAdmin, action.getOrganizationId()))
            throw ExceptionFactory.notAuthorizedException();

        PlanVersionBean versionBean = null;
        try {
            versionBean = orgs.getPlanVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (PlanVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("PlanNotFound")); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - plan must not already be locked
        if (versionBean.getStatus() == PlanStatus.Locked) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidPlanStatus")); //$NON-NLS-1$
        }

        versionBean.setStatus(PlanStatus.Locked);
        versionBean.setLockedOn(new Date());

        try {
            storage.beginTx();
            storage.updatePlanVersion(versionBean);
            storage.createAuditEntry(AuditUtils.planLocked(versionBean, securityContext));
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("LockError"), e); //$NON-NLS-1$
        }

        log.debug(String.format("Successfully locked Plan %s: %s", //$NON-NLS-1$
                versionBean.getPlan().getName(), versionBean.getPlan()));
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
     * @return the apiValidator
     */
    public IApiValidator getApiValidator() {
        return apiValidator;
    }

    /**
     * @param apiValidator the apiValidator to set
     */
    public void setApiValidator(IApiValidator apiValidator) {
        this.apiValidator = apiValidator;
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
