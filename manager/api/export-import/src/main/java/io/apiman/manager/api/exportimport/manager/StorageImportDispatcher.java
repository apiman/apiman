/*
 * Copyright 2015 JBoss Inc
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

package io.apiman.manager.api.exportimport.manager;

import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationStatus;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.RoleBean;
import io.apiman.manager.api.beans.idm.RoleMembershipBean;
import io.apiman.manager.api.beans.idm.UserBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.exportimport.beans.MetadataBean;
import io.apiman.manager.api.exportimport.i18n.Messages;
import io.apiman.manager.api.exportimport.read.IImportReaderDispatcher;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.IGatewayLinkFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

/**
 * Used to store imported entities into the {@link IStorage}.
 *
 * @author eric.wittmann@redhat.com
 */
@RequestScoped
public class StorageImportDispatcher implements IImportReaderDispatcher {

    @Inject
    private IStorage storage;
    @Inject @ApimanLogger(StorageImportDispatcher.class)
    private IApimanLogger logger;
    @Inject
    private IGatewayLinkFactory gatewayLinks;

    private Map<String, PolicyDefinitionBean> policyDefIndex = new HashMap<>();

    private OrganizationBean currentOrg;
    private PlanBean currentPlan;
    private ApiBean currentApi;
    private ApplicationBean currentApp;
    private ApplicationVersionBean currentAppVersion;

    private List<ContractBean> contracts = new LinkedList<>();

    private Set<EntityInfo> apisToPublish = new HashSet<>();
    private Set<EntityInfo> appsToRegister = new HashSet<>();

    private Map<String, IGatewayLink> gatewayLinkCache = new HashMap<>();

    /**
     * Constructor.
     * @param storage
     */
    public StorageImportDispatcher() {
    }

    /**
     * @param logger
     */
    public void setLogger(IApimanLogger logger) {
        this.logger = logger;
    }

