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

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiBean_;
import io.apiman.manager.api.beans.apis.ApiDefinitionBean;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiPlanBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.apis.ApiVersionBean_;
import io.apiman.manager.api.beans.audit.AuditEntityType;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.developers.DeveloperBean;
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
import io.apiman.manager.api.beans.plans.PlanStatus;
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
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.beans.summary.mappers.ApiMapper;
import io.apiman.manager.api.beans.system.MetadataBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.util.PolicyTemplateUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.transaction.Transactional;

import org.apache.commons.io.IOUtils;

/**
 * A JPA implementation of the storage interface.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("nls")
@ApplicationScoped @Alternative @Transactional
public class JpaStorage extends AbstractJpaStorage implements IStorage, IStorageQuery {

    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(JpaStorage.class);

    private IDataEncrypter encrypter;
    private final ApiMapper apiMapper = ApiMapper.INSTANCE;

    @Inject
    public JpaStorage(IDataEncrypter encrypter) {
        this.encrypter = encrypter;
    }

    /**
     * Constructor.
     */
    public JpaStorage() {
    }

    @PostConstruct
    public void postConstruct() {
        // Kick the encrypter, causing it to be loaded/resolved in CDI
        encrypter.encrypt("", new DataEncryptionContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createClient(ClientBean client) throws StorageException {
        super.create(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createClientVersion(ClientVersionBean version) throws StorageException {
        super.create(version);
    }

    /**
     * {@inheritDoc}
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
                    throw new IllegalStateException("Error creating contract: duplicate contract detected."); //$NON-NLS-1$
                }
        }
        super.create(contract);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createGateway(GatewayBean gateway) throws StorageException {
        super.create(gateway);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDownload(DownloadBean download) throws StorageException {
        super.create(download);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createDeveloper(DeveloperBean developerBean) throws StorageException {
        super.create(developerBean);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMetadata(MetadataBean metadata) throws StorageException {
        super.create(metadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPlugin(PluginBean plugin) throws StorageException {
        super.create(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createOrganization(OrganizationBean organization) throws StorageException {
        super.create(organization);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPlan(PlanBean plan) throws StorageException {
        super.create(plan);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPlanVersion(PlanVersionBean version) throws StorageException {
        super.create(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPolicy(PolicyBean policy) throws StorageException {
        super.create(policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        super.create(policyDef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createApi(ApiBean api) throws StorageException {
        super.create(api);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createApiVersion(ApiVersionBean version) throws StorageException {
        super.create(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClient(ClientBean client) throws StorageException {
        super.update(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateClientVersion(ClientVersionBean version) throws StorageException {
        LOGGER.debug("Updating client version: {0}", version);
        super.update(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateContract(ContractBean contract) throws StorageException {
        super.update(contract);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateGateway(GatewayBean gateway) throws StorageException {
        super.update(gateway);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateOrganization(OrganizationBean organization) throws StorageException {
        super.update(organization);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePlan(PlanBean plan) throws StorageException {
        super.update(plan);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePlanVersion(PlanVersionBean version) throws StorageException {
        super.update(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePolicy(PolicyBean policy) throws StorageException {
        super.update(policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        super.update(policyDef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePlugin(PluginBean pluginBean) throws StorageException {
        super.update(pluginBean);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDeveloper(DeveloperBean developer) throws StorageException {
        super.update(developer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateApi(ApiBean api) throws StorageException {
        super.update(api);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateApiVersion(ApiVersionBean version) throws StorageException {
        super.update(version);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public void deleteOrganization(OrganizationBean organization) throws StorageException {
        // Remove memberships
        deleteAllMemberships(organization);
        // Remove audit entries (as now orphaned)
        deleteAllAuditEntries(organization);
        // Remove contracts
        deleteAllContracts(organization);
        // Remove Policies
        deleteAllPolicies(organization);
        remove(organization);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteClient(ClientBean client) throws StorageException {
        // Remove audit
        deleteAllAuditEntries(client);
        // Remove contracts
        deleteAllContracts(client);
        // Remove policies
        deleteAllPolicies(client);
        remove(client);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteClientVersion(ClientVersionBean version) throws StorageException {
        remove(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteContract(ContractBean contract) throws StorageException {
        remove(contract);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteApi(ApiBean api) throws StorageException {
        // Remove audit entries (as now orphaned)
        deleteAllAuditEntries(api);
        // Remove contracts
        deleteAllContracts(api);
        // Remove policies
        deleteAllPolicies(api);
        remove(api);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteApiVersion(ApiVersionBean version) throws StorageException {
        remove(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteApiDefinition(ApiVersionBean version) throws StorageException {
        ApiDefinitionBean bean = super.get(version.getId(), ApiDefinitionBean.class);
        if (bean != null) {
            super.delete(bean);
        } else {
            throw new StorageException("No definition found.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePlan(PlanBean plan) throws StorageException {
        // Delete audit entries
        deleteAllAuditEntries(plan);
        // Delete entity
        super.delete(plan);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePlanVersion(PlanVersionBean version) throws StorageException {
        super.delete(version);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePolicy(PolicyBean policy) throws StorageException {
        super.delete(policy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteGateway(GatewayBean gateway) throws StorageException {
        super.delete(gateway);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDownload(DownloadBean download) throws StorageException {
        super.delete(download);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteDeveloper(DeveloperBean developer) throws StorageException {
        super.delete(developer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePlugin(PluginBean plugin) throws StorageException {
        super.delete(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        super.delete(policyDef);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrganizationBean getOrganization(String id) throws StorageException {
        return super.get(id, OrganizationBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientBean getClient(String organizationId, String id) throws StorageException {
        return super.get(organizationId, id, ClientBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContractBean getContract(Long id) throws StorageException {
        return super.get(id, ContractBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiBean getApi(String organizationId, String id) throws StorageException {
        return super.get(organizationId, id, ApiBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanBean getPlan(String organizationId, String id) throws StorageException {
        return super.get(organizationId, id, PlanBean.class);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public GatewayBean getGateway(String id) throws StorageException {
        return super.get(id, GatewayBean.class);
    }

    @Override
    public List<GatewayBean> getGateways(Set<String> ids) throws StorageException {
        return getActiveEntityManager()
                .createQuery("SELECT g FROM GatewayBean g WHERE g.id IN (:ids)", GatewayBean.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DownloadBean getDownload(String id) throws StorageException {
        return super.get(id, DownloadBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeveloperBean getDeveloper(String id) throws StorageException {
        return super.get(id, DeveloperBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MetadataBean getMetadata(Long id) throws StorageException {
        return super.get(id, MetadataBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginBean getPlugin(long id) throws StorageException {
        return super.get(id, PluginBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginBean getPlugin(String groupId, String artifactId) throws StorageException {
        try {
            Query query = getActiveEntityManager().createQuery(
                "SELECT p FROM PluginBean p"
                    + "  WHERE p.groupId = :groupId "
                    + "    AND p.artifactId = :artifactId",
                PluginBean.class)
                .setParameter("groupId", groupId)
                .setParameter("artifactId", artifactId);
            return (PluginBean) super.getOne(query).orElse(null); // TODO consider migrating to Optional
        } catch (Throwable t) {
            LOGGER.error(t, t.getMessage());
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PolicyDefinitionBean getPolicyDefinition(String id) throws StorageException {
        return super.get(id, PolicyDefinitionBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reorderPolicies(PolicyType type, String organizationId, String entityId,
            String entityVersion, List<Long> newOrder) throws StorageException {
        int orderIndex = 0;
        for (Long policyId : newOrder) {
            PolicyBean storedPolicy = getPolicy(type, organizationId, entityId, entityVersion, policyId);
            if (storedPolicy == null) {
                throw new StorageException("Invalid policy id: " + policyId);
            }
            storedPolicy.setOrderIndex(orderIndex++);
            updatePolicy(storedPolicy);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, Class<T> type) throws StorageException {
       return super.find(criteria, type);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
            summary.setImage(client.getImage());
            // TODO find the number of contracts - probably need native SQL for that
            summary.setNumContracts(0);
            summary.setOrganizationId(client.getOrganization().getId());
            summary.setOrganizationName(organization.getName());
            rval.getBeans().add(summary);
        }
        return rval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResultsBean<ApiSummaryBean> findApis(SearchCriteriaBean criteria)
            throws StorageException {
        SearchResultsBean<ApiBean> result = find(criteria, ApiBean.class);
        List<ApiSummaryBean> beans = result.getBeans()
                .stream()
                .map(apiMapper::toSummary)
                .collect(Collectors.toList());

        return new SearchResultsBean<ApiSummaryBean>()
                .setTotalSize(result.getTotalSize())
                .setBeans(beans);
    }

    /**
     * As we can't use the existing SearchCriteriaBean framework to look into relationships for our searches,
     * this is a (possibly temporary) solution that tacks on an extra criteria to any devportal search query
     * that any APIs found must have at least 1 version that is exposed in the developer portal.
     * <p>
     * This avoids potentially private APIs appearing in the dev portal.
     */
    @Override
    public SearchResultsBean<ApiSummaryBean> findExposedApis(SearchCriteriaBean criteria) throws StorageException {
        CriteriaBuilder builder = getActiveEntityManager().getCriteriaBuilder();
        CriteriaQuery<ApiBean> criteriaQuery = builder.createQuery(ApiBean.class).distinct(true);
        Root<ApiBean> root = criteriaQuery.from(ApiBean.class);

        super.applySearchCriteriaToQuery(criteria, builder, criteriaQuery , root, false);

        // If no restrictions were applied
        // TODO(msavy): tidy this up
        if (criteriaQuery.getRestriction() != null) {
            SetJoin<ApiBean, ApiVersionBean> apiVersions = root.join(ApiBean_.apiVersionSet, JoinType.INNER);
            Predicate isExposed = builder.equal(apiVersions.get(ApiVersionBean_.exposeInPortal), true);
            Predicate exposedAndCustomRestrictions = builder.and(isExposed, criteriaQuery.getRestriction());
            CriteriaQuery<ApiBean> combinedWhere = criteriaQuery.where(exposedAndCustomRestrictions);
        } else {
            SetJoin<ApiBean, ApiVersionBean> apiVersions = root.join(ApiBean_.apiVersionSet, JoinType.INNER);
            Predicate isExposed = builder.equal(apiVersions.get(ApiVersionBean_.exposeInPortal), true);
            CriteriaQuery<ApiBean> exposed = criteriaQuery.where(isExposed);
        }

        List<ApiBean> results = getActiveEntityManager()
                .createQuery(criteriaQuery)
                .getResultList();

        return new SearchResultsBean<ApiSummaryBean>()
                .setBeans(apiMapper.toSummary(results))
                .setTotalSize(results.size());

    }

    // TODO(msavy): optimise this
    @Override
    public List<ApiSummaryBean> findExposedApis() throws StorageException {
        List<ApiBean> apisWithExposedVersions = getActiveEntityManager()
                .createQuery("SELECT DISTINCT ApiBean "
                                + "FROM ApiBean ab "
                                + "JOIN ApiVersionBean avb "
                                + "WHERE ab.id = avb.api.id "
                                + "AND avb.exposeInPortal = true ", ApiBean.class)
                .getResultList();
        return apiMapper.toSummary(apisWithExposedVersions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResultsBean<PlanSummaryBean> findPlans(String organizationId, SearchCriteriaBean criteria)
            throws StorageException {

        criteria.addFilter("organization.id", organizationId, SearchCriteriaFilterOperator.eq);
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
     * {@inheritDoc}
     */
    @Override
    public void createAuditEntry(AuditEntryBean entry) throws StorageException {
        super.create(entry);
    }

    /**
     * {@inheritDoc}
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
        criteria.setOrder("id", false);
        if (organizationId != null) {
            criteria.addFilter("organizationId", organizationId, SearchCriteriaFilterOperator.eq);
        }
        if (entityId != null) {
            criteria.addFilter("entityId", entityId, SearchCriteriaFilterOperator.eq);
        }
        if (entityVersion != null) {
            criteria.addFilter("entityVersion", entityVersion, SearchCriteriaFilterOperator.eq);
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
                criteria.addFilter("entityType", entityType.name(), SearchCriteriaFilterOperator.eq);
            }
        }

        return find(criteria, AuditEntryBean.class);
    }

    /**
     * {@inheritDoc}
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
        criteria.setOrder("createdOn", false);
        if (userId != null) {
            criteria.addFilter("who", userId, SearchCriteriaFilterOperator.eq);
        }

        return find(criteria, AuditEntryBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GatewaySummaryBean> listGateways() throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();

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
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**C
     * {@inheritDoc}
     */
    @Override// TODO consider just returning GatewayBean and using converter
    public List<PluginSummaryBean> listPlugins() throws StorageException {
        try {
            String sql =
            "SELECT p.id, p.artifact_id, p.group_id, p.version, p.classifier, p.type, p.name, p.description, p.created_by, p.created_on" +
            "  FROM plugins p" +
            " WHERE p.deleted IS NULL OR p.deleted = 0" +
            " ORDER BY p.name ASC";

            return getJdbi().withHandle(handle ->
                handle.createQuery(sql)
                      .mapToBean(PluginSummaryBean.class)
                      .list()
            );
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PolicyDefinitionSummaryBean> listPolicyDefinitions() throws StorageException {
        try {
            String sql =
                "SELECT pd.id, pd.policy_impl, pd.name, pd.description, pd.icon, pd.plugin_id, pd.form_type" +
                "  FROM policydefs pd" +
                " WHERE pd.deleted IS NULL OR pd.deleted = 0" +
                " ORDER BY pd.name ASC";

            return getJdbi().withHandle(handle ->
                 handle.createQuery(sql)
                       .mapToBean(PolicyDefinitionSummaryBean.class)
                       .list()
            );
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<OrganizationSummaryBean> getOrgs(Set<String> orgIds) throws StorageException {
        if (orgIds == null || orgIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return getJdbi().withHandle(handle ->
                 handle.createQuery("SELECT * FROM ORGANIZATIONS org WHERE org.ID IN (<orgIds>) ORDER BY org.id ASC")
                       .bindList("orgIds", orgIds)
                       .mapToBean(OrganizationSummaryBean.class)
                       .list()
            );
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClientSummaryBean> getClientsInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<>();
        orgIds.add(orgId);
        return getClientsInOrgs(orgIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClientSummaryBean> getClientsInOrgs(Set<String> orgIds) throws StorageException {
        if (orgIds == null || orgIds.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String sql =
                 "SELECT client.*, " // All client fields
                  + "       (SELECT COUNT(*) " // numContracts
                  + "        FROM CONTRACTS contract "
                  + "                 JOIN CLIENT_VERSIONS clientVersion on contract.CLIENTV_ID = clientVersion.ID "
                  + "        WHERE clientVersion.CLIENT_ID = client.ID) AS NUM_CONTRACTS, "
                  + "       (SELECT NAME " // OrganizationName
                  + "        FROM ORGANIZATIONS org "
                  + "        WHERE client.ORGANIZATION_ID = org.ID)     AS ORGANIZATION_NAME "
                  + "FROM CLIENTS client "
                  + "WHERE client.ORGANIZATION_ID IN (<orgIds>) "
                  + "ORDER BY client.ID ASC";

            return getJdbi().withHandle(handle ->
                 handle.createQuery(sql)
                       .bindList("orgIds", orgIds)
                       .mapToBean(ClientSummaryBean.class)
                       .list()
            );
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ApiSummaryBean> getApisInOrg(String orgId) throws StorageException {
        return getApisInOrgs(Set.of(orgId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ApiSummaryBean> getApisInOrgs(Set<String> orgIds) throws StorageException {
        if (orgIds == null || orgIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT a FROM ApiBean a JOIN a.organization o WHERE o.id IN :orgs ORDER BY a.id ASC";
            TypedQuery<ApiBean> query = entityManager.createQuery(jpql, ApiBean.class);
            query.setParameter("orgs", orgIds);

            return query.getResultList()
                    .stream()
                    .map(apiMapper::toSummary)
                    .collect(Collectors.toList());
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ApiVersionBean getApiVersion(String orgId, String apiId, String version)
            throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT v from ApiVersionBean v JOIN v.api s JOIN s.organization o WHERE o.id = :orgId AND s.id = :apiId AND v.version = :version";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId);
            query.setParameter("apiId", apiId);
            query.setParameter("version", version);

            return (ApiVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public InputStream getApiDefinition(String orgId, String apiId, String apiVersion) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT v from ApiDefinitionBean v "
                                  + "JOIN v.apiVersion av "
                                  + "JOIN av.api api "
                                  + "WHERE api.id = :apiId "
                                  + "AND av.version = :apiVersion";
            TypedQuery<ApiDefinitionBean> query = entityManager.createQuery(jpql, ApiDefinitionBean.class)
                    .setParameter("apiId", apiId)
                    .setParameter("apiVersion", apiVersion);
            ApiDefinitionBean apiDef = query.getSingleResult();
            return new ByteArrayInputStream(apiDef.getData());
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ApiVersionSummaryBean> getApiVersions(String orgId, String apiId)
            throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
                    String jpql =
                      "SELECT v "
                    + "  FROM ApiVersionBean v"
                    + "  JOIN v.api s"
                    + "  JOIN s.organization o"
                    + " WHERE o.id = :orgId"
                    + "  AND s.id = :apiId"
                    + " ORDER BY v.createdOn DESC";
            TypedQuery<ApiVersionBean> query = entityManager.createQuery(jpql, ApiVersionBean.class);
            query.setMaxResults(500);
            query.setParameter("orgId", orgId);
            query.setParameter("apiId", apiId);

            List<ApiVersionBean> apiVersions = query.getResultList();
            return apiVersions.stream()
                    .map(apiMapper::toSummary)
                    .collect(Collectors.toList());
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    @Override
    public List<ApiBean> getApisByTagNameAndValue(String tagKey, String tagValue) {
        TypedQuery<ApiBean> query = getActiveEntityManager()
                .createQuery(
                        "SELECT avb FROM ApiBean avb "
                                + "JOIN avb.tags tag "
                                + "WHERE tag.key = :key "
                                + "AND tag.value = :value",
                        ApiBean.class
                )
                .setParameter("key", tagKey)
                .setParameter("value", tagValue);

        return query.getResultList();
    }

    @Override
    public List<ApiBean> getApisByTagName(String tagKey) {
        TypedQuery<ApiBean> query = getActiveEntityManager()
                .createQuery(
                        "SELECT ab FROM ApiBean ab "
                                + "JOIN ab.tags tag "
                                + "WHERE tag.key = :key",
                        ApiBean.class
                )
                .setParameter("key", tagKey);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ApiPlanSummaryBean> getApiVersionPlans(String organizationId, String apiId,
            String version) throws StorageException {
        List<ApiPlanSummaryBean> plans = new ArrayList<>();

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
                summary.setRequiresApproval(spb.isRequiresApproval());
                summary.setExposeInPortal(spb.isExposeInPortal());
                plans.add(summary);
            }
        }
        return plans;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ContractSummaryBean> getContracts(String organizationId, String apiId,
            String version, int page, int pageSize) throws StorageException {
        int start = (page - 1) * pageSize;

        try {
            EntityManager entityManager = getActiveEntityManager();
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
                    " ORDER BY sorg.id, api.id ASC";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", organizationId);
            query.setParameter("apiId", apiId);
            query.setParameter("version", version);
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
                csb.setStatus(contractBean.getStatus());
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
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientVersionBean getClientVersion(String orgId, String clientId, String version)
            throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT v from ClientVersionBean v JOIN v.client a JOIN a.organization o WHERE o.id = :orgId AND a.id = :clientId AND v.version = :version";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId);
            query.setParameter("clientId", clientId);
            query.setParameter("version", version);

            return (ClientVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ClientVersionSummaryBean> getClientVersions(String orgId, String clientId)
            throws StorageException {

        try {
            EntityManager entityManager = getActiveEntityManager();
                    String jpql =
                      "SELECT v"
                    + "  FROM ClientVersionBean v"
                    + "  JOIN v.client a"
                    + "  JOIN a.organization o"
                    + " WHERE o.id = :orgId"
                    + "   AND a.id = :clientId"
                    + " ORDER BY v.createdOn DESC";
            Query query = entityManager.createQuery(jpql);
            query.setMaxResults(500);
            query.setParameter("orgId", orgId);
            query.setParameter("clientId", clientId);
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
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ContractSummaryBean> getClientContracts(String organizationId, String clientId,
            String version) throws StorageException {
        try {
            return getClientContractsInternal(organizationId, clientId, version);
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
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
            csb.setStatus(contractBean.getStatus());
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
     * {@inheritDoc}
     */
    @Override
    public ApiRegistryBean getApiRegistry(String organizationId, String clientId, String version)
            throws StorageException {
        ApiRegistryBean rval = new ApiRegistryBean();

        try {
            EntityManager entityManager = getActiveEntityManager();
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
            query.setParameter("orgId", organizationId);
            query.setParameter("clientId", clientId);
            query.setParameter("version", version);

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
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
        return rval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrg(String orgId) throws StorageException {
        Set<String> orgIds = new HashSet<>();
        orgIds.add(orgId);
        return getPlansInOrgs(orgIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrgs(Set<String> orgIds) throws StorageException {
        List<PlanSummaryBean> rval = new ArrayList<>();
        if (orgIds == null || orgIds.isEmpty()) {
            return rval;
        }
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT p FROM PlanBean p JOIN p.organization o WHERE o.id IN :orgs ORDER BY p.id ASC";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgs", orgIds);
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
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlanVersionBean getPlanVersion(String orgId, String planId, String version)
            throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            String jpql = "SELECT v from PlanVersionBean v JOIN v.plan p JOIN p.organization o WHERE o.id = :orgId AND p.id = :planId AND v.version = :version";
            Query query = entityManager.createQuery(jpql);
            query.setParameter("orgId", orgId);
            query.setParameter("planId", planId);
            query.setParameter("version", version);

            return (PlanVersionBean) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PlanVersionSummaryBean> getPlanVersions(String orgId, String planId) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
                    String jpql = "SELECT v from PlanVersionBean v" +
                          "  JOIN v.plan p" +
                          "  JOIN p.organization o" +
                          " WHERE o.id = :orgId" +
                          "   AND p.id = :planId" +
                          " ORDER BY v.createdOn DESC";
            Query query = entityManager.createQuery(jpql);
            query.setMaxResults(500);
            query.setParameter("orgId", orgId);
            query.setParameter("planId", planId);
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
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PolicySummaryBean> getPolicies(String organizationId, String entityId, String version,
            PolicyType type) throws StorageException {
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
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxPolicyOrderIndex(String organizationId, String entityId, String entityVersion,
            PolicyType type) throws StorageException {
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        criteria.addFilter("organizationId", organizationId, SearchCriteriaFilterOperator.eq);
        criteria.addFilter("entityId", entityId, SearchCriteriaFilterOperator.eq);
        criteria.addFilter("entityVersion", entityVersion, SearchCriteriaFilterOperator.eq);
        criteria.addFilter("type", type.name(), SearchCriteriaFilterOperator.eq);
        criteria.setOrder("orderIndex", false);
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
     * {@inheritDoc}
     */
    @Override
    public List<PolicyDefinitionSummaryBean> listPluginPolicyDefs(Long pluginId) throws StorageException {
        try {
            String sql = ""
                 + "SELECT pd.id, pd.policy_impl, pd.name, pd.description, pd.icon, pd.plugin_id, pd.form_type"
                 + "  FROM policydefs pd"
                 + " WHERE pd.plugin_id = :pluginId"
                 + " ORDER BY pd.name ASC";

            return getJdbi().withHandle(handle ->
                 handle.createQuery(sql)
                       .bind("pluginId", pluginId)
                       .mapToBean(PolicyDefinitionSummaryBean.class)
                       .list()
            );
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createUser(UserBean user) throws StorageException {
        super.create(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserBean getUser(String userId) throws StorageException {
        return super.get(userId, UserBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(UserBean user) throws StorageException {
        super.update(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException {
        return super.find(criteria, UserBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createRole(RoleBean role) throws StorageException {
        super.create(role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRole(RoleBean role) throws StorageException {
        super.update(role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRole(RoleBean role) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();

            RoleBean prole = get(role.getId(), RoleBean.class);

            // First delete all memberships in this role
            entityManager.createQuery(
                            "DELETE from RoleMembershipBean m "
                                    + "WHERE m.roleId = :roleId")
                    .setParameter("roleId", role.getId())
                    .executeUpdate();
            // Then delete the role itself.
            super.delete(prole);
        } catch (Throwable t) {
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleBean getRole(String roleId) throws StorageException {
        return getRoleInternal(roleId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) throws StorageException {
        return super.find(criteria, RoleBean.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createMembership(RoleMembershipBean membership) throws StorageException {
        super.create(membership);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleMembershipBean getMembership(String userId, String roleId, String organizationId) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            TypedQuery<RoleMembershipBean> query = entityManager.createQuery(
                            "SELECT m FROM RoleMembershipBean m "
                                    + "WHERE m.roleId = :roleId "
                                    + "AND m.userId = :userId "
                                    + "AND m.organizationId = :orgId", RoleMembershipBean.class)
                    .setParameter("roleId", roleId)
                    .setParameter("userId", userId)
                    .setParameter("orgId", organizationId);
            return query.getResultStream().findFirst().orElse(null);
        } catch (Throwable t) {
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            entityManager.createQuery(
                            "DELETE FROM RoleMembershipBean m "
                                    + "WHERE m.roleId = :roleId "
                                    + "AND m.userId = :userId "
                                    + "AND m.organizationId = :orgId")
                    .setParameter("roleId", roleId)
                    .setParameter("userId", userId)
                    .setParameter("orgId", organizationId)
                    .executeUpdate();
        } catch (Throwable t) {
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMemberships(String userId, String organizationId) throws StorageException {
        try {
            EntityManager entityManager = getActiveEntityManager();
            entityManager.createQuery(
                            "DELETE FROM RoleMembershipBean m "
                                    + "WHERE m.userId = :userId "
                                    + "AND m.organizationId = :orgId")
                    .setParameter("userId", userId)
                    .setParameter("orgId", organizationId)
                    .executeUpdate();
        } catch (Throwable t) {
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException {
        Set<RoleMembershipBean> memberships = new HashSet<>();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(builder.equal(from.get("userId"), userId));
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            memberships.addAll(resultList);
            return memberships;
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId, String organizationId) throws StorageException {
        Set<RoleMembershipBean> memberships = new HashSet<>();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(
                    builder.equal(from.get("userId"), userId),
                    builder.equal(from.get("organizationId"), organizationId) );
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            memberships.addAll(resultList);
            return memberships;
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException {
        Set<RoleMembershipBean> memberships = new HashSet<>();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(builder.equal(from.get("organizationId"), organizationId));
            TypedQuery<RoleMembershipBean> typedQuery = entityManager.createQuery(criteriaQuery);
            List<RoleMembershipBean> resultList = typedQuery.getResultList();
            memberships.addAll(resultList);
            return memberships;
        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<PermissionBean> getPermissions(String userId) throws StorageException {
        Set<PermissionBean> permissions = new HashSet<>();
        try {
            EntityManager entityManager = getActiveEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaQuery<RoleMembershipBean> criteriaQuery = builder.createQuery(RoleMembershipBean.class);
            Root<RoleMembershipBean> from = criteriaQuery.from(RoleMembershipBean.class);
            criteriaQuery.where(builder.equal(from.get("userId"), userId));
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
            LOGGER.error(t.getMessage(), t);
            throw new StorageException(t);
        }
    }

    /**
     * @param roleId the role id
     * @return a role by id
     * @throws StorageException
     */
    protected RoleBean getRoleInternal(String roleId) throws StorageException {
        return super.get(roleId, RoleBean.class);
    }

    @Override
    public Iterator<OrganizationBean> getAllOrganizations() throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jqpl = "SELECT b FROM OrganizationBean b ORDER BY b.id ASC";
        Query query = entityManager.createQuery(jqpl);

        return super.getAll(OrganizationBean.class, query);
    }

    /**
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
    @Override
    public Iterator<ContractBean> getAllContracts(String organizationId, String clientId, String version)
            throws StorageException {
        EntityManager entityManager = getActiveEntityManager();
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
        query.setParameter("orgId", organizationId);
        query.setParameter("clientId", clientId);
        query.setParameter("version", version);

        return getAll(ContractBean.class, query);
    }

    /**
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
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
     * {@inheritDoc}
     */
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

        Query query = entityManager.createQuery(jpql)
            .setParameter("orgId", organizationId)
            .setParameter("apiId", apiId);
            //.setHint(QueryHints.READ_ONLY, true);

        return super.getAll(ApiVersionBean.class, query);
    }

    @Override
    public Iterator<GatewayBean> getAllGateways() throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b FROM GatewayBean b ORDER BY b.id ASC";

        Query query = entityManager.createQuery(jpql);
        return super.getAll(GatewayBean.class, query);
    }

    @Override
    public Iterator<AuditEntryBean> getAllAuditEntries(String orgId) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM AuditEntryBean b "
                + "WHERE b.organizationId = :orgId "
                + "ORDER BY b.id ASC"; //$NON-NLS-1$

        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", orgId);
        return super.getAll(AuditEntryBean.class, query);
    }

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
     * {@inheritDoc}
     */
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

    @Override
    public Iterator<RoleMembershipBean> getAllMemberships(String orgId) throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql =
                "SELECT b "
                + "FROM RoleMembershipBean b "
                + "WHERE b.organizationId = :orgId "
                + "ORDER BY b.id ASC"; //$NON-NLS-1$

        Query query = entityManager.createQuery(jpql);
        query.setParameter("orgId", orgId);
        return super.getAll(RoleMembershipBean.class, query);
    }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<DeveloperBean> getDevelopers() throws StorageException {
        EntityManager entityManager = getActiveEntityManager();

        String jpql = "SELECT db FROM DeveloperBean db ORDER BY db.id ASC";

        Query query = entityManager.createQuery(jpql);
        return super.getAll(DeveloperBean.class, query);
    }

    @Override
    public Iterator<ContractBean> getAllContracts(OrganizationBean organizationBean, int lim) throws StorageException {
        String jpql =
                  " SELECT contractBean "
                + " FROM ContractBean contractBean "
                // Api
                + " JOIN contractBean.api apiVersion "
                + " JOIN apiVersion.api api "
                + " JOIN api.organization apiOrg "
                // Client
                + " JOIN contractBean.client clientVersion "
                + " JOIN clientVersion.client client "
                + " JOIN api.organization clientOrg "
                // Check API status
                + " WHERE (apiOrg.id = :orgId AND apiVersion.status = :apiStatus)"
                // Check In-Org ClientApp status
                + " OR (clientOrg.id = :orgId AND clientVersion.status = :clientStatus)";

        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", organizationBean.getId());
        query.setParameter("clientStatus", ClientStatus.Registered);
        query.setParameter("apiStatus", ApiStatus.Published);

        if (lim > 0) {
            query.setMaxResults(lim);
        }
        return super.getAll(ContractBean.class, query);
    }

    @Override
    public Iterator<ClientVersionBean> getAllClientVersions(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAllClientVersions(organizationBean, null, lim);
    }

    @Override
    public Iterator<ClientVersionBean> getAllClientVersions(OrganizationBean organizationBean, ClientStatus status, int lim) throws StorageException {
        String jpql = "SELECT v "
                + " FROM ClientVersionBean v "
                + " JOIN v.client c "
                + " JOIN c.organization o "
                + "WHERE o.id = :orgId ";

        if (status != null) {
            jpql += String.format(" AND v.status = '%s' ", status.name());
        }

        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", organizationBean.getId());
        if (lim > 0) {
            query.setMaxResults(lim);
        }
        return super.getAll(ClientVersionBean.class, query);
    }

    @Override
    public Iterator<ApiVersionBean> getAllApiVersions(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAllApiVersions(organizationBean, null, lim);
    }

    @Override
    public Iterator<ApiVersionBean> getAllApiVersions(OrganizationBean organizationBean, ApiStatus status, int lim) throws StorageException {
        String jpql = "SELECT v "
                + "  FROM ApiVersionBean v "
                + "  JOIN v.api a"
                + "  JOIN a.organization o "
                + " WHERE o.id = :orgId ";

        if (status != null) {
            jpql += String.format(" AND v.status = '%s' ", status.name());
        }

        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", organizationBean.getId());

        if (lim > 0) {
            query.setMaxResults(lim);
        }
        return super.getAll(ApiVersionBean.class, query);
    }

    @Override
    public Iterator<PlanVersionBean> getAllPlanVersions(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAllPlanVersions(organizationBean, null, lim);
    }

    @Override
    public Iterator<PlanVersionBean> getAllPlanVersions(OrganizationBean organizationBean, PlanStatus status, int lim) throws StorageException {
        String jpql = "SELECT v "
                + "  FROM PlanVersionBean v "
                + "  JOIN v.plan p "
                + "  JOIN p.organization o "
                + " WHERE o.id = :orgId ";

        if (status != null) {
            jpql += String.format(" AND v.status = '%s' ", status.name());
        }

        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", organizationBean.getId());
        if (lim > 0) {
            query.setMaxResults(lim);
        }
        return super.getAll(PlanVersionBean.class, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<ApiVersionBean> getAllPublicApiVersions() throws StorageException {
        String jpql = "SELECT v "
            + "  FROM ApiVersionBean v "
            + "  WHERE v.publicAPI = true";
        // TODO(msavy): consider adding pagination
        Query query = getActiveEntityManager().createQuery(jpql);
        getActiveEntityManager().createQuery(jpql);
        return super.getAll(ApiVersionBean.class, query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserBean> getAllUsersWithPermission(PermissionType permission, String orgName) throws StorageException {
        return getJdbi().withHandle(h -> h.createQuery(
             "SELECT DISTINCT u.* "
                     + "FROM USERS u "
                     + "     INNER JOIN MEMBERSHIPS m ON m.USER_ID = u.USERNAME "
                     + "     INNER JOIN PERMISSIONS p ON p.ROLE_ID = m.ROLE_ID "
                     + "     INNER JOIN USERS ON u.USERNAME = m.USER_ID "
                     + "WHERE p.PERMISSIONS = :permissionType "
                     + "  AND m.ORG_ID = :orgName")
               .bind("permissionType", permission.ordinal())
               .bind("orgName", orgName)
               .mapToBean(UserBean.class)
               .list()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserBean> getAllUsersWithRole(String roleName, String orgName) throws StorageException {
        return getJdbi().withHandle(h -> h.createQuery(
             "SELECT DISTINCT u.*  "
                     + "FROM USERS u  "
                     + "     INNER JOIN MEMBERSHIPS m ON m.USER_ID = u.USERNAME "
                     + "     INNER JOIN USERS ON u.USERNAME = m.USER_ID "
                     + "WHERE m.ORG_ID = :orgName "
                     + "  AND m.ROLE_ID = :roleName")
              .bind("orgName", orgName)
              .bind("roleName", roleName)
              .mapToBean(UserBean.class)
              .list()
        );
    }

    @Override
    public void flush() {
        getActiveEntityManager().flush();
    }

    private void deleteAllPolicies(OrganizationBean organizationBean) throws StorageException {
        deleteAllPolicies(organizationBean, null);
    }

    private void deleteAllPolicies(ApiBean apiBean) throws StorageException {
        deleteAllPolicies(apiBean.getOrganization(), apiBean.getId());
    }

    private void deleteAllPolicies(ClientBean clientBean) throws StorageException {
        deleteAllPolicies(clientBean.getOrganization(), clientBean.getId());
    }

    private void deleteAllPolicies(OrganizationBean organizationBean, String entityId) throws StorageException {
        String jpql =
            "DELETE FROM PolicyBean b "
          + "      WHERE b.organizationId = :orgId ";

        if (entityId != null) {
            jpql += String.format("AND b.entityId = '%s'", entityId);
        }

        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", organizationBean.getId());
    }

    private void deleteAllMemberships(OrganizationBean organizationBean) throws StorageException {
        String jpql = "DELETE FROM RoleMembershipBean b "
                    + "   WHERE b.organizationId = :orgId ";

        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", organizationBean.getId());
        query.executeUpdate();
    }

    private void deleteAllAuditEntries(PlanBean plan) throws StorageException {
        deleteAllAuditEntries(plan.getOrganization(), AuditEntityType.Plan, plan.getId());
    }

    private void deleteAllAuditEntries(ApiBean apiBean) throws StorageException {
        deleteAllAuditEntries(apiBean.getOrganization(), AuditEntityType.Api, apiBean.getId());
    }

    private void deleteAllAuditEntries(ClientBean clientBean) throws StorageException {
        deleteAllAuditEntries(clientBean.getOrganization(), AuditEntityType.Client, clientBean.getId());
    }

    private void deleteAllAuditEntries(OrganizationBean organizationBean) throws StorageException {
        deleteAllAuditEntries(organizationBean, null, null);
    }

    private void deleteAllAuditEntries(OrganizationBean organizationBean, AuditEntityType entityType, String entityId) throws StorageException {
        String jpql =
            "DELETE FROM AuditEntryBean b "
          + "   WHERE b.organizationId = :orgId ";

        if (entityId != null && entityType != null) {
            jpql += String.format("AND b.entityId = '%s' AND b.entityType = '%s' ", entityId, entityType.name());
        }

        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", organizationBean.getId());
        query.executeUpdate();
    }

    private void deleteAllContracts(ApiBean apiBean) throws StorageException {
        String jpql =
            "DELETE FROM ContractBean deleteBean " +
            "   WHERE deleteBean IN ( " +
            "       SELECT b " +
            "           FROM ContractBean b " +
            "           JOIN b.api apiVersion " +
            "           JOIN apiVersion.api api " +
            "           JOIN api.organization o " +
            "       WHERE o.id = :orgId " +
            "       AND api.id = :apiId " +
            "   )";
        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", apiBean.getOrganization().getId());
        query.setParameter("apiId", apiBean.getId());
        query.executeUpdate();
    }

    private void deleteAllContracts(ClientBean clientBean) throws StorageException {
        String jpql =
            "DELETE FROM ContractBean deleteBean " +
            "   WHERE deleteBean IN ( " +
            "       SELECT b " +
            "           FROM ContractBean b " +
            "           JOIN b.client clientVersion " +
            "           JOIN clientVersion.client client " +
            "           JOIN client.organization o " +
            "       WHERE o.id = :orgId " +
            "       AND client.id = :clientId " +
            "   )";
        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", clientBean.getOrganization().getId());
        query.setParameter("clientId", clientBean.getId());
        query.executeUpdate();
    }

    private void deleteAllContracts(OrganizationBean organizationBean) throws StorageException {
        String jpql =
            "DELETE FROM ContractBean deleteBean " +
                "   WHERE deleteBean IN ( " +
                "       SELECT b " +
                "           FROM ContractBean b " +
                "           JOIN b.api apiVersion " +
                "           JOIN apiVersion.api api " +
                "           JOIN api.organization o " +
                "       WHERE o.id = :orgId " +
                "   )";

        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", organizationBean.getId());
        query.executeUpdate();
    }

    private <T> void remove(T entity) throws StorageException {
        EntityManager em = getActiveEntityManager();
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public void deleteAllPlans(OrganizationBean organizationBean) throws StorageException {
        deleteAllPlanVersions(organizationBean);

        String jpql = "DELETE FROM PlanBean p "
                + "     WHERE p.organization.id = :orgId ";

        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", organizationBean.getId());
        query.executeUpdate();
    }

    private void deleteAllPlanVersions(OrganizationBean organizationBean) throws StorageException {
        String jpql = "DELETE FROM PlanVersionBean deleteBean "
            + " WHERE deleteBean IN ("
            + "SELECT v"
            + "  FROM PlanVersionBean v "
            + "  JOIN v.plan p "
            + "  JOIN p.organization o "
            + " WHERE o.id = :orgId "
            + ")";

        Query query = getActiveEntityManager().createQuery(jpql);
        query.setParameter("orgId", organizationBean.getId());
        query.executeUpdate();
    }

}
