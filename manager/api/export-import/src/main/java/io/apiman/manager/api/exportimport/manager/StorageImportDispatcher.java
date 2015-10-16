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

import io.apiman.manager.api.beans.apps.ApplicationBean;
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
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.IApimanLogger;
import io.apiman.manager.api.exportimport.beans.MetadataBean;
import io.apiman.manager.api.exportimport.i18n.Messages;
import io.apiman.manager.api.exportimport.read.IImportReaderDispatcher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Used to store imported entities into the {@link IStorage}.
 *
 * @author eric.wittmann@redhat.com
 */
public class StorageImportDispatcher implements IImportReaderDispatcher {

    private IStorage storage;
    private IApimanLogger logger;
    
    private Map<String, PolicyDefinitionBean> policyDefIndex = new HashMap<>();

    private OrganizationBean currentOrg;
    private PlanBean currentPlan;
    private ServiceBean currentService;
    private ApplicationBean currentApp;
    private ApplicationVersionBean currentAppVersion;
    
    private List<ContractBean> contracts = new LinkedList<>();
    
    /**
     * Constructor.
     * @param storage
     */
    public StorageImportDispatcher(IStorage storage, IApimanLogger logger) {
        this.storage = storage;
        this.logger = logger;
        
        logger.info("----------------------------"); //$NON-NLS-1$
        logger.info(Messages.i18n.format("StorageImportDispatcher.StartingImport")); //$NON-NLS-1$
        
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
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingUser") + user); //$NON-NLS-1$
            storage.createUser(user);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#role(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void role(RoleBean role) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingRole") + role); //$NON-NLS-1$
            storage.createRole(role);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#plugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void plugin(PluginBean plugin) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingPlugin") + plugin); //$NON-NLS-1$
            plugin.setId(null);
            storage.createPlugin(plugin);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#gateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void gateway(GatewayBean gateway) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingGateway") + gateway); //$NON-NLS-1$
            storage.createGateway(gateway);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#policyDef(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void policyDef(PolicyDefinitionBean policyDef) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingPolicyDef") + policyDef); //$NON-NLS-1$
            storage.createPolicyDefinition(policyDef);
            policyDefIndex.put(policyDef.getId(), policyDef);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#organization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void organization(OrganizationBean org) {
        currentOrg = org;
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingOrg") + org); //$NON-NLS-1$
            storage.createOrganization(org);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#membership(io.apiman.manager.api.beans.idm.RoleMembershipBean)
     */
    @Override
    public void membership(RoleMembershipBean membership) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingMembership") + membership); //$NON-NLS-1$
            membership.setId(null);
            storage.createMembership(membership);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#plan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void plan(PlanBean plan) {
        currentPlan = plan;
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingPlan") + plan); //$NON-NLS-1$
            plan.setOrganization(currentOrg);
            storage.createPlan(plan);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#planVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void planVersion(PlanVersionBean planVersion) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingPlanVersion") + planVersion); //$NON-NLS-1$
            planVersion.setPlan(currentPlan);
            planVersion.setId(null);
            storage.createPlanVersion(planVersion);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#planPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void planPolicy(PolicyBean policy) {
        logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingPlanPolicy") + policy); //$NON-NLS-1$
        policy(policy);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#service(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void service(ServiceBean service) {
        currentService = service;
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingService") + service); //$NON-NLS-1$
            service.setOrganization(currentOrg);
            storage.createService(service);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#serviceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void serviceVersion(ServiceVersionBean serviceVersion) {
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingServiceVersion") + serviceVersion); //$NON-NLS-1$
            serviceVersion.setService(currentService);
            serviceVersion.setId(null);
            storage.createServiceVersion(serviceVersion);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#servicePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void servicePolicy(PolicyBean policy) {
        logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingServicePolicy") + policy); //$NON-NLS-1$
        policy(policy);
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#application(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void application(ApplicationBean application) {
        currentApp = application;
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingApp") + application); //$NON-NLS-1$
            application.setOrganization(currentOrg);
            storage.createApplication(application);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#applicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void applicationVersion(ApplicationVersionBean appVersion) {
        currentAppVersion = appVersion;
        try {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingAppVersion") + appVersion); //$NON-NLS-1$
            appVersion.setApplication(currentApp);
            appVersion.setId(null);
            storage.createApplicationVersion(appVersion);
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#applicationPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void applicationPolicy(PolicyBean policy) {
        logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingAppPolicy") + policy); //$NON-NLS-1$
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
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingAuditEntry") + entry); //$NON-NLS-1$
            entry.setId(null);
            storage.createAuditEntry(entry);
        } catch (StorageException e) {
            rollback(e);
        }
    }
    
    /**
     * @see io.apiman.manager.api.exportimport.read.IImportReaderDispatcher#close()
     */
    @Override
    public void close() {
        try {
            importContracts();
//            publishServices();
//            registerApplications();
            storage.commitTx();
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingImportComplete")); //$NON-NLS-1$
            logger.info("-----------------------------------"); //$NON-NLS-1$
        } catch (StorageException e) {
            rollback(e);
        }
    }

    /**
     * Imports the (deferred) contracts.
     * @throws StorageException 
     */
    private void importContracts() throws StorageException {
        for (ContractBean contract : contracts) {
            logger.info(Messages.i18n.format("StorageImportDispatcher.ImportingAppContract") + contract); //$NON-NLS-1$
            String appId = contract.getApplication().getApplication().getId();
            String appOrganizationId = contract.getApplication().getApplication().getOrganization().getId();
            String appVersion = contract.getApplication().getVersion();
            String serviceId = contract.getService().getService().getId();
            String serviceOrganizationId = contract.getService().getService().getOrganization().getId();
            String serviceVersion = contract.getService().getVersion();
            String planId = contract.getPlan().getPlan().getId();
            String planVersion = contract.getPlan().getVersion();
            
            contract.setService(lookupService(serviceOrganizationId, serviceId, serviceVersion));
            contract.setPlan(lookupPlan(serviceOrganizationId, planId, planVersion));
            contract.setApplication(lookupApplication(appOrganizationId, appId, appVersion));
            contract.setId(null);
            
            storage.createContract(contract);
        }
    }

    /**
     * @param serviceOrganizationId
     * @param serviceId
     * @param serviceVersion
     */
    private ServiceVersionBean lookupService(String serviceOrganizationId, String serviceId,
            String serviceVersion) {
        try {
            return storage.getServiceVersion(serviceOrganizationId, serviceId, serviceVersion);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param planOrganizationId
     * @param planId
     * @param planVersion
     */
    private PlanVersionBean lookupPlan(String planOrganizationId, String planId, String planVersion) {
        try {
            return storage.getPlanVersion(planOrganizationId, planId, planVersion);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param applicationOrganizationId
     * @param applicationId
     * @param applicationVersion
     */
    private ApplicationVersionBean lookupApplication(String applicationOrganizationId, String applicationId, String applicationVersion) {
        try {
            return storage.getApplicationVersion(applicationOrganizationId, applicationId, applicationVersion);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
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
            rollback(e);
        }
    }

    /**
     * @param error
     */
    private void rollback(StorageException error) {
        this.storage.rollbackTx();
        throw new RuntimeException(error);
    }
    
}