    /**
     * Starts the import.
     */
    public void start() {
        logger.info("----------------------------"); //$NON-NLS-1$
        logger.info(Messages.i18n.format("StorageImportDispatcher.StartingImport")); //$NON-NLS-1$

        policyDefIndex.clear();
        currentOrg = null;
        currentPlan = null;
        currentApi = null;
        currentApp = null;
        currentAppVersion = null;
        contracts.clear();
        apisToPublish.clear();
        appsToRegister.clear();
        gatewayLinkCache.clear();

        try {
            this.storage.beginTx();
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#metadata(io.apiman.manager.api.exportimport.beans.MetadataBean)
     */
    @Override
    public void metadata(MetadataBean metadata) {
        // Nothing to do here at the moment.
        // TODO: at some point we need to compare the version in metadata against the current version of apiman
        logger.info(Messages.i18n.format("StorageImportDispatcher.FromVersion") + metadata.getApimanVersion()); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#user(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void user(UserBean user) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingUser") + user.getUsername()); //$NON-NLS-1$
            UserBean userBean = storage.getUser(user.getUsername());
            if (userBean == null) {
                storage.createUser(user);
            } else {
                storage.updateUser(user);
            }
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#role(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void role(RoleBean role) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingRole") + role.getName()); //$NON-NLS-1$
            storage.createRole(role);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#plugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void plugin(PluginBean plugin) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingPlugin") + plugin.getGroupId() + '/' + plugin.getArtifactId() + '/' + plugin.getVersion()); //$NON-NLS-1$
            plugin.setId(null);
            storage.createPlugin(plugin);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#gateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void gateway(GatewayBean gateway) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingGateway") + gateway.getName()); //$NON-NLS-1$
            storage.createGateway(gateway);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#policyDef(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void policyDef(PolicyDefinitionBean policyDef) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingPolicyDef") + policyDef.getName()); //$NON-NLS-1$
            storage.createPolicyDefinition(policyDef);
            policyDefIndex.put(policyDef.getId(), policyDef);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#organization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void organization(OrganizationBean org) {
        currentOrg = org;
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingOrg") + org.getName()); //$NON-NLS-1$
            storage.createOrganization(org);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#membership(io.apiman.manager.api.beans.idm.RoleMembershipBean)
     */
    @Override
    public void membership(RoleMembershipBean membership) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingMembership") + membership.getUserId() + '+' + membership.getRoleId() + "=>" + membership.getOrganizationId()); //$NON-NLS-1$ //$NON-NLS-2$
            membership.setId(null);
            storage.createMembership(membership);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#plan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void plan(PlanBean plan) {
        currentPlan = plan;
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingPlan") + plan.getName()); //$NON-NLS-1$
            plan.setOrganization(currentOrg);
            storage.createPlan(plan);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#planVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void planVersion(PlanVersionBean planVersion) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingPlanVersion") + planVersion.getVersion()); //$NON-NLS-1$
            planVersion.setPlan(currentPlan);
            planVersion.setId(null);
            storage.createPlanVersion(planVersion);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#planPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void planPolicy(PolicyBean policy) {
        logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingPlanPolicy") + policy.getName()); //$NON-NLS-1$
        policy(policy);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#api(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    public void api(ApiBean api) {
        currentApi = api;
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingApi") + api.getName()); //$NON-NLS-1$
            api.setOrganization(currentOrg);
            storage.createApi(api);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#apiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void apiVersion(ApiVersionBean apiVersion) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingApiVersion") + apiVersion.getVersion()); //$NON-NLS-1$
            apiVersion.setApi(currentApi);
            apiVersion.setId(null);
            storage.createApiVersion(apiVersion);

            if (apiVersion.getStatus() == ApiStatus.Published) {
                apisToPublish.add(new EntityInfo(
                        apiVersion.getApi().getOrganization().getId(),
                        apiVersion.getApi().getId(),
                        apiVersion.getVersion()
                ));
            }
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#apiPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void apiPolicy(PolicyBean policy) {
        logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingApiPolicy") + policy.getName()); //$NON-NLS-1$
        policy(policy);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#application(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void application(ApplicationBean application) {
        currentApp = application;
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingApp") + application.getName()); //$NON-NLS-1$
            application.setOrganization(currentOrg);
            storage.createApplication(application);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#applicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void applicationVersion(ApplicationVersionBean appVersion) {
        currentAppVersion = appVersion;
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingAppVersion") + appVersion.getVersion()); //$NON-NLS-1$
            appVersion.setApplication(currentApp);
            appVersion.setId(null);
            storage.createApplicationVersion(appVersion);

            if (appVersion.getStatus() == ApplicationStatus.Registered) {
                appsToRegister.add(new EntityInfo(
                        appVersion.getApplication().getOrganization().getId(),
                        appVersion.getApplication().getId(),
                        appVersion.getVersion()
                ));
            }
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#applicationPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void applicationPolicy(PolicyBean policy) {
        logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingAppPolicy") + policy.getName()); //$NON-NLS-1$
        policy(policy);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#applicationContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void applicationContract(ContractBean contract) {
        ApplicationVersionBean avb = new ApplicationVersionBean();
        avb.setApplication(new ApplicationBean());
        avb.getApplication().setOrganization(new OrganizationBean());
        avb.getApplication().setId(currentApp.getId());
        avb.getApplication().getOrganization().setId(currentOrg.getId());
        avb.setVersion(currentAppVersion.getVersion());
        contract.setApplication(avb);
        contracts.add(contract);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#audit(io.apiman.manager.api.beans.audit.AuditEntryBean)
     */
    @Override
    public void audit(AuditEntryBean entry) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingAuditEntry") + entry.getId()); //$NON-NLS-1$
            entry.setId(null);
            storage.createAuditEntry(entry);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#close()
     */
    @Override
    public void close() {
        try {
            importContracts();
            publishApis();
            registerApplications();

            // Close the gateway links that we created during the publish/registration
            for (IGatewayLink gwLink : gatewayLinkCache.values()) {
                try { gwLink.close(); } catch (Exception e) { }
            }

            storage.commitTx();
            logger.info("-----------------------------------"); //$NON-NLS-1$
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingImportComplete")); //$NON-NLS-1$
            logger.info("-----------------------------------"); //$NON-NLS-1$
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#cancel()
     */
    @Override
    public void cancel() {
        this.storage.rollbackTx();
    }

    /**
     * Publishes any apis that were imported in the "Published" state.
     * @throws StorageException
     */
    private void publishApis() throws StorageException {
        logger.info(Messages.i18n.format("StorageExporter.PublishingApis")); //$NON-NLS-1$

        try {
            for (EntityInfo info : apisToPublish) {
                logger.info(Messages.i18n.format("StorageExporter.PublishingApi", info)); //$NON-NLS-1$
                ApiVersionBean versionBean = storage.getApiVersion(info.organizationId, info.id, info.version);

                Api gatewayApi = new Api();
                gatewayApi.setEndpoint(versionBean.getEndpoint());
                gatewayApi.setEndpointType(versionBean.getEndpointType().toString());
                gatewayApi.setEndpointProperties(versionBean.getEndpointProperties());
                gatewayApi.setOrganizationId(versionBean.getApi().getOrganization().getId());
                gatewayApi.setApiId(versionBean.getApi().getId());
                gatewayApi.setVersion(versionBean.getVersion());
                gatewayApi.setPublicAPI(versionBean.isPublicAPI());
                if (versionBean.isPublicAPI()) {
                    List<Policy> policiesToPublish = new ArrayList<>();
                    Iterator<PolicyBean> apiPolicies = storage.getAllPolicies(info.organizationId,
                            info.id, info.version, PolicyType.Api);
                    while (apiPolicies.hasNext()) {
                        PolicyBean apiPolicy = apiPolicies.next();
                        Policy policyToPublish = new Policy();
                        policyToPublish.setPolicyJsonConfig(apiPolicy.getConfiguration());
                        policyToPublish.setPolicyImpl(apiPolicy.getDefinition().getPolicyImpl());
                        policiesToPublish.add(policyToPublish);
                    }
                    gatewayApi.setApiPolicies(policiesToPublish);
                }

                // Publish the api to all relevant gateways
                Set<ApiGatewayBean> gateways = versionBean.getGateways();
                if (gateways == null) {
                    throw new RuntimeException("No gateways specified for api!"); //$NON-NLS-1$
                }
                for (ApiGatewayBean apiGatewayBean : gateways) {
                    IGatewayLink gatewayLink = createGatewayLink(apiGatewayBean.getGatewayId());
                    gatewayLink.publishApi(gatewayApi);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers any applications that were imported in the "Registered" state.
     * @throws StorageException
     */
    private void registerApplications() throws StorageException {
        logger.info(Messages.i18n.format("StorageExporter.RegisteringApps")); //$NON-NLS-1$

        for (EntityInfo info : appsToRegister) {
            logger.info(Messages.i18n.format("StorageExporter.RegisteringApp", info)); //$NON-NLS-1$
            ApplicationVersionBean versionBean = storage.getApplicationVersion(info.organizationId, info.id, info.version);
            Iterator<ContractBean> contractBeans = storage.getAllContracts(info.organizationId, info.id, info.version);

            Application application = new Application();
            application.setOrganizationId(versionBean.getApplication().getOrganization().getId());
            application.setApplicationId(versionBean.getApplication().getId());
            application.setVersion(versionBean.getVersion());

            Set<Contract> contracts = new HashSet<>();
            while (contractBeans.hasNext()) {
                ContractBean contractBean = contractBeans.next();
                EntityInfo apiInfo = new EntityInfo(
                        contractBean.getApi().getApi().getOrganization().getId(),
                        contractBean.getApi().getApi().getId(),
                        contractBean.getApi().getVersion());
                if (apisToPublish.contains(apiInfo)) {
                    Contract contract = new Contract();
                    contract.setApiKey(contractBean.getApikey());
                    contract.setPlan(contractBean.getPlan().getPlan().getId());
                    contract.setApiId(contractBean.getApi().getApi().getId());
                    contract.setApiOrgId(contractBean.getApi().getApi().getOrganization().getId());
                    contract.setApiVersion(contractBean.getApi().getVersion());
                    contract.getPolicies().addAll(aggregateContractPolicies(contractBean, info));
                    contracts.add(contract);
                }
            }
            application.setContracts(contracts);

            // Next, register the application with *all* relevant gateways.  This is done by
            // looking up all referenced apis and getting the gateway information for them.
            // Each of those gateways must be told about the application.
            Map<String, IGatewayLink> links = new HashMap<>();
            for (Contract contract : application.getContracts()) {
                ApiVersionBean svb = storage.getApiVersion(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
                Set<ApiGatewayBean> gateways = svb.getGateways();
                if (gateways == null) {
                    throw new PublishingException("No gateways specified for api: " + svb.getApi().getName()); //$NON-NLS-1$
                }
                for (ApiGatewayBean apiGatewayBean : gateways) {
                    String gatewayId = apiGatewayBean.getGatewayId();
                    if (!links.containsKey(gatewayId)) {
                        IGatewayLink gatewayLink = createGatewayLink(gatewayId);
                        links.put(gatewayId, gatewayLink);
                    }
                }
            }
            for (IGatewayLink gatewayLink : links.values()) {
                try {
                    gatewayLink.registerApplication(application);
                } catch (Exception e) {
                    throw new StorageException(e);
                }
            }
        }
    }

    /**
     * Aggregates the api, app, and plan policies into a single ordered list.
     * @param contractBean
     * @param appInfo
     */
    private List<Policy> aggregateContractPolicies(ContractBean contractBean, EntityInfo appInfo) throws StorageException {
        List<Policy> policies = new ArrayList<>();
        PolicyType [] types = new PolicyType[] {
                PolicyType.Application, PolicyType.Plan, PolicyType.Api
        };
        for (PolicyType policyType : types) {
            String org, id, ver;
            switch (policyType) {
              case Application: {
                  org = appInfo.organizationId;
                  id = appInfo.id;
                  ver = appInfo.version;
                  break;
              }
              case Plan: {
                  org = contractBean.getApi().getApi().getOrganization().getId();
                  id = contractBean.getPlan().getPlan().getId();
                  ver = contractBean.getPlan().getVersion();
                  break;
              }
              case Api: {
                  org = contractBean.getApi().getApi().getOrganization().getId();
                  id = contractBean.getApi().getApi().getId();
                  ver = contractBean.getApi().getVersion();
                  break;
              }
              default: {
                  throw new RuntimeException("Missing case for switch!"); //$NON-NLS-1$
              }
            }

            Iterator<PolicyBean> appPolicies = storage.getAllPolicies(org, id, ver, policyType);
            while (appPolicies.hasNext()) {
                PolicyBean policyBean = appPolicies.next();
                Policy policy = new Policy();
                policy.setPolicyJsonConfig(policyBean.getConfiguration());
                policy.setPolicyImpl(policyBean.getDefinition().getPolicyImpl());
                policies.add(policy);
            }
        }
        return policies;
    }

    /**
     * Imports the (deferred) contracts.
     * @throws StorageException
     */
    private void importContracts() throws StorageException {
        for (ContractBean contract : contracts) {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingAppContract") + contract.getApikey()); //$NON-NLS-1$
            String appId = contract.getApplication().getApplication().getId();
            String appOrganizationId = contract.getApplication().getApplication().getOrganization().getId();
            String appVersion = contract.getApplication().getVersion();
            String apiId = contract.getApi().getApi().getId();
            String apiOrganizationId = contract.getApi().getApi().getOrganization().getId();
            String apiVersion = contract.getApi().getVersion();
            String planId = contract.getPlan().getPlan().getId();
            String planVersion = contract.getPlan().getVersion();

            contract.setApi(lookupApi(apiOrganizationId, apiId, apiVersion));
            contract.setPlan(lookupPlan(apiOrganizationId, planId, planVersion));
            contract.setApplication(lookupApplication(appOrganizationId, appId, appVersion));
            contract.setId(null);

            storage.createContract(contract);
        }
    }

    /**
     * @param apiOrganizationId
     * @param apiId
     * @param apiVersion
     * @throws StorageException
     */
    private ApiVersionBean lookupApi(String apiOrganizationId, String apiId,
            String apiVersion) throws StorageException {
        return storage.getApiVersion(apiOrganizationId, apiId, apiVersion);
    }

    /**
     * @param planOrganizationId
     * @param planId
     * @param planVersion
     * @throws StorageException
     */
    private PlanVersionBean lookupPlan(String planOrganizationId, String planId, String planVersion) throws StorageException {
        return storage.getPlanVersion(planOrganizationId, planId, planVersion);
    }

    /**
     * @param applicationOrganizationId
     * @param applicationId
     * @param applicationVersion
     * @throws StorageException
     */
    private ApplicationVersionBean lookupApplication(String applicationOrganizationId, String applicationId, String applicationVersion) throws StorageException {
        return storage.getApplicationVersion(applicationOrganizationId, applicationId, applicationVersion);
    }

    /**
     * @param policy
     */
    private void policy(PolicyBean policy) {
        try {
            policy.setId(null);
            policy.setDefinition(policyDefIndex.get(policy.getDefinition().getId()));
            storage.createPolicy(policy);
        } catch (StorageException e) {
            error(e);
        }
    }

    /**
     * Creates a gateway link given a gateway id.
     * @param gatewayId
     */
    private IGatewayLink createGatewayLink(String gatewayId) throws StorageException {
        if (gatewayLinkCache.containsKey(gatewayId)) {
            return gatewayLinkCache.get(gatewayId);
        }
        try {
            GatewayBean gateway = storage.getGateway(gatewayId);
            if (gateway == null) {
                throw new Exception("Gateway not found: " + gatewayId); //$NON-NLS-1$
            }
            IGatewayLink link = gatewayLinks.create(gateway);
            gatewayLinkCache.put(gatewayId, link);
            return link;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * @param error
     */
    private void error(StorageException error) {
        logger.error(error);
        throw new RuntimeException(error);
    }

    private static class EntityInfo {
        private String organizationId;
        private String id;
        private String version;

        /**
         * Constructor.
         * @param orgId
         * @param id
         * @param version
         */
        public EntityInfo(String orgId, String id, String version) {
            this.organizationId = orgId;
            this.id = id;
            this.version = version;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @SuppressWarnings("nls")
        @Override
        public String toString() {
            return organizationId + " / " + id + " -> " + version;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((organizationId == null) ? 0 : organizationId.hashCode());
            result = prime * result + ((version == null) ? 0 : version.hashCode());
            return result;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            EntityInfo other = (EntityInfo) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (organizationId == null) {
                if (other.organizationId != null)
                    return false;
            } else if (!organizationId.equals(other.organizationId))
                return false;
            if (version == null) {
                if (other.version != null)
                    return false;
            } else if (!version.equals(other.version))
                return false;
            return true;
        }


    }
}
