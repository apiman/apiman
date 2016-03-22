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
package io.apiman.manager.api.jpa;

import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiDefinitionBean;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiPlanBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntityType;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.gateways.GatewayType;
import io.apiman.manager.api.beans.idm.PermissionBean;
import io.apiman.manager.api.beans.idm.PermissionType;
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
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.summary.ApiEntryBean;
import io.apiman.manager.api.beans.summary.ApiPlanSummaryBean;
import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.api.beans.summary.ApiSummaryBean;
import io.apiman.manager.api.beans.summary.ApiVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ClientSummaryBean;
import io.apiman.manager.api.beans.summary.ClientVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PluginSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyFormType;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.util.PolicyTemplateUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JPA implementation of the storage interface.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class JpaStorage extends AbstractJpaStorage implements IStorage, IStorageQuery {

    private static Logger logger = LoggerFactory.getLogger(JpaStorage.class);

    @Inject IDataEncrypter encrypter;
    @PostConstruct
    public void postConstruct() {
        // Kick the encrypter, causing it to be loaded/resolved in CDI
        encrypter.encrypt(""); //$NON-NLS-1$
    }

    /**
     * Constructor.
     */
    public JpaStorage() {
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#beginTx()
     */
    @Override
    public void beginTx() throws StorageException {
        super.beginTx();
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#commitTx()
     */
    @Override
    public void commitTx() throws StorageException {
        super.commitTx();
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#rollbackTx()
     */
    @Override
    public void rollbackTx() {
        super.rollbackTx();
    }

    @Override
    public void initialize() {
        // No-Op for JPA
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createClient(io.apiman.manager.api.beans.clients.ClientBean)
     */
    @Override
    public void createClient(ClientBean client) throws StorageException {
        super.create(client);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createClientVersion(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public void createClientVersion(ClientVersionBean version) throws StorageException {
        super.create(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void createContract(ContractBean contract) throws StorageException {
        List<ContractSummaryBean> contracts = getClientContractsInternal(contract.getClient().getClient().getOrganization().getId(),
                contract.getClient().getClient().getId(), contract.getClient().getVersion());
        for (ContractSummaryBean csb : contracts) {
            if (csb.getApiOrganizationId().equals(contract.getApi().getApi().getOrganization().getId()) &&
                    csb.getApiId().equals(contract.getApi().getApi().getId()) &&
                    csb.getApiVersion().equals(contract.getApi().getVersion()))
                {
                    throw new StorageException("Error creating contract: duplicate contract detected."); //$NON-NLS-1$
                }
        }
        super.create(contract);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void createGateway(GatewayBean gateway) throws StorageException {
        super.create(gateway);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createDownload(io.apiman.manager.api.beans.download.DownloadBean)
     */
    @Override
    public void createDownload(DownloadBean download) throws StorageException {
        super.create(download);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void createPlugin(PluginBean plugin) throws StorageException {
        super.create(plugin);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void createOrganization(OrganizationBean organization) throws StorageException {
        super.create(organization);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void createPlan(PlanBean plan) throws StorageException {
        super.create(plan);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void createPlanVersion(PlanVersionBean version) throws StorageException {
        super.create(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void createPolicy(PolicyBean policy) throws StorageException {
        super.create(policy);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void createPolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        super.create(policyDef);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApi(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    public void createApi(ApiBean api) throws StorageException {
        super.create(api);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void createApiVersion(ApiVersionBean version) throws StorageException {
        super.create(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateClient(io.apiman.manager.api.beans.clients.ClientBean)
     */
    @Override
    public void updateClient(ClientBean client) throws StorageException {
        super.update(client);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateClientVersion(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public void updateClientVersion(ClientVersionBean version) throws StorageException {
        super.update(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void updateGateway(GatewayBean gateway) throws StorageException {
        super.update(gateway);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void updateOrganization(OrganizationBean organization) throws StorageException {
        super.update(organization);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void updatePlan(PlanBean plan) throws StorageException {
        super.update(plan);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void updatePlanVersion(PlanVersionBean version) throws StorageException {
        super.update(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void updatePolicy(PolicyBean policy) throws StorageException {
        super.update(policy);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void updatePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        super.update(policyDef);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void updatePlugin(PluginBean pluginBean) throws StorageException {
        super.update(pluginBean);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApi(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    public void updateApi(ApiBean api) throws StorageException {
        super.update(api);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void updateApiVersion(ApiVersionBean version) throws StorageException {
        super.update(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApiDefinition(io.apiman.manager.api.beans.apis.ApiVersionBean, java.io.InputStream)
     */
    @Override
    public void updateApiDefinition(ApiVersionBean version, InputStream definitionStream)
            throws StorageException {
        try {
            ApiDefinitionBean bean = super.get(version.getId(), ApiDefinitionBean.class);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(definitionStream, baos);
            byte [] data = baos.toByteArray();
            if (bean != null) {
                bean.setData(data);
                super.update(bean);
            } else {
                bean = new ApiDefinitionBean();
                bean.setId(version.getId());
                bean.setData(data);
                bean.setApiVersion(version);
                super.create(bean);
            }
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void deleteOrganization(OrganizationBean organization) throws StorageException {
        super.delete(organization);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteClient(io.apiman.manager.api.beans.clients.ClientBean)
     */
    @Override
    public void deleteClient(ClientBean client) throws StorageException {
        super.delete(client);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteClientVersion(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public void deleteClientVersion(ClientVersionBean version) throws StorageException {
        super.delete(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void deleteContract(ContractBean contract) throws StorageException {
        super.delete(contract);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApi(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    public void deleteApi(ApiBean api) throws StorageException {
        super.delete(api);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void deleteApiVersion(ApiVersionBean version) throws StorageException {
        deleteApiDefinition(version);
        super.delete(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApiDefinition(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void deleteApiDefinition(ApiVersionBean version) throws StorageException {
        ApiDefinitionBean bean = super.get(version.getId(), ApiDefinitionBean.class);
        if (bean != null) {
            super.delete(bean);
        } else {
            throw new StorageException("No definition found."); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void deletePlan(PlanBean plan) throws StorageException {
        super.delete(plan);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void deletePlanVersion(PlanVersionBean version) throws StorageException {
        super.delete(version);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void deletePolicy(PolicyBean policy) throws StorageException {
        super.delete(policy);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void deleteGateway(GatewayBean gateway) throws StorageException {
        super.delete(gateway);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteDownload(io.apiman.manager.api.beans.download.DownloadBean)
     */
    @Override
    public void deleteDownload(DownloadBean download) throws StorageException {
        super.delete(download);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void deletePlugin(PluginBean plugin) throws StorageException {
        super.delete(plugin);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        super.delete(policyDef);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getOrganization(java.lang.String)
     */
    @Override
    public OrganizationBean getOrganization(String id) throws StorageException {
        return super.get(id, OrganizationBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getClient(java.lang.String, java.lang.String)
     */
    @Override
    public ClientBean getClient(String organizationId, String id) throws StorageException {
        return super.get(organizationId, id, ClientBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getContract(java.lang.Long)
     */
    @Override
    public ContractBean getContract(Long id) throws StorageException {
        return super.get(id, ContractBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApi(java.lang.String, java.lang.String)
     */
    @Override
    public ApiBean getApi(String organizationId, String id) throws StorageException {
        return super.get(organizationId, id, ApiBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlan(java.lang.String, java.lang.String)
     */
    @Override
    public PlanBean getPlan(String organizationId, String id) throws StorageException {
        return super.get(organizationId, id, PlanBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPolicy(io.apiman.manager.api.beans.policies.PolicyType, java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public PolicyBean getPolicy(PolicyType type, String organizationId, String entityId, String version,
            Long id) throws StorageException {
        PolicyBean policyBean = super.get(id, PolicyBean.class);
        if (policyBean == null || policyBean.getType() != type) {
            return null;
        }
        if (!policyBean.getOrganizationId().equals(organizationId)) {
            return null;
        }
        if (!policyBean.getEntityId().equals(entityId)) {
            return null;
        }
        if (!policyBean.getEntityVersion().equals(version)) {
            return null;
        }
        return policyBean;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getGateway(java.lang.String)
     */
    @Override
    public GatewayBean getGateway(String id) throws StorageException {
        return super.get(id, GatewayBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getDownload(java.lang.String)
     */
    @Override
    public DownloadBean getDownload(String id) throws StorageException {
        return super.get(id, DownloadBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlugin(long)
     */
    @Override
    public PluginBean getPlugin(long id) throws StorageException {
        return super.get(id, PluginBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlugin(java.lang.String, java.lang.String)
     */
    @Override
    public PluginBean getPlugin(String groupId, String artifactId) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();

            @SuppressWarnings("nls")
            String sql =
                    "SELECT p.id, p.artifact_id, p.group_id, p.version, p.classifier, p.type, p.name, p.description, p.created_by, p.created_on, p.deleted" +
                    "  FROM plugins p" +
                    " WHERE p.group_id = ? AND p.artifact_id = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, groupId);
            query.setParameter(2, artifactId);
            List<Object[]> rows = query.getResultList();
            if (!rows.isEmpty()) {
                Object[] row = rows.get(0);
                PluginBean plugin = new PluginBean();
                plugin.setId(((Number) row[0]).longValue());
                plugin.setArtifactId(String.valueOf(row[1]));
                plugin.setGroupId(String.valueOf(row[2]));
                plugin.setVersion(String.valueOf(row[3]));
                plugin.setClassifier((String) row[4]);
                plugin.setType((String) row[5]);
                plugin.setName(String.valueOf(row[6]));
                plugin.setDescription(String.valueOf(row[7]));
                plugin.setCreatedBy(String.valueOf(row[8]));
                plugin.setCreatedOn((Date) row[9]);
                plugin.setDeleted((Boolean) row[10]);
                return plugin;
            } else {
                return null;
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPolicyDefinition(java.lang.String)
     */
    @Override
    public PolicyDefinitionBean getPolicyDefinition(String id) throws StorageException {
        return super.get(id, PolicyDefinitionBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#reorderPolicies(io.apiman.manager.api.beans.policies.PolicyType, java.lang.String, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public void reorderPolicies(PolicyType type, String organizationId, String entityId,
            String entityVersion, List<Long> newOrder) throws StorageException {
        int orderIndex = 0;
        for (Long policyId : newOrder) {
            PolicyBean storedPolicy = getPolicy(type, organizationId, entityId, entityVersion, policyId);
            if (storedPolicy == null) {
                throw new StorageException("Invalid policy id: " + policyId); //$NON-NLS-1$
            }
            storedPolicy.setOrderIndex(orderIndex++);
            updatePolicy(storedPolicy);
        }
    }

    /**
     * @see io.apiman.manager.api.jpa.AbstractJpaStorage#find(io.apiman.manager.api.beans.search.SearchCriteriaBean, java.lang.Class)
     */
    @Override
    protected <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, Class<T> type) throws StorageException {
        beginTx();
        try {
            SearchResultsBean<T> rval = super.find(criteria, type);
            return rval;
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findOrganizations(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<OrganizationSummaryBean> findOrganizations(SearchCriteriaBean criteria)
            throws StorageException {
        SearchResultsBean<OrganizationBean> orgs = find(criteria, OrganizationBean.class);
        SearchResultsBean<OrganizationSummaryBean> rval = new SearchResultsBean<>();
        rval.setTotalSize(orgs.getTotalSize());
        List<OrganizationBean> beans = orgs.getBeans();
        for (OrganizationBean bean : beans) {
            OrganizationSummaryBean osb = new OrganizationSummaryBean();
            osb.setId(bean.getId());
            osb.setName(bean.getName());
            osb.setDescription(bean.getDescription());
            rval.getBeans().add(osb);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findClients(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ClientSummaryBean> findClients(SearchCriteriaBean criteria)
            throws StorageException {
        SearchResultsBean<ClientBean> result = find(criteria, ClientBean.class);

        SearchResultsBean<ClientSummaryBean> rval = new SearchResultsBean<>();
        rval.setTotalSize(result.getTotalSize());
        List<ClientBean> beans = result.getBeans();
        rval.setBeans(new ArrayList<>(beans.size()));
        for (ClientBean client : beans) {
            ClientSummaryBean summary = new ClientSummaryBean();
            OrganizationBean organization = client.getOrganization();
            summary.setId(client.getId());
            summary.setName(client.getName());
            summary.setDescription(client.getDescription());
            // TODO find the number of contracts - probably need native SQL for that
            summary.setNumContracts(0);
            summary.setOrganizationId(client.getOrganization().getId());
            summary.setOrganizationName(organization.getName());
            rval.getBeans().add(summary);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findApis(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ApiSummaryBean> findApis(SearchCriteriaBean criteria)
            throws StorageException {
        SearchResultsBean<ApiBean> result = find(criteria, ApiBean.class);
        SearchResultsBean<ApiSummaryBean> rval = new SearchResultsBean<>();
        rval.setTotalSize(result.getTotalSize());
        List<ApiBean> beans = result.getBeans();
        rval.setBeans(new ArrayList<>(beans.size()));
        for (ApiBean api : beans) {
            ApiSummaryBean summary = new ApiSummaryBean();
            OrganizationBean organization = api.getOrganization();
            summary.setId(api.getId());
            summary.setName(api.getName());
            summary.setDescription(api.getDescription());
            summary.setCreatedOn(api.getCreatedOn());
            summary.setOrganizationId(api.getOrganization().getId());
            summary.setOrganizationName(organization.getName());
            rval.getBeans().add(summary);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findPlans(java.lang.String, io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<PlanSummaryBean> findPlans(String organizationId, SearchCriteriaBean criteria)
            throws StorageException {

        criteria.addFilter("organization.id", organizationId, SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
        SearchResultsBean<PlanBean> result = find(criteria, PlanBean.class);
        SearchResultsBean<PlanSummaryBean> rval = new SearchResultsBean<>();
        rval.setTotalSize(result.getTotalSize());
        List<PlanBean> plans = result.getBeans();
        rval.setBeans(new ArrayList<>(plans.size()));
        for (PlanBean plan : plans) {
            PlanSummaryBean summary = new PlanSummaryBean();
            OrganizationBean organization = plan.getOrganization();
            summary.setId(plan.getId());
            summary.setName(plan.getName());
            summary.setDescription(plan.getDescription());
            summary.setOrganizationId(plan.getOrganization().getId());
            summary.setOrganizationName(organization.getName());
            rval.getBeans().add(summary);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createAuditEntry(io.apiman.manager.api.beans.audit.AuditEntryBean)
     */
    @Override
    public void createAuditEntry(AuditEntryBean entry) throws StorageException {
        super.create(entry);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#auditEntity(java.lang.String, java.lang.String, java.lang.String, java.lang.Class, io.apiman.manager.api.beans.search.PagingBean)
     */
    @Override
    public <T> SearchResultsBean<AuditEntryBean> auditEntity(String organizationId, String entityId, String entityVersion,
            Class<T> type, PagingBean paging) throws StorageException {
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        if (paging != null) {
            criteria.setPaging(paging);
        } else {
            criteria.setPage(1);
            criteria.setPageSize(20);
        }
        criteria.setOrder("id", false); //$NON-NLS-1$
        if (organizationId != null) {
            criteria.addFilter("organizationId", organizationId, SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
        }
        if (entityId != null) {
            criteria.addFilter("entityId", entityId, SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
        }
        if (entityVersion != null) {
            criteria.addFilter("entityVersion", entityVersion, SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
        }
        if (type != null) {
            AuditEntityType entityType = null;
            if (type == OrganizationBean.class) {
                entityType = AuditEntityType.Organization;
            } else if (type == ClientBean.class) {
                entityType = AuditEntityType.Client;
            } else if (type == ApiBean.class) {
                entityType = AuditEntityType.Api;
            } else if (type == PlanBean.class) {
                entityType = AuditEntityType.Plan;
            }
            if (entityType != null) {
                criteria.addFilter("entityType", entityType.name(), SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
            }
        }

        return find(criteria, AuditEntryBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#auditUser(java.lang.String, io.apiman.manager.api.beans.search.PagingBean)
     */
    @Override
    public <T> SearchResultsBean<AuditEntryBean> auditUser(String userId, PagingBean paging)
            throws StorageException {
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        if (paging != null) {
            criteria.setPaging(paging);
        } else {
            criteria.setPage(1);
            criteria.setPageSize(20);
        }
        criteria.setOrder("createdOn", false); //$NON-NLS-1$
        if (userId != null) {
            criteria.addFilter("who", userId, SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
        }

        return find(criteria, AuditEntryBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listGateways()
     */
    @Override
    public List<GatewaySummaryBean> listGateways() throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();

            @SuppressWarnings("nls")
            String sql =
                    "SELECT g.id, g.name, g.description, g.type" +
                    "  FROM gateways g" +
                    " ORDER BY g.name ASC";
            Query query = entityManager.createNativeQuery(sql);

            List<Object[]> rows = query.getResultList();
            List<GatewaySummaryBean> gateways = new ArrayList<>(rows.size());
            for (Object [] row : rows) {
                GatewaySummaryBean gateway = new GatewaySummaryBean();
                gateway.setId(String.valueOf(row[0]));
                gateway.setName(String.valueOf(row[1]));
                gateway.setDescription(String.valueOf(row[2]));
                gateway.setType(GatewayType.valueOf(String.valueOf(row[3])));
                gateways.add(gateway);
            }
            return gateways;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPlugins()
     */
    @Override
    public List<PluginSummaryBean> listPlugins() throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();

            @SuppressWarnings("nls")
            String sql =
                    "SELECT p.id, p.artifact_id, p.group_id, p.version, p.classifier, p.type, p.name, p.description, p.created_by, p.created_on" +
                    "  FROM plugins p" +
                    " WHERE p.deleted IS NULL OR p.deleted = 0" +
                    " ORDER BY p.name ASC";
            Query query = entityManager.createNativeQuery(sql);

            List<Object[]> rows = query.getResultList();
            List<PluginSummaryBean> plugins = new ArrayList<>(rows.size());
            for (Object [] row : rows) {
                PluginSummaryBean plugin = new PluginSummaryBean();
                plugin.setId(((Number) row[0]).longValue());
                plugin.setArtifactId(String.valueOf(row[1]));
                plugin.setGroupId(String.valueOf(row[2]));
                plugin.setVersion(String.valueOf(row[3]));
                plugin.setClassifier((String) row[4]);
                plugin.setType((String) row[5]);
                plugin.setName(String.valueOf(row[6]));
                plugin.setDescription(String.valueOf(row[7]));
                plugin.setCreatedBy(String.valueOf(row[8]));
                plugin.setCreatedOn((Date) row[9]);
                plugins.add(plugin);
            }
            return plugins;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPolicyDefinitions()
     */
    @Override
    public List<PolicyDefinitionSummaryBean> listPolicyDefinitions() throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();

            @SuppressWarnings("nls")
            String sql =
                    "SELECT pd.id, pd.policy_impl, pd.name, pd.description, pd.icon, pd.plugin_id, pd.form_type" +
                    "  FROM policydefs pd" +
                    " WHERE pd.deleted IS NULL OR pd.deleted = 0" +
                    " ORDER BY pd.name ASC";
            Query query = entityManager.createNativeQuery(sql);

            List<Object[]> rows = query.getResultList();
            List<PolicyDefinitionSummaryBean> rval = new ArrayList<>(rows.size());
            for (Object [] row : rows) {
                PolicyDefinitionSummaryBean bean = new PolicyDefinitionSummaryBean();
                bean.setId(String.valueOf(row[0]));
                bean.setPolicyImpl(String.valueOf(row[1]));
                bean.setName(String.valueOf(row[2]));
                bean.setDescription(String.valueOf(row[3]));
                bean.setIcon(String.valueOf(row[4]));
                if (row[5] != null) {
                    bean.setPluginId(((Number) row[5]).longValue());
                }
                if (row[6] != null) {
                    bean.setFormType(PolicyFormType.valueOf(String.valueOf(row[6])));
                }
                rval.add(bean);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getOrgs(java.util.Set)
     */
    @Override
    public List<OrganizationSummaryBean> getOrgs(Set<String> orgIds) throws StorageException {
        List<OrganizationSummaryBean> orgs = new ArrayList<>();
        if (orgIds == null || orgIds.isEmpty()) {
            return orgs;
        }
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT o from OrganizationBean o WHERE o.id IN :orgs ORDER BY o.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$
            List<OrganizationBean> qr = query.getResultList();
            for (OrganizationBean bean : qr) {
                OrganizationSummaryBean summary = new OrganizationSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                orgs.add(summary);
            }
            return orgs;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getClientsInOrg(java.lang.String)
     */
    @Override
    public List<ClientSummaryBean> getClientsInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<>();
        orgIds.add(orgId);
        return getClientsInOrgs(orgIds);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getClientsInOrgs(java.util.Set)
     */
    @Override
    public List<ClientSummaryBean> getClientsInOrgs(Set<String> orgIds) throws StorageException {
        List<ClientSummaryBean> rval = new ArrayList<>();
        if (orgIds == null || orgIds.isEmpty()) {
            return rval;
        }
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT a FROM ClientBean a JOIN a.organization o WHERE o.id IN :orgs ORDER BY a.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$

            List<ClientBean> qr = query.getResultList();
            for (ClientBean bean : qr) {
                ClientSummaryBean summary = new ClientSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                // TODO find the number of contracts - probably need a native SQL query to pull that together
                summary.setNumContracts(0);
                OrganizationBean org = bean.getOrganization();
                summary.setOrganizationId(org.getId());
                summary.setOrganizationName(org.getName());
                rval.add(summary);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApisInOrg(java.lang.String)
     */
    @Override
    public List<ApiSummaryBean> getApisInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<>();
        orgIds.add(orgId);
        return getApisInOrgs(orgIds);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApisInOrgs(java.util.Set)
     */
    @Override
    public List<ApiSummaryBean> getApisInOrgs(Set<String> orgIds) throws StorageException {
        List<ApiSummaryBean> rval = new ArrayList<>();
        if (orgIds == null || orgIds.isEmpty()) {
            return rval;
        }
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT a FROM ApiBean a JOIN a.organization o WHERE o.id IN :orgs ORDER BY a.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$

            List<ApiBean> qr = query.getResultList();
            for (ApiBean bean : qr) {
                ApiSummaryBean summary = new ApiSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                summary.setCreatedOn(bean.getCreatedOn());
                OrganizationBean org = bean.getOrganization();
                summary.setOrganizationId(org.getId());
                summary.setOrganizationName(org.getName());
                rval.add(summary);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApiVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiVersionBean getApiVersion(String orgId, String apiId, String version)
            throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT v from ApiVersionBean v JOIN v.api s JOIN s.organization o WHERE o.id = :orgId AND s.id = :apiId AND v.version = :version"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("apiId", apiId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$

            return (ApiVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApiDefinition(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public InputStream getApiDefinition(ApiVersionBean apiVersion) throws StorageException {
        ApiDefinitionBean bean = super.get(apiVersion.getId(), ApiDefinitionBean.class);
        if (bean == null) {
            return null;
        } else {
            return new ByteArrayInputStream(bean.getData());
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApiVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ApiVersionSummaryBean> getApiVersions(String orgId, String apiId)
            throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql =
                      "SELECT v "
                    + "  FROM ApiVersionBean v"
                    + "  JOIN v.api s"
                    + "  JOIN s.organization o"
                    + " WHERE o.id = :orgId"
                    + "  AND s.id = :apiId"
                    + " ORDER BY v.createdOn DESC";
            Query query = entityManager.createQuery(jpql);
            query.setMaxResults(500);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("apiId", apiId); //$NON-NLS-1$

            List<ApiVersionBean> apiVersions = query.getResultList();
            List<ApiVersionSummaryBean> rval = new ArrayList<>(apiVersions.size());
            for (ApiVersionBean apiVersion : apiVersions) {
                ApiVersionSummaryBean svsb = new ApiVersionSummaryBean();
                svsb.setOrganizationId(apiVersion.getApi().getOrganization().getId());
                svsb.setOrganizationName(apiVersion.getApi().getOrganization().getName());
                svsb.setId(apiVersion.getApi().getId());
                svsb.setName(apiVersion.getApi().getName());
                svsb.setDescription(apiVersion.getApi().getDescription());
                svsb.setVersion(apiVersion.getVersion());
                svsb.setStatus(apiVersion.getStatus());
                svsb.setPublicAPI(apiVersion.isPublicAPI());
                rval.add(svsb);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApiVersionPlans(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ApiPlanSummaryBean> getApiVersionPlans(String organizationId, String apiId,
            String version) throws StorageException {
        List<ApiPlanSummaryBean> plans = new ArrayList<>();

        beginTx();
        try {
            ApiVersionBean versionBean = getApiVersion(organizationId, apiId, version);
            Set<ApiPlanBean> apiPlans = versionBean.getPlans();
            if (apiPlans != null) {
                for (ApiPlanBean spb : apiPlans) {
                    PlanVersionBean planVersion = getPlanVersion(organizationId, spb.getPlanId(), spb.getVersion());
                    ApiPlanSummaryBean summary = new ApiPlanSummaryBean();
                    summary.setPlanId(planVersion.getPlan().getId());
                    summary.setPlanName(planVersion.getPlan().getName());
                    summary.setPlanDescription(planVersion.getPlan().getDescription());
                    summary.setVersion(spb.getVersion());
                    plans.add(summary);
                }
            }
            return plans;
        } catch (StorageException e) {
            throw e;
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getContracts(java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public List<ContractSummaryBean> getContracts(String organizationId, String apiId,
            String version, int page, int pageSize) throws StorageException {
        int start = (page - 1) * pageSize;
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql =
                    "SELECT c from ContractBean c " +
                    "  JOIN c.api apiv " +
                    "  JOIN apiv.api api " +
                    "  JOIN c.client clientv " +
                    "  JOIN clientv.client client " +
                    "  JOIN api.organization sorg" +
                    "  JOIN client.organization aorg" +
                    " WHERE api.id = :apiId " +
                    "   AND sorg.id = :orgId " +
                    "   AND apiv.version = :version " +
                    " ORDER BY sorg.id, api.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            query.setParameter("apiId", apiId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$
            query.setFirstResult(start);
            query.setMaxResults(pageSize);
            List<ContractBean> contracts = query.getResultList();
            List<ContractSummaryBean> rval = new ArrayList<>(contracts.size());
            for (ContractBean contractBean : contracts) {
                ClientBean client = contractBean.getClient().getClient();
                ApiBean api = contractBean.getApi().getApi();
                PlanBean plan = contractBean.getPlan().getPlan();

                OrganizationBean clientOrg = entityManager.find(OrganizationBean.class, client.getOrganization().getId());
                OrganizationBean apiOrg = entityManager.find(OrganizationBean.class, api.getOrganization().getId());

                ContractSummaryBean csb = new ContractSummaryBean();
                csb.setClientId(client.getId());
                csb.setClientOrganizationId(client.getOrganization().getId());
                csb.setClientOrganizationName(clientOrg.getName());
                csb.setClientName(client.getName());
                csb.setClientVersion(contractBean.getClient().getVersion());
                csb.setContractId(contractBean.getId());
                csb.setCreatedOn(contractBean.getCreatedOn());
                csb.setPlanId(plan.getId());
                csb.setPlanName(plan.getName());
                csb.setPlanVersion(contractBean.getPlan().getVersion());
                csb.setApiDescription(api.getDescription());
                csb.setApiId(api.getId());
                csb.setApiName(api.getName());
                csb.setApiOrganizationId(apiOrg.getId());
                csb.setApiOrganizationName(apiOrg.getName());
                csb.setApiVersion(contractBean.getApi().getVersion());

                rval.add(csb);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getClientVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ClientVersionBean getClientVersion(String orgId, String clientId, String version)
            throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT v from ClientVersionBean v JOIN v.client a JOIN a.organization o WHERE o.id = :orgId AND a.id = :clientId AND v.version = :version"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("clientId", clientId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$

            return (ClientVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getClientVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ClientVersionSummaryBean> getClientVersions(String orgId, String clientId)
            throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql =
                      "SELECT v"
                    + "  FROM ClientVersionBean v"
                    + "  JOIN v.client a"
                    + "  JOIN a.organization o"
                    + " WHERE o.id = :orgId"
                    + "   AND a.id = :clientId"
                    + " ORDER BY v.createdOn DESC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setMaxResults(500);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("clientId", clientId); //$NON-NLS-1$
            List<ClientVersionBean> clientVersions = query.getResultList();
            List<ClientVersionSummaryBean> rval = new ArrayList<>();
            for (ClientVersionBean clientVersion : clientVersions) {
                ClientVersionSummaryBean avsb = new ClientVersionSummaryBean();
                avsb.setOrganizationId(clientVersion.getClient().getOrganization().getId());
                avsb.setOrganizationName(clientVersion.getClient().getOrganization().getName());
                avsb.setId(clientVersion.getClient().getId());
                avsb.setName(clientVersion.getClient().getName());
                avsb.setDescription(clientVersion.getClient().getDescription());
                avsb.setVersion(clientVersion.getVersion());
                avsb.setApiKey(clientVersion.getApikey());
                avsb.setStatus(clientVersion.getStatus());

                rval.add(avsb);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getClientContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ContractSummaryBean> getClientContracts(String organizationId, String clientId,
            String version) throws StorageException {
        beginTx();
        try {
            return getClientContractsInternal(organizationId, clientId, version);
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }
    
    /**
     * Returns a list of all contracts for the given client.
     * @param organizationId
     * @param clientId
     * @param version
     * @throws StorageException
     */
    protected List<ContractSummaryBean> getClientContractsInternal(String organizationId, String clientId,
            String version) throws StorageException {
        List<ContractSummaryBean> rval = new ArrayList<>();
        EntityManager entityManager = getActiveEntityManager();
        @SuppressWarnings("nls")
        String jpql =
                "SELECT c from ContractBean c " +
                "  JOIN c.client clientv " +
                "  JOIN clientv.client client " +
                "  JOIN client.organization aorg" +
                " WHERE client.id = :clientId " +
                "   AND aorg.id = :orgId " +
                "   AND clientv.version = :version " +
                " ORDER BY aorg.id, client.id ASC";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", organizationId); //$NON-NLS-1$
        query.setParameter("clientId", clientId); //$NON-NLS-1$
        query.setParameter("version", version); //$NON-NLS-1$
        List<ContractBean> contracts = query.getResultList();
        for (ContractBean contractBean : contracts) {
            ClientBean client = contractBean.getClient().getClient();
            ApiBean api = contractBean.getApi().getApi();
            PlanBean plan = contractBean.getPlan().getPlan();

            OrganizationBean clientOrg = entityManager.find(OrganizationBean.class, client.getOrganization().getId());
            OrganizationBean apiOrg = entityManager.find(OrganizationBean.class, api.getOrganization().getId());

            ContractSummaryBean csb = new ContractSummaryBean();
            csb.setClientId(client.getId());
            csb.setClientOrganizationId(client.getOrganization().getId());
            csb.setClientOrganizationName(clientOrg.getName());
            csb.setClientName(client.getName());
            csb.setClientVersion(contractBean.getClient().getVersion());
            csb.setContractId(contractBean.getId());
            csb.setCreatedOn(contractBean.getCreatedOn());
            csb.setPlanId(plan.getId());
            csb.setPlanName(plan.getName());
            csb.setPlanVersion(contractBean.getPlan().getVersion());
            csb.setApiDescription(api.getDescription());
            csb.setApiId(api.getId());
            csb.setApiName(api.getName());
            csb.setApiOrganizationId(apiOrg.getId());
            csb.setApiOrganizationName(apiOrg.getName());
            csb.setApiVersion(contractBean.getApi().getVersion());

            rval.add(csb);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApiRegistry(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiRegistryBean getApiRegistry(String organizationId, String clientId, String version)
            throws StorageException {
        ApiRegistryBean rval = new ApiRegistryBean();

        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql =
                    "SELECT c from ContractBean c " +
                    "  JOIN c.client clientv " +
                    "  JOIN clientv.client client " +
                    "  JOIN client.organization aorg" +
                    " WHERE client.id = :clientId " +
                    "   AND aorg.id = :orgId " +
                    "   AND clientv.version = :version " +
                    " ORDER BY c.id ASC";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            query.setParameter("clientId", clientId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$

            List<ContractBean> contracts = query.getResultList();
            for (ContractBean contractBean : contracts) {
                ApiVersionBean svb = contractBean.getApi();
                ApiBean api = svb.getApi();
                PlanBean plan = contractBean.getPlan().getPlan();

                OrganizationBean apiOrg = api.getOrganization();

                ApiEntryBean entry = new ApiEntryBean();
                entry.setApiId(api.getId());
                entry.setApiName(api.getName());
                entry.setApiOrgId(apiOrg.getId());
                entry.setApiOrgName(apiOrg.getName());
                entry.setApiVersion(svb.getVersion());
                entry.setPlanId(plan.getId());
                entry.setPlanName(plan.getName());
                entry.setPlanVersion(contractBean.getPlan().getVersion());

                Set<ApiGatewayBean> gateways = svb.getGateways();
                if (gateways != null && !gateways.isEmpty()) {
                    ApiGatewayBean sgb = gateways.iterator().next();
                    entry.setGatewayId(sgb.getGatewayId());
                }

                rval.getApis().add(entry);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlansInOrg(java.lang.String)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<>();
        orgIds.add(orgId);
        return getPlansInOrgs(orgIds);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlansInOrgs(java.util.Set)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrgs(Set<String> orgIds) throws StorageException {
        List<PlanSummaryBean> rval = new ArrayList<>();
        if (orgIds == null || orgIds.isEmpty()) {
            return rval;
        }
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT p FROM PlanBean p JOIN p.organization o WHERE o.id IN :orgs ORDER BY p.id ASC"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds); //$NON-NLS-1$
            query.setMaxResults(500);

            List<PlanBean> qr = query.getResultList();
            for (PlanBean bean : qr) {
                PlanSummaryBean summary = new PlanSummaryBean();
                summary.setId(bean.getId());
                summary.setName(bean.getName());
                summary.setDescription(bean.getDescription());
                OrganizationBean org = bean.getOrganization();
                summary.setOrganizationId(org.getId());
                summary.setOrganizationName(org.getName());
                rval.add(summary);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlanVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PlanVersionBean getPlanVersion(String orgId, String planId, String version)
            throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT v from PlanVersionBean v JOIN v.plan p JOIN p.organization o WHERE o.id = :orgId AND p.id = :planId AND v.version = :version"; //$NON-NLS-1$
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("planId", planId); //$NON-NLS-1$
            query.setParameter("version", version); //$NON-NLS-1$

            return (PlanVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlanVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<PlanVersionSummaryBean> getPlanVersions(String orgId, String planId) throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            @SuppressWarnings("nls")
            String jpql = "SELECT v from PlanVersionBean v" +
                          "  JOIN v.plan p" +
                          "  JOIN p.organization o" +
                          " WHERE o.id = :orgId" +
                          "   AND p.id = :planId" +
                          " ORDER BY v.createdOn DESC";
            Query query = entityManager.createQuery(jpql);
            query.setMaxResults(500);
            query.setParameter("orgId", orgId); //$NON-NLS-1$
            query.setParameter("planId", planId); //$NON-NLS-1$
            List<PlanVersionBean> planVersions = query.getResultList();
            List<PlanVersionSummaryBean> rval = new ArrayList<>(planVersions.size());
            for (PlanVersionBean planVersion : planVersions) {
                PlanVersionSummaryBean pvsb = new PlanVersionSummaryBean();
                pvsb.setOrganizationId(planVersion.getPlan().getOrganization().getId());
                pvsb.setOrganizationName(planVersion.getPlan().getOrganization().getName());
                pvsb.setId(planVersion.getPlan().getId());
                pvsb.setName(planVersion.getPlan().getName());
                pvsb.setDescription(planVersion.getPlan().getDescription());
                pvsb.setVersion(planVersion.getVersion());
                pvsb.setStatus(planVersion.getStatus());
                rval.add(pvsb);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @SuppressWarnings("nls")
    @Override
    public List<PolicySummaryBean> getPolicies(String organizationId, String entityId, String version,
            PolicyType type) throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql =
                      "SELECT p from PolicyBean p "
                    + " WHERE p.organizationId = :orgId "
                    + "   AND p.entityId = :entityId "
                    + "   AND p.entityVersion = :entityVersion "
                    + "   AND p.type = :type"
                    + " ORDER BY p.orderIndex ASC";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", organizationId);
            query.setParameter("entityId", entityId);
            query.setParameter("entityVersion", version);
            query.setParameter("type", type);

            List<PolicyBean> policyBeans = query.getResultList();
            List<PolicySummaryBean> rval = new ArrayList<>(policyBeans.size());
            for (PolicyBean policyBean : policyBeans) {
                PolicyTemplateUtil.generatePolicyDescription(policyBean);
                PolicySummaryBean psb = new PolicySummaryBean();
                psb.setId(policyBean.getId());
                psb.setName(policyBean.getName());
                psb.setDescription(policyBean.getDescription());
                psb.setPolicyDefinitionId(policyBean.getDefinition().getId());
                psb.setIcon(policyBean.getDefinition().getIcon());
                psb.setCreatedBy(policyBean.getCreatedBy());
                psb.setCreatedOn(policyBean.getCreatedOn());
                rval.add(psb);
            }
            return rval;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getMaxPolicyOrderIndex(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @Override
    public int getMaxPolicyOrderIndex(String organizationId, String entityId, String entityVersion,
            PolicyType type) throws StorageException {
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        criteria.addFilter("organizationId", organizationId, SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
        criteria.addFilter("entityId", entityId, SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
        criteria.addFilter("entityVersion", entityVersion, SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
        criteria.addFilter("type", type.name(), SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
        criteria.setOrder("orderIndex", false); //$NON-NLS-1$
        criteria.setPage(1);
        criteria.setPageSize(1);
        SearchResultsBean<PolicyBean> resultsBean = find(criteria, PolicyBean.class);
        if (resultsBean.getBeans() == null || resultsBean.getBeans().isEmpty()) {
            return 0;
        } else {
            return resultsBean.getBeans().get(0).getOrderIndex();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPluginPolicyDefs(java.lang.Long)
     */
    @Override
    public List<PolicyDefinitionSummaryBean> listPluginPolicyDefs(Long pluginId) throws StorageException {
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();

            @SuppressWarnings("nls")
            String sql =
                    "SELECT pd.id, pd.policy_impl, pd.name, pd.description, pd.icon, pd.plugin_id, pd.form_type" +
                    "  FROM policydefs pd" +
                    " WHERE pd.plugin_id = ?" +
                    " ORDER BY pd.name ASC";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, pluginId);

            List<Object[]> rows = query.getResultList();
            List<PolicyDefinitionSummaryBean> beans = new ArrayList<>(rows.size());
            for (Object [] row : rows) {
                PolicyDefinitionSummaryBean bean = new PolicyDefinitionSummaryBean();
                bean.setId(String.valueOf(row[0]));
                bean.setPolicyImpl(String.valueOf(row[1]));
                bean.setName(String.valueOf(row[2]));
                bean.setDescription(String.valueOf(row[3]));
                bean.setIcon(String.valueOf(row[4]));
                if (row[5] != null) {
                    bean.setPluginId(((Number) row[5]).longValue());
                }
                if (row[6] != null) {
                    bean.setFormType(PolicyFormType.valueOf(String.valueOf(row[6])));
                }
                beans.add(bean);
            }
            return beans;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void createUser(UserBean user) throws StorageException {
        super.create(user);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getUser(java.lang.String)
     */
    @Override
    public UserBean getUser(String userId) throws StorageException {
        return super.get(userId, UserBean.class);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void updateUser(UserBean user) throws StorageException {
        super.update(user);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findUsers(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException {
        beginTx();
        try {
            return super.find(criteria, UserBean.class);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void createRole(RoleBean role) throws StorageException {
        super.create(role);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void updateRole(RoleBean role) throws StorageException {
        super.update(role);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void deleteRole(RoleBean role) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();

            RoleBean prole = get(role.getId(), RoleBean.class);

            // First delete all memberships in this role
            Query query = entityManager.createQuery("DELETE from RoleMembershipBean m WHERE m.roleId = :roleId" ); //$NON-NLS-1$
            query.setParameter("roleId", role.getId()); //$NON-NLS-1$
            query.executeUpdate();

            // Then delete the role itself.
            super.delete(prole);
        } catch (Throwable t) {
            throw new StorageException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getRole(java.lang.String)
     */
    @Override
    public RoleBean getRole(String roleId) throws StorageException {
        return getRoleInternal(roleId);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findRoles(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) throws StorageException {
        beginTx();
        try {
            return super.find(criteria, RoleBean.class);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createMembership(io.apiman.manager.api.beans.idm.RoleMembershipBean)
     */
    @Override
    public void createMembership(RoleMembershipBean membership) throws StorageException {
        super.create(membership);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public RoleMembershipBean getMembership(String userId, String roleId, String organizationId) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            Query query = entityManager.createQuery("SELECT m FROM RoleMembershipBean m WHERE m.roleId = :roleId AND m.userId = :userId AND m.organizationId = :orgId" ); //$NON-NLS-1$
            query.setParameter("roleId", roleId); //$NON-NLS-1$
            query.setParameter("userId", userId); //$NON-NLS-1$
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            RoleMembershipBean bean = null;
            List<?> resultList = query.getResultList();
            if (!resultList.isEmpty()) {
                bean = (RoleMembershipBean) resultList.iterator().next();
            }
            return bean;
        } catch (Throwable t) {
            throw new StorageException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            Query query = entityManager.createQuery("DELETE FROM RoleMembershipBean m WHERE m.roleId = :roleId AND m.userId = :userId AND m.organizationId = :orgId" ); //$NON-NLS-1$
            query.setParameter("roleId", roleId); //$NON-NLS-1$
            query.setParameter("userId", userId); //$NON-NLS-1$
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            query.executeUpdate();
        } catch (Throwable t) {
            throw new StorageException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMemberships(String userId, String organizationId) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            Query query = entityManager.createQuery("DELETE FROM RoleMembershipBean m WHERE m.userId = :userId AND m.organizationId = :orgId" ); //$NON-NLS-1$
            query.setParameter("userId", userId); //$NON-NLS-1$
            query.setParameter("orgId", organizationId); //$NON-NLS-1$
            query.executeUpdate();
        } catch (Throwable t) {
            throw new StorageException(t);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getUserMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException {
        Set<RoleMembershipBean> memberships = new HashSet<>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(builder.equal(from.get("userId"), userId)); //$NON-NLS-1$
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            memberships.addAll(resultList);
            return memberships;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getUserMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId, String organizationId) throws StorageException {
        Set<RoleMembershipBean> memberships = new HashSet<>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(
                    builder.equal(from.get("userId"), userId), //$NON-NLS-1$
                    builder.equal(from.get("organizationId"), organizationId) ); //$NON-NLS-1$
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            memberships.addAll(resultList);
            return memberships;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getOrgMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException {
        Set<RoleMembershipBean> memberships = new HashSet<>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(builder.equal(from.get("organizationId"), organizationId)); //$NON-NLS-1$
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            memberships.addAll(resultList);
            return memberships;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPermissions(java.lang.String)
     */
    @Override
    public Set<PermissionBean> getPermissions(String userId) throws StorageException {
        Set<PermissionBean> permissions = new HashSet<>();
        beginTx();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(builder.equal(from.get("userId"), userId)); //$NON-NLS-1$
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            typedQuery.setMaxResults(500);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            for (RoleMembershipBean membership : resultList) {
                RoleBean role = getRoleInternal(membership.getRoleId());
                String qualifier = membership.getOrganizationId();
                for (PermissionType permission : role.getPermissions()) {
                    PermissionBean p = new PermissionBean();
                    p.setName(permission);
                    p.setOrganizationId(qualifier);
                    permissions.add(p);
                }
            }
            return permissions;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            throw new StorageException(t);
        } finally {
            rollbackTx();
        }
    }

    /**
     * @param roleId
     * @return a role by id
     * @throws StorageException
     * @throws DoesNotExistException
     */
    protected RoleBean getRoleInternal(String roleId) throws StorageException {
        return super.get(roleId, RoleBean.class);
    }

    @SuppressWarnings("nls")
    @Override
    public Iterator<OrganizationBean> getAllOrganizations() throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jqpl = "SELECT b FROM OrganizationBean b ORDER BY b.id ASC";
        Query query = entityManager.createQuery(jqpl);

        return super.getAll(OrganizationBean.class, query);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPlans(java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<PlanBean> getAllPlans(String organizationId) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM PlanBean b "
                + "WHERE b.organization.id = :orgId "
                + "ORDER BY b.id ASC"; //$NON-NLS-1$

        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", organizationId);
        return super.getAll(PlanBean.class, query);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllApis(java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<ApiBean> getAllApis(String organizationId) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM ApiBean b "
                + "WHERE b.organization.id = :orgId "
                + "ORDER BY b.id ASC"; //$NON-NLS-1$

        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", organizationId);
        return super.getAll(ApiBean.class, query);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllClients(java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<ClientBean> getAllClients(String organizationId) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM ClientBean b "
                + "WHERE b.organization.id = :orgId "
                + "ORDER BY b.id ASC"; //$NON-NLS-1$

        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", organizationId);
        return super.getAll(ClientBean.class, query);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllClientVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<ClientVersionBean> getAllClientVersions(String organizationId,
            String clientId) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql = "SELECT v "
                + "   FROM ClientVersionBean v "
                + "   JOIN v.client a "
                + "   JOIN a.organization o "
                + "  WHERE o.id = :orgId "
                + "    AND a.id = :clientId"
                + "  ORDER BY v.id ASC";

        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", organizationId);
        query.setParameter("clientId", clientId);
        return super.getAll(ClientVersionBean.class, query);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Iterator<ContractBean> getAllContracts(String organizationId, String clientId, String version)
            throws StorageException {
        EntityManager entityManager = getActiveEntityManager();
        @SuppressWarnings("nls")
        String jpql =
                "SELECT c from ContractBean c " +
                "  JOIN c.client clientv " +
                "  JOIN clientv.client client " +
                "  JOIN client.organization aorg" +
                " WHERE client.id = :clientId " +
                "   AND aorg.id = :orgId " +
                "   AND clientv.version = :version " +
                " ORDER BY c.id ASC";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", organizationId); //$NON-NLS-1$
        query.setParameter("clientId", clientId); //$NON-NLS-1$
        query.setParameter("version", version); //$NON-NLS-1$

        return getAll(ContractBean.class, query);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<PolicyBean> getAllPolicies(String organizationId, String entityId, String version,
            PolicyType type) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();
        String jpql =
                  "SELECT p from PolicyBean p "
                + " WHERE p.organizationId = :orgId "
                + "   AND p.entityId = :entityId "
                + "   AND p.entityVersion = :entityVersion "
                + "   AND p.type = :type"
                + " ORDER BY p.orderIndex ASC";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", organizationId);
        query.setParameter("entityId", entityId);
        query.setParameter("entityVersion", version);
        query.setParameter("type", type);
        return getAll(PolicyBean.class, query);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPlanVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<PlanVersionBean> getAllPlanVersions(String organizationId, String planId)
            throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql = "SELECT v "
                + "   FROM PlanVersionBean v "
                + "   JOIN v.plan p "
                + "   JOIN p.organization o "
                + "  WHERE o.id = :orgId "
                + "    AND p.id = :planId"
                + "  ORDER BY v.id ASC";

        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", organizationId);
        query.setParameter("planId", planId);
        return super.getAll(PlanVersionBean.class, query);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllApiVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<ApiVersionBean> getAllApiVersions(String organizationId, String apiId)
            throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql = "SELECT v "
                + "   FROM ApiVersionBean v "
                + "   JOIN v.api s "
                + "   JOIN s.organization o "
                + "  WHERE o.id = :orgId "
                + "    AND s.id = :apiId"
                + "  ORDER BY v.id ASC";

        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", organizationId);
        query.setParameter("apiId", apiId);
        return super.getAll(ApiVersionBean.class, query);
    }

    @SuppressWarnings("nls")
    @Override
    public Iterator<GatewayBean> getAllGateways() throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b FROM GatewayBean b ORDER BY b.id ASC";

        Query query = entityManager.createQuery(jpql);
        return super.getAll(GatewayBean.class, query);
    }

    @SuppressWarnings("nls")
    @Override
    public Iterator<AuditEntryBean> getAllAuditEntries(String orgId) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM AuditEntryBean b "
                + "WHERE organization_id = :orgId "
                + "ORDER BY b.id ASC"; //$NON-NLS-1$

        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", orgId);
        return super.getAll(AuditEntryBean.class, query);
    }

    @SuppressWarnings("nls")
    @Override
    public Iterator<PluginBean> getAllPlugins() throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM PluginBean b "
                + "ORDER BY b.id ASC";

        Query query = entityManager.createQuery(jpql);
        return super.getAll(PluginBean.class, query);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPolicyDefinitions()
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<PolicyDefinitionBean> getAllPolicyDefinitions() throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM PolicyDefinitionBean b "
                + "ORDER BY b.id ASC";

        Query query = entityManager.createQuery(jpql);
        return super.getAll(PolicyDefinitionBean.class, query);
    }

    @SuppressWarnings("nls")
    @Override
    public Iterator<RoleMembershipBean> getAllMemberships(String orgId) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM RoleMembershipBean b "
                + "WHERE organizationId = :orgId "
                + "ORDER BY b.id ASC"; //$NON-NLS-1$

        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", orgId);
        return super.getAll(RoleMembershipBean.class, query);
    }

    @SuppressWarnings("nls")
    @Override
    public Iterator<UserBean> getAllUsers() throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM UserBean b "
                + "ORDER BY b.username ASC";

        Query query = entityManager.createQuery(jpql);
        return super.getAll(UserBean.class, query);
    }

    @SuppressWarnings("nls")
    @Override
    public Iterator<RoleBean> getAllRoles() throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM RoleBean b "
                + "ORDER BY b.id ASC";

        Query query = entityManager.createQuery(jpql);
        return super.getAll(RoleBean.class, query);
    }

}
