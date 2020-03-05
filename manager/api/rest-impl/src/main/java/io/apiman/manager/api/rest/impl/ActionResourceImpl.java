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

import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.manager.api.beans.actions.ActionBean;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.PermissionType;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IApiValidator;
import io.apiman.manager.api.core.IClientValidator;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;
import io.apiman.manager.api.rest.IActionResource;
import io.apiman.manager.api.rest.IOrganizationResource;
import io.apiman.manager.api.rest.exceptions.*;
import io.apiman.manager.api.rest.impl.audit.AuditUtils;
import io.apiman.manager.api.rest.exceptions.i18n.Messages;
import io.apiman.manager.api.rest.exceptions.util.ExceptionFactory;
import io.apiman.manager.api.security.ISecurityContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

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
    @Inject IClientValidator clientValidator;

    @Inject ISecurityContext securityContext;

    @Inject @ApimanLogger(ActionResourceImpl.class) IApimanLogger log;

    /**
     * Constructor.
     */
    public ActionResourceImpl() {
    }

    /**
     * @see IActionResource#performAction(io.apiman.manager.api.beans.actions.ActionBean)
     */
    @Override
    public void performAction(ActionBean action) throws ActionException, NotAuthorizedException {
        switch (action.getType()) {
            case publishAPI:
                publishApi(action);
                return;
            case retireAPI:
                retireApi(action);
                return;
            case registerClient:
                registerClient(action);
                return;
            case unregisterClient:
                unregisterClient(action);
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
    private void publishApi(ActionBean action) throws ActionException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiAdmin, action.getOrganizationId());

        ApiVersionBean versionBean;
        try {
            versionBean = orgs.getApiVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (ApiVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ApiNotFound")); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - API must be Ready.
        if (!versionBean.isPublicAPI() && versionBean.getStatus() != ApiStatus.Ready) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidApiStatus")); //$NON-NLS-1$
        }
        if (versionBean.isPublicAPI()) {
            if (versionBean.getStatus() == ApiStatus.Retired || versionBean.getStatus() == ApiStatus.Created) {
                throw ExceptionFactory.actionException(Messages.i18n.format("InvalidApiStatus")); //$NON-NLS-1$
            }
            if (versionBean.getStatus() == ApiStatus.Published) {
                Date modOn = versionBean.getModifiedOn();
                Date publishedOn = versionBean.getPublishedOn();
                int c = modOn.compareTo(publishedOn);
                if (c <= 0) {
                    throw ExceptionFactory.actionException(Messages.i18n.format("ApiRePublishNotRequired")); //$NON-NLS-1$
                }
            }
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
        gatewayApi.setParsePayload(versionBean.isParsePayload());
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
    private void retireApi(ActionBean action) throws ActionException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.apiAdmin, action.getOrganizationId());

        ApiVersionBean versionBean;
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
     * Registers an client (along with all of its contracts) to the gateway.
     * @param action
     */
    private void registerClient(ActionBean action) throws ActionException, NotAuthorizedException {
       securityContext.checkPermissions(PermissionType.clientAdmin, action.getOrganizationId());

        ClientVersionBean versionBean;
        List<ContractSummaryBean> contractBeans;
        try {
            versionBean = orgs.getClientVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (ClientVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("clientVersionDoesNotExist", action.getEntityId(), action.getEntityVersion())); //$NON-NLS-1$
        }
        try {
            contractBeans = query.getClientContracts(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ClientNotFound"), e); //$NON-NLS-1$
        }

        boolean isReregister = false;

        // Validate that it's ok to perform this action
        if (versionBean.getStatus() == ClientStatus.Registered) {
            Date modOn = versionBean.getModifiedOn();
            Date publishedOn = versionBean.getPublishedOn();
            int c = modOn.compareTo(publishedOn);
            if (c <= 0) {
                throw ExceptionFactory.actionException(Messages.i18n.format("ClientReRegisterNotRequired")); //$NON-NLS-1$
            }
            isReregister = true;
        }

        Client client = new Client();
        client.setOrganizationId(versionBean.getClient().getOrganization().getId());
        client.setClientId(versionBean.getClient().getId());
        client.setVersion(versionBean.getVersion());
        client.setApiKey(versionBean.getApikey());

        Set<Contract> contracts = new HashSet<>();
        for (ContractSummaryBean contractBean : contractBeans) {
            Contract contract = new Contract();
            contract.setPlan(contractBean.getPlanId());
            contract.setApiId(contractBean.getApiId());
            contract.setApiOrgId(contractBean.getApiOrganizationId());
            contract.setApiVersion(contractBean.getApiVersion());
            contract.getPolicies().addAll(aggregateContractPolicies(contractBean));
            contracts.add(contract);
        }
        client.setContracts(contracts);

        // Next, register the client with *all* relevant gateways.  This is done by
        // looking up all referenced APIs and getting the gateway information for them.
        // Each of those gateways must be told about the client.
        try {
            storage.beginTx();
            Map<String, IGatewayLink> links = new HashMap<>();
            for (Contract contract : client.getContracts()) {
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
            if (isReregister) {
                // Once we figure out which gateways to register with, make sure we also "unregister"
                // the client app from all other gateways.  This is necessary because we may have broken
                // contracts we previously had on APIs that are published to other gateways.  And thus
                // it's possible we need to remove a contract from a Gateway that is not otherwise/currently
                // referenced.
                //
                // This is a fix for:  https://issues.jboss.org/browse/APIMAN-895

                Iterator<GatewayBean> gateways = storage.getAllGateways();
                while (gateways.hasNext()) {
                    GatewayBean gbean = gateways.next();
                    if (!links.containsKey(gbean.getId())) {
                        IGatewayLink gatewayLink = createGatewayLink(gbean.getId());
                        
                        try {
                            gatewayLink.unregisterClient(client);
                        } catch (Exception e) {
                            // We need to catch the error, but ignore it,
                            // in the event that the gateway is invalid.
                        }
                        
                        gatewayLink.close();
                    }
                }
            }
            for (IGatewayLink gatewayLink : links.values()) {
                gatewayLink.registerClient(client);
                gatewayLink.close();
            }
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("RegisterError"), e); //$NON-NLS-1$
        }

        versionBean.setStatus(ClientStatus.Registered);
        versionBean.setPublishedOn(new Date());

        try {
            storage.beginTx();
            storage.updateClientVersion(versionBean);
            storage.createAuditEntry(AuditUtils.clientRegistered(versionBean, securityContext));
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("RegisterError"), e); //$NON-NLS-1$
        }

        log.debug(String.format("Successfully registered Client %s on specified gateways: %s", //$NON-NLS-1$
                versionBean.getClient().getName(), versionBean.getClient()));
    }

    /**
     * Aggregates the API, client, and plan policies into a single ordered list.
     * @param contractBean
     */
    private List<Policy> aggregateContractPolicies(ContractSummaryBean contractBean) {
        try {
            List<Policy> policies = new ArrayList<>();
            PolicyType [] types = new PolicyType[] {
                    PolicyType.Client, PolicyType.Plan, PolicyType.Api
            };
            for (PolicyType policyType : types) {
                String org, id, ver;
                switch (policyType) {
                  case Client: {
                      org = contractBean.getClientOrganizationId();
                      id = contractBean.getClientId();
                      ver = contractBean.getClientVersion();
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
                List<PolicySummaryBean> clientPolicies = query.getPolicies(org, id, ver, policyType);
                try {
                    storage.beginTx();
                    for (PolicySummaryBean policySummaryBean : clientPolicies) {
                        PolicyBean policyBean = storage.getPolicy(policyType, org, id, ver, policySummaryBean.getId());
                        Policy policy = new Policy();
                        policy.setPolicyJsonConfig(policyBean.getConfiguration());
                        policy.setPolicyImpl(policyBean.getDefinition().getPolicyImpl());
                        policies.add(policy);
                    }
                } finally {
                    storage.rollbackTx();
                }
            }
            return policies;
        } catch (Exception e) {
            throw ExceptionFactory.actionException(
                    Messages.i18n.format("ErrorAggregatingPolicies", contractBean.getClientId() + "->" + contractBean.getApiDescription()), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * De-registers an client that is currently registered with the gateway.
     * @param action
     */
    private void unregisterClient(ActionBean action) throws ActionException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.clientAdmin, action.getOrganizationId());

        ClientVersionBean versionBean;
        List<ContractSummaryBean> contractBeans;
        try {
            versionBean = orgs.getClientVersion(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (ClientVersionNotFoundException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ClientNotFound")); //$NON-NLS-1$
        }
        try {
            contractBeans = query.getClientContracts(action.getOrganizationId(), action.getEntityId(), action.getEntityVersion());
        } catch (StorageException e) {
            throw ExceptionFactory.actionException(Messages.i18n.format("ClientNotFound"), e); //$NON-NLS-1$
        }

        // Validate that it's ok to perform this action - client must be Ready.
        if (versionBean.getStatus() != ClientStatus.Registered) {
            throw ExceptionFactory.actionException(Messages.i18n.format("InvalidClientStatus")); //$NON-NLS-1$
        }

        Client client = new Client();
        client.setOrganizationId(versionBean.getClient().getOrganization().getId());
        client.setClientId(versionBean.getClient().getId());
        client.setVersion(versionBean.getVersion());

        // Next, unregister the client from *all* relevant gateways.  This is done by
        // looking up all referenced APIs and getting the gateway information for them.
        // Each of those gateways must be told about the client.
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
                gatewayLink.unregisterClient(client);
                gatewayLink.close();
            }
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("UnregisterError"), e); //$NON-NLS-1$
        }

        versionBean.setStatus(ClientStatus.Retired);
        versionBean.setRetiredOn(new Date());

        try {
            storage.beginTx();
            storage.updateClientVersion(versionBean);
            storage.createAuditEntry(AuditUtils.clientUnregistered(versionBean, securityContext));
            storage.commitTx();
        } catch (Exception e) {
            storage.rollbackTx();
            throw ExceptionFactory.actionException(Messages.i18n.format("UnregisterError"), e); //$NON-NLS-1$
        }

        log.debug(String.format("Successfully registered Client %s on specified gateways: %s", //$NON-NLS-1$
                versionBean.getClient().getName(), versionBean.getClient()));
    }

    /**
     * Locks the plan.
     * @param action
     */
    private void lockPlan(ActionBean action) throws ActionException, NotAuthorizedException {
        securityContext.checkPermissions(PermissionType.planAdmin, action.getOrganizationId());

        PlanVersionBean versionBean;
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
     * @return the clientValidator
     */
    public IClientValidator getClientValidator() {
        return clientValidator;
    }

    /**
     * @param clientValidator the clientValidator to set
     */
    public void setClientValidator(IClientValidator clientValidator) {
        this.clientValidator = clientValidator;
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
