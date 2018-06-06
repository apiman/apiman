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
package io.apiman.manager.api.es;

import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.Holder;
import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.manager.api.beans.apis.ApiBean;
import io.apiman.manager.api.beans.apis.ApiGatewayBean;
import io.apiman.manager.api.beans.apis.ApiPlanBean;
import io.apiman.manager.api.beans.apis.ApiStatus;
import io.apiman.manager.api.beans.apis.ApiVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntityType;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
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
import io.apiman.manager.api.beans.search.OrderByBean;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
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
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.logging.ApimanLogger;
import io.apiman.manager.api.core.util.PolicyTemplateUtil;
import io.apiman.manager.api.es.beans.ApiDefinitionBean;
import io.apiman.manager.api.es.beans.PoliciesBean;
import io.apiman.manager.api.es.util.FilterBuilder;
import io.apiman.manager.api.es.util.FilterBuilders;
import io.apiman.manager.api.es.util.QueryBuilder;
import io.apiman.manager.api.es.util.QueryBuilders;
import io.apiman.manager.api.es.util.SearchSourceBuilder;
import io.apiman.manager.api.es.util.SortOrder;
import io.apiman.manager.api.es.util.TermsQueryBuilder;
import io.apiman.manager.api.es.util.XContentBuilder;
import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.cluster.Health;
import io.searchbox.core.Delete;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import io.searchbox.core.SearchScroll;
import io.searchbox.core.SearchScroll.Builder;
import io.searchbox.core.search.sort.Sort;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.params.Parameters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

/**
 * An implementation of the API Manager persistence layer that uses git to store
 * the entities.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class EsStorage implements IStorage, IStorageQuery {

    private static final String DEFAULT_INDEX_NAME = "apiman_manager"; //$NON-NLS-1$

    private static int guidCounter = 100;

    @Inject @ApimanLogger(EsStorage.class)
    private IApimanLogger logger;

    @Inject @Named("storage")
    JestClient esClient;
    @Inject IDataEncrypter encrypter;
    @PostConstruct
    public void postConstruct() {
        // Kick the encrypter, causing it to be loaded/resolved in CDI
        encrypter.encrypt("", new DataEncryptionContext()); //$NON-NLS-1$
    }

    private String indexName = DEFAULT_INDEX_NAME;

    /**
     * Constructor.
     */
    public EsStorage() {
    }

    /**
     * Called to initialize the storage.
     */
    @Override
    @SuppressWarnings("nls")
    public void initialize() {
        try {
            ScheduledExecutorService schedulerService = Executors.newSingleThreadScheduledExecutor();
            CountDownLatch cdl = new CountDownLatch(1);
            Holder<Exception> exception = new Holder<>();

            ScheduledFuture<?> sched = schedulerService.scheduleAtFixedRate(() -> {
                logger.info("Polling for Elasticsearch...");
                try {
                    esClient.execute(new Health.Builder().build());
                    cdl.countDown();
                } catch (IOException e) {
                    logger.info("Unable to reach Elasticsearch. Will continue polling.");
                    //System.out.println("Result of polling", e);
                    exception.setValue(e);
                }
            },
            0, // Start immediately
            1, // Poll every 1 seconds
            TimeUnit.SECONDS);

            cdl.await(30, TimeUnit.SECONDS); // Max wait 30 seconds
            sched.cancel(true);

            // CDL > 0 means we never successfully hit the health endpoint.
            if (exception.getValue() != null && cdl.getCount() > 0) {
                throw exception.getValue();
            }
            // TODO Do we need a loop to wait for all nodes to join the cluster?
            Action<JestResult> action = new IndicesExists.Builder(getIndexName()).build();
            JestResult result = esClient.execute(action);
            if (! result.isSucceeded()) {
                createIndex(getIndexName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param indexName
     * @throws Exception
     */
    private void createIndex(String indexName) throws Exception {
        URL settings = getClass().getResource("index-settings.json"); //$NON-NLS-1$
        String source = IOUtils.toString(settings);
        JestResult response = esClient.execute(new CreateIndex.Builder(indexName).settings(source).build());
        if (!response.isSucceeded()) {
            throw new StorageException("Failed to create index " + indexName + ": " + response.getErrorMessage()); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#beginTx()
     */
    @Override
    public void beginTx() throws StorageException {
        // No Transaction support for ES
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#commitTx()
     */
    @Override
    public void commitTx() throws StorageException {
        // No Transaction support for ES
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#rollbackTx()
     */
    @Override
    public void rollbackTx() {
        // No Transaction support for ES
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void createOrganization(OrganizationBean organization) throws StorageException {
        indexEntity("organization", organization.getId(), EsMarshalling.marshall(organization), true); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createClient(io.apiman.manager.api.beans.clients.ClientBean)
     */
    @Override
    public void createClient(ClientBean client) throws StorageException {
        indexEntity("client", id(client.getOrganization().getId(), client.getId()), EsMarshalling.marshall(client)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createClientVersion(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public void createClientVersion(ClientVersionBean version) throws StorageException {
        ClientBean client = version.getClient();
        String id = id(client.getOrganization().getId(), client.getId(), version.getVersion());
        indexEntity("clientVersion", id, EsMarshalling.marshall(version)); //$NON-NLS-1$
        PoliciesBean policies = PoliciesBean.from(PolicyType.Client, client.getOrganization().getId(),
                client.getId(), version.getVersion());
        indexEntity("clientPolicies", id, EsMarshalling.marshall(policies)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void createContract(ContractBean contract) throws StorageException {
        List<ContractSummaryBean> contracts = getClientContracts(contract.getClient().getClient().getOrganization().getId(),
                contract.getClient().getClient().getId(), contract.getClient().getVersion());
        for (ContractSummaryBean csb : contracts) {
            if (csb.getApiOrganizationId().equals(contract.getApi().getApi().getOrganization().getId()) &&
                    csb.getApiId().equals(contract.getApi().getApi().getId()) &&
                    csb.getApiVersion().equals(contract.getApi().getVersion()))
                {
                    throw new StorageException("Error creating contract: duplicate contract detected."); //$NON-NLS-1$
                }
        }
        contract.setId(generateGuid());
        indexEntity("contract", String.valueOf(contract.getId()), EsMarshalling.marshall(contract), true); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApi(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    public void createApi(ApiBean api) throws StorageException {
        indexEntity("api", id(api.getOrganization().getId(), api.getId()), EsMarshalling.marshall(api)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void createApiVersion(ApiVersionBean version) throws StorageException {
        ApiBean api = version.getApi();
        String id = id(api.getOrganization().getId(), api.getId(), version.getVersion());
        indexEntity("apiVersion", id, EsMarshalling.marshall(version)); //$NON-NLS-1$
        PoliciesBean policies = PoliciesBean.from(PolicyType.Api, api.getOrganization().getId(),
                api.getId(), version.getVersion());
        indexEntity("apiPolicies", id, EsMarshalling.marshall(policies)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void createPlan(PlanBean plan) throws StorageException {
        indexEntity("plan", id(plan.getOrganization().getId(), plan.getId()), EsMarshalling.marshall(plan)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void createPlanVersion(PlanVersionBean version) throws StorageException {
        PlanBean plan = version.getPlan();
        String id = id(plan.getOrganization().getId(), plan.getId(), version.getVersion());
        indexEntity("planVersion", id, EsMarshalling.marshall(version)); //$NON-NLS-1$
        PoliciesBean policies = PoliciesBean.from(PolicyType.Plan, plan.getOrganization().getId(),
                plan.getId(), version.getVersion());
        indexEntity("planPolicies", id, EsMarshalling.marshall(policies)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void createPolicy(PolicyBean policy) throws StorageException {
        String docType = getPoliciesDocType(policy.getType());
        String id = id(policy.getOrganizationId(), policy.getEntityId(), policy.getEntityVersion());
        Map<String, Object> source = getEntity(docType, id);
        if (source == null) {
            throw new StorageException("Failed to create policy (missing PoliciesBean)."); //$NON-NLS-1$
        }
        PoliciesBean policies = EsMarshalling.unmarshallPolicies(source);
        policy.setId(generateGuid());
        policies.getPolicies().add(policy);
        orderPolicies(policies);
        updateEntity(docType, id, EsMarshalling.marshall(policies));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#reorderPolicies(io.apiman.manager.api.beans.policies.PolicyType, java.lang.String, java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public void reorderPolicies(PolicyType type, String organizationId, String entityId,
            String entityVersion, List<Long> newOrder) throws StorageException {
        String docType = getPoliciesDocType(type);
        String pid = id(organizationId, entityId, entityVersion);
        Map<String, Object> source = getEntity(docType, pid);
        if (source == null) {
            return;
        }
        PoliciesBean policiesBean = EsMarshalling.unmarshallPolicies(source);
        List<PolicyBean> policies = policiesBean.getPolicies();
        List<PolicyBean> reordered = new ArrayList<>(policies.size());
        for (Long policyId : newOrder) {
            ListIterator<PolicyBean> iterator = policies.listIterator();
            while (iterator.hasNext()) {
                PolicyBean policyBean = iterator.next();
                if (policyBean.getId().equals(policyId)) {
                    iterator.remove();
                    reordered.add(policyBean);
                    break;
                }
            }
        }
        // Make sure we don't stealth-delete any policies.  Put anything
        // remaining at the end of the list.
        for (PolicyBean policyBean : policies) {
            reordered.add(policyBean);
        }
        policiesBean.setPolicies(reordered);
        updateEntity(docType, pid, EsMarshalling.marshall(policiesBean));
    }

    /**
     * Set the order index of all policies.
     * @param policies
     */
    private void orderPolicies(PoliciesBean policies) {
        int idx = 1;
        for (PolicyBean policy : policies.getPolicies()) {
            policy.setOrderIndex(idx++);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void createGateway(GatewayBean gateway) throws StorageException {
        indexEntity("gateway", gateway.getId(), EsMarshalling.marshall(gateway)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void createPlugin(PluginBean plugin) throws StorageException {
        plugin.setId(generateGuid());
        indexEntity("plugin", String.valueOf(plugin.getId()), EsMarshalling.marshall(plugin), true); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createDownload(io.apiman.manager.api.beans.download.DownloadBean)
     */
    @Override
    public void createDownload(DownloadBean download) throws StorageException {
        indexEntity("download", download.getId(), EsMarshalling.marshall(download)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void createPolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        indexEntity("policyDef", policyDef.getId(), EsMarshalling.marshall(policyDef)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void createRole(RoleBean role) throws StorageException {
        indexEntity("role", role.getId(), EsMarshalling.marshall(role)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createAuditEntry(io.apiman.manager.api.beans.audit.AuditEntryBean)
     */
    @Override
    public void createAuditEntry(AuditEntryBean entry) throws StorageException {
        if (entry == null) {
            return;
        }
        entry.setId(generateGuid());
        indexEntity("auditEntry", String.valueOf(entry.getId()), EsMarshalling.marshall(entry)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void updateOrganization(OrganizationBean organization) throws StorageException {
        updateEntity("organization", organization.getId(), EsMarshalling.marshall(organization)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateClient(io.apiman.manager.api.beans.clients.ClientBean)
     */
    @Override
    public void updateClient(ClientBean client) throws StorageException {
        updateEntity("client", id(client.getOrganization().getId(), client.getId()), EsMarshalling.marshall(client)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateClientVersion(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public void updateClientVersion(ClientVersionBean version) throws StorageException {
        ClientBean client = version.getClient();
        updateEntity("clientVersion", id(client.getOrganization().getId(), client.getId(), version.getVersion()),  //$NON-NLS-1$
                EsMarshalling.marshall(version));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApi(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    public void updateApi(ApiBean api) throws StorageException {
        updateEntity("api", id(api.getOrganization().getId(), api.getId()), EsMarshalling.marshall(api)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void updateApiVersion(ApiVersionBean version) throws StorageException {
        ApiBean api = version.getApi();
        updateEntity("apiVersion", id(api.getOrganization().getId(), api.getId(), version.getVersion()),  //$NON-NLS-1$
                EsMarshalling.marshall(version));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApiDefinition(io.apiman.manager.api.beans.apis.ApiVersionBean, java.io.InputStream)
     */
    @Override
    public void updateApiDefinition(ApiVersionBean version, InputStream definitionStream)
            throws StorageException {
        InputStream apiDefinition = null;
        try {
            String id = id(version.getApi().getOrganization().getId(), version.getApi().getId(), version.getVersion()) + ":def"; //$NON-NLS-1$
            apiDefinition = getApiDefinition(version);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(definitionStream, baos);
            String data = Base64.encodeBase64String(baos.toByteArray());
            ApiDefinitionBean definition = new ApiDefinitionBean();
            definition.setData(data);
            if (apiDefinition == null) {
                indexEntity("apiDefinition", id, EsMarshalling.marshall(definition)); //$NON-NLS-1$
            } else {
                updateEntity("apiDefinition", id, EsMarshalling.marshall(definition)); //$NON-NLS-1$
            }
        } catch (IOException e) {
            throw new StorageException(e);
        } finally {
            IOUtils.closeQuietly(apiDefinition);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void updatePlan(PlanBean plan) throws StorageException {
        updateEntity("plan", id(plan.getOrganization().getId(), plan.getId()), EsMarshalling.marshall(plan)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void updatePlanVersion(PlanVersionBean version) throws StorageException {
        PlanBean plan = version.getPlan();
        updateEntity("planVersion", id(plan.getOrganization().getId(), plan.getId(), version.getVersion()),  //$NON-NLS-1$
                EsMarshalling.marshall(version));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void updatePolicy(PolicyBean policy) throws StorageException {
        String docType = getPoliciesDocType(policy.getType());
        String pid = id(policy.getOrganizationId(), policy.getEntityId(), policy.getEntityVersion());
        Map<String, Object> source = getEntity(docType, pid);
        if (source == null) {
            throw new StorageException("Policy not found."); //$NON-NLS-1$
        }
        PoliciesBean policies = EsMarshalling.unmarshallPolicies(source);
        List<PolicyBean> policyBeans = policies.getPolicies();
        boolean found = false;
        if (policyBeans != null) {
            for (PolicyBean policyBean : policyBeans) {
                if (policyBean.getId().equals(policy.getId())) {
                    policyBean.setConfiguration(policy.getConfiguration());
                    policyBean.setModifiedBy(policy.getModifiedBy());
                    policyBean.setModifiedOn(policy.getModifiedOn());
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            updateEntity(docType, pid, EsMarshalling.marshall(policies));
        } else {
            throw new StorageException("Policy not found."); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void updateGateway(GatewayBean gateway) throws StorageException {
        updateEntity("gateway", gateway.getId(), EsMarshalling.marshall(gateway)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void updatePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        updateEntity("policyDef", policyDef.getId(), EsMarshalling.marshall(policyDef)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void updatePlugin(PluginBean pluginBean) throws StorageException {
        updateEntity("plugin", String.valueOf(pluginBean.getId()), EsMarshalling.marshall(pluginBean)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void updateRole(RoleBean role) throws StorageException {
        updateEntity("role", role.getId(), EsMarshalling.marshall(role)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    @SuppressWarnings("nls")
    public void deleteOrganization(OrganizationBean organization) throws StorageException {
        try {
            String orgId = organization.getId().replace('"', '_');

            QueryBuilder qb =
                FilterBuilders.boolFilter(
                    FilterBuilders.shouldFilter(
                        FilterBuilders.termFilter("organizationId", orgId),
                        FilterBuilders.termFilter("clientOrganizationId", orgId),
                        FilterBuilders.termFilter("apiOrganizationId", orgId)
                    )
                );

            SearchSourceBuilder query = new SearchSourceBuilder().query(qb);
            DeleteByQuery deleteByQuery = new DeleteByQuery.Builder(query.string()).addIndex(getIndexName())
                    .addType("api")
                    .addType("apiPolicies")
                    .addType("apiVersion")
                    .addType("auditEntry")
                    .addType("client")
                    .addType("clientPolicies")
                    .addType("clientVersion")
                    .addType("contract")
                    .addType("plan")
                    .addType("planPolicies")
                    .addType("planVersion")
                    .addType("roleMembership")
                    .build();

            JestResult response = esClient.execute(deleteByQuery);
            if (!response.isSucceeded()) {
                throw new StorageException(response.getErrorMessage());
            }
            deleteEntity("organization", orgId); //$NON-NLS-1$
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteClient(io.apiman.manager.api.beans.clients.ClientBean)
     */
    @Override
    @SuppressWarnings("nls")
    public void deleteClient(ClientBean client) throws StorageException {
        String clientId = client.getId().replace('"', '_');
        String orgId = client.getOrganization().getId().replace('"', '_');

        QueryBuilder qb =
            QueryBuilders.query(
                FilterBuilders.boolFilter(
                    FilterBuilders.filter(
                        FilterBuilders.boolFilter(
                            FilterBuilders.shouldFilter(
                                FilterBuilders.termFilter("clientOrganizationId", orgId),
                                FilterBuilders.termFilter("organizationId", orgId)
                            )
                        ),
                        FilterBuilders.boolFilter(
                            FilterBuilders.shouldFilter(
                                FilterBuilders.boolFilter(
                                    FilterBuilders.filter(
                                        FilterBuilders.termFilter("entityId", clientId),
                                        FilterBuilders.termFilter("entityType", AuditEntityType.Client.name())
                                    )
                                ),
                                FilterBuilders.boolFilter(
                                    FilterBuilders.filter(
                                        FilterBuilders.termFilter("entityId", clientId),
                                        FilterBuilders.termFilter("type", AuditEntityType.Client.name())
                                    )
                                ),
                                FilterBuilders.termFilter("clientId", clientId)
                            )
                        )
                    )
                )
            );

        try {
            DeleteByQuery deleteByQuery = new DeleteByQuery.Builder(qb.string()).addIndex(getIndexName())
                    .addType("auditEntry")
                    .addType("client")
                    .addType("clientVersion")
                    .addType("clientPolicies")
                    .addType("contract")
                    .build();

            JestResult response = esClient.execute(deleteByQuery);
            if (!response.isSucceeded()) {
                throw new StorageException(response.getErrorMessage());
            }
        } catch (Exception e) {
            throw new StorageException(e);
        }
        deleteEntity("client", id(orgId, clientId)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteClientVersion(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public void deleteClientVersion(ClientVersionBean version) throws StorageException {
        ClientBean client = version.getClient();
        deleteEntity("clientVersion", id(client.getOrganization().getId(), client.getId(), version.getVersion())); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void deleteContract(ContractBean contract) throws StorageException {
        deleteEntity("contract", String.valueOf(contract.getId())); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApi(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    @SuppressWarnings("nls")
    public void deleteApi(ApiBean api) throws StorageException {
        String apiId = api.getId().replace('"', '_');
        String orgId = api.getOrganization().getId().replace('"', '_');

        QueryBuilder qb =
                QueryBuilders.query(
                    FilterBuilders.boolFilter(
                        FilterBuilders.filter(
                            FilterBuilders.boolFilter(
                                FilterBuilders.shouldFilter(
                                    FilterBuilders.termFilter("apiOrganizationId", orgId),
                                    FilterBuilders.termFilter("organizationId", orgId)
                                )
                            ),
                            FilterBuilders.boolFilter(
                                FilterBuilders.shouldFilter(
                                    FilterBuilders.boolFilter(
                                        FilterBuilders.filter(
                                            FilterBuilders.termFilter("entityId", apiId),
                                            FilterBuilders.termFilter("entityType", AuditEntityType.Api.name())
                                        )
                                    ),
                                    FilterBuilders.boolFilter(
                                        FilterBuilders.filter(
                                            FilterBuilders.termFilter("entityId", apiId),
                                            FilterBuilders.termFilter("type", AuditEntityType.Api.name())
                                        )
                                    ),
                                    FilterBuilders.termFilter("apiId", apiId)
                                )
                            )
                        )
                    )
                );

        try {
            DeleteByQuery deleteByQuery = new DeleteByQuery.Builder(qb.string()).addIndex(getIndexName())
                    .addType("auditEntry")
                    .addType("api")
                    .addType("apiVersion")
                    .addType("apiPolicies")
                    .addType("contract")
                    .build();

            JestResult response = esClient.execute(deleteByQuery);
            if (!response.isSucceeded()) {
                throw new StorageException(response.getErrorMessage());
            }
        } catch (Exception e) {
            throw new StorageException(e);
        }
        deleteEntity("api", id(orgId, apiId)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void deleteApiVersion(ApiVersionBean version) throws StorageException {
        deleteApiDefinition(version);
        ApiBean api = version.getApi();
        String id = id(api.getOrganization().getId(), api.getId(), version.getVersion());
        deleteEntity("apiVersion", id); //$NON-NLS-1$
        deleteEntity("apiPolicies", id); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApiDefinition(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void deleteApiDefinition(ApiVersionBean version) throws StorageException {
        String id = id(version.getApi().getOrganization().getId(), version.getApi().getId(), version.getVersion()) + ":def"; //$NON-NLS-1$
        deleteEntity("apiDefinition", id); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    @SuppressWarnings("nls")
    public void deletePlan(PlanBean plan) throws StorageException {
        String planId = plan.getId().replace('"', '_');
        String orgId = plan.getOrganization().getId().replace('"', '_');

        QueryBuilder qb =
            FilterBuilders.boolFilter(
                FilterBuilders.shouldFilter(
                    FilterBuilders.boolFilter(
                        FilterBuilders.filter(
                           FilterBuilders.termFilter("entityId", planId),
                           FilterBuilders.termFilter("entityType", AuditEntityType.Plan.name()),
                           FilterBuilders.termFilter("organizationId", orgId)
                        )
                    ),
                    FilterBuilders.boolFilter(
                        FilterBuilders.filter(
                           FilterBuilders.termFilter("planId", planId),
                           FilterBuilders.termFilter("organizationId", orgId)
                        )
                    ),
                    FilterBuilders.boolFilter(
                        FilterBuilders.filter(
                           FilterBuilders.termFilter("entityId", planId),
                           FilterBuilders.termFilter("type", AuditEntityType.Plan.name())
                        )
                    )
                )
            );

        try {
            DeleteByQuery deleteByQuery = new DeleteByQuery.Builder("{\"query\":"+qb.string()+"}") // TODO
                    .addIndex(getIndexName())
                    .addType("auditEntry")
                    .addType("planVersion")
                    .build();

            JestResult response = esClient.execute(deleteByQuery);
            if (!response.isSucceeded()) {
                throw new StorageException(response.getErrorMessage());
            }
        } catch (Exception e) {
            throw new StorageException(e);
        }
        deleteEntity("plan", id(plan.getOrganization().getId(), plan.getId())); //$NON-NLS-1$
    }

    public @interface Foo {
        String maxo() default "/";
    }


    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void deletePlanVersion(PlanVersionBean version) throws StorageException {
        PlanBean plan = version.getPlan();
        deleteEntity("planVersion", id(plan.getOrganization().getId(), plan.getId(), version.getVersion())); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void deletePolicy(PolicyBean policy) throws StorageException {
        String docType = getPoliciesDocType(policy.getType());
        String pid = id(policy.getOrganizationId(), policy.getEntityId(), policy.getEntityVersion());
        Map<String, Object> source = getEntity(docType, pid);
        if (source == null) {
            throw new StorageException("Policy not found."); //$NON-NLS-1$
        }
        PoliciesBean policies = EsMarshalling.unmarshallPolicies(source);
        if (policies == null) throw new StorageException("Policy not found."); //$NON-NLS-1$
        List<PolicyBean> policyBeans = policies.getPolicies();
        boolean found = false;
        if (policyBeans != null) {
            for (PolicyBean policyBean : policyBeans) {
                if (policyBean.getId().equals(policy.getId())) {
                    policies.getPolicies().remove(policyBean);
                    found = true;
                    break;
                }
            }
        }
        if (found) {
            updateEntity(docType, pid, EsMarshalling.marshall(policies));
        } else {
            throw new StorageException("Policy not found."); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteGateway(io.apiman.manager.api.beans.gateways.GatewayBean)
     */
    @Override
    public void deleteGateway(GatewayBean gateway) throws StorageException {
        deleteEntity("gateway", gateway.getId()); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void deletePlugin(PluginBean plugin) throws StorageException {
        deleteEntity("plugin", String.valueOf(plugin.getId())); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteDownload(io.apiman.manager.api.beans.download.DownloadBean)
     */
    @Override
    public void deleteDownload(DownloadBean download) throws StorageException {
        deleteEntity("download", download.getId()); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        deleteEntity("policyDef", policyDef.getId()); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see io.apiman.manager.api.core.IStorage#deleteRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void deleteRole(RoleBean role) throws StorageException {
        deleteEntity("role", role.getId()); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getOrganization(java.lang.String)
     */
    @Override
    public OrganizationBean getOrganization(String id) throws StorageException {
        Map<String, Object> source = getEntity("organization", id); //$NON-NLS-1$
        return EsMarshalling.unmarshallOrganization(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getClient(java.lang.String, java.lang.String)
     */
    @Override
    public ClientBean getClient(String organizationId, String id) throws StorageException {
        Map<String, Object> source = getEntity("client", id(organizationId, id)); //$NON-NLS-1$
        if (source == null) {
            return null;
        }
        ClientBean bean = EsMarshalling.unmarshallClient(source);
        bean.setOrganization(getOrganization(organizationId));
        return bean;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getClientVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ClientVersionBean getClientVersion(String organizationId, String clientId,
            String version) throws StorageException {
        Map<String, Object> source = getEntity("clientVersion", id(organizationId, clientId, version)); //$NON-NLS-1$
        if (source == null) {
            return null;
        }
        ClientVersionBean bean = EsMarshalling.unmarshallClientVersion(source);
        bean.setClient(getClient(organizationId, clientId));
        return bean;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getContract(java.lang.Long)
     */
    @SuppressWarnings("nls")
    @Override
    public ContractBean getContract(Long id) throws StorageException {
        Map<String, Object> source = getEntity("contract", String.valueOf(id)); //$NON-NLS-1$
        ContractBean contract = EsMarshalling.unmarshallContract(source);
        if (contract == null) {
            return null;
        }
        String clientOrgId = (String) source.get("clientOrganizationId");
        String clientId = (String) source.get("clientId");
        String clientVersion = (String) source.get("clientVersion");
        String apiOrgId = (String) source.get("apiOrganizationId");
        String apiId = (String) source.get("apiId");
        String apiVersion = (String) source.get("apiVersion");
        String planId = (String) source.get("planId");
        String planVersion = (String) source.get("planVersion");
        ClientVersionBean avb = getClientVersion(clientOrgId, clientId, clientVersion);
        ApiVersionBean svb = getApiVersion(apiOrgId, apiId, apiVersion);
        PlanVersionBean pvb = getPlanVersion(apiOrgId, planId, planVersion);
        contract.setClient(avb);
        contract.setPlan(pvb);
        contract.setApi(svb);
        return contract;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApi(java.lang.String, java.lang.String)
     */
    @Override
    public ApiBean getApi(String organizationId, String id) throws StorageException {
        Map<String, Object> source = getEntity("api", id(organizationId, id)); //$NON-NLS-1$
        if (source == null) {
            return null;
        }
        ApiBean bean = EsMarshalling.unmarshallApi(source);
        bean.setOrganization(getOrganization(organizationId));
        return bean;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApiVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiVersionBean getApiVersion(String organizationId, String apiId, String version)
            throws StorageException {
        Map<String, Object> source = getEntity("apiVersion", id(organizationId, apiId, version)); //$NON-NLS-1$
        if (source == null) {
            return null;
        }
        ApiVersionBean bean = EsMarshalling.unmarshallApiVersion(source);
        bean.setApi(getApi(organizationId, apiId));
        return bean;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApiDefinition(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public InputStream getApiDefinition(ApiVersionBean version) throws StorageException {
        String id = id(version.getApi().getOrganization().getId(), version.getApi().getId(), version.getVersion()) + ":def"; //$NON-NLS-1$
        Map<String, Object> source = getEntity("apiDefinition", id); //$NON-NLS-1$
        if (source == null) {
            return null;
        }
        ApiDefinitionBean def = EsMarshalling.unmarshallApiDefinition(source);
        if (def == null) return null;
        String data = def.getData();
        return new ByteArrayInputStream(Base64.decodeBase64(data));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlan(java.lang.String, java.lang.String)
     */
    @Override
    public PlanBean getPlan(String organizationId, String id) throws StorageException {
        Map<String, Object> source = getEntity("plan", id(organizationId, id)); //$NON-NLS-1$
        if (source == null) {
            return null;
        }
        PlanBean bean = EsMarshalling.unmarshallPlan(source);
        bean.setOrganization(getOrganization(organizationId));
        return bean;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlanVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
            throws StorageException {
        Map<String, Object> source = getEntity("planVersion", id(organizationId, planId, version)); //$NON-NLS-1$
        if (source == null) {
            return null;
        }
        PlanVersionBean bean = EsMarshalling.unmarshallPlanVersion(source);
        bean.setPlan(getPlan(organizationId, planId));
        return bean;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPolicy(io.apiman.manager.api.beans.policies.PolicyType, java.lang.String, java.lang.String, java.lang.String, java.lang.Long)
     */
    @Override
    public PolicyBean getPolicy(PolicyType type, String organizationId, String entityId, String version,
            Long id) throws StorageException {
        String docType = getPoliciesDocType(type);
        String pid = id(organizationId, entityId, version);
        Map<String, Object> source = getEntity(docType, pid);
        if (source == null) {
            return null;
        }
        PoliciesBean policies = EsMarshalling.unmarshallPolicies(source);
        if (policies == null) return null;
        List<PolicyBean> policyBeans = policies.getPolicies();
        if (policyBeans != null) {
            for (PolicyBean policyBean : policyBeans) {
                if (policyBean.getId().equals(id)) {
                    PolicyDefinitionBean def = getPolicyDefinition(policyBean.getDefinition().getId());
                    policyBean.setDefinition(def);
                    return policyBean;
                }
            }
        }
        return null;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getGateway(java.lang.String)
     */
    @Override
    public GatewayBean getGateway(String id) throws StorageException {
        Map<String, Object> source = getEntity("gateway", id); //$NON-NLS-1$
        return EsMarshalling.unmarshallGateway(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getDownload(java.lang.String)
     */
    @Override
    public DownloadBean getDownload(String id) throws StorageException {
        Map<String, Object> source = getEntity("download", id); //$NON-NLS-1$
        return EsMarshalling.unmarshallDownload(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlugin(long)
     */
    @Override
    public PluginBean getPlugin(long id) throws StorageException {
        Map<String, Object> source = getEntity("plugin", String.valueOf(id)); //$NON-NLS-1$
        return EsMarshalling.unmarshallPlugin(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlugin(java.lang.String, java.lang.String)
     */
    @Override
    public PluginBean getPlugin(String groupId, String artifactId) throws StorageException {
        try {
            @SuppressWarnings("nls")
            QueryBuilder qb =
                FilterBuilders.boolFilter(
                        FilterBuilders.filter(
                            FilterBuilders.termFilter("groupId", groupId),
                            FilterBuilders.termFilter("artifactId", artifactId)
                        )
                );
            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(50);
            List<Hit<Map<String,Object>,Void>> hits = listEntities("plugin", builder); //$NON-NLS-1$
            if (hits.size() == 1) {
                Hit<Map<String,Object>,Void> hit = hits.iterator().next();
                return EsMarshalling.unmarshallPlugin(hit.source);
            }
            return null;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPolicyDefinition(java.lang.String)
     */
    @Override
    public PolicyDefinitionBean getPolicyDefinition(String id) throws StorageException {
        Map<String, Object> source = getEntity("policyDef", id); //$NON-NLS-1$
        return EsMarshalling.unmarshallPolicyDefinition(source);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getRole(java.lang.String)
     */
    @Override
    public RoleBean getRole(String id) throws StorageException {
        Map<String, Object> source = getEntity("role", id); //$NON-NLS-1$
        return EsMarshalling.unmarshallRole(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPlugins()
     */
    @Override
    @SuppressWarnings("nls")
    public List<PluginSummaryBean> listPlugins() throws StorageException {
        String[] fields = {"id", "artifactId", "groupId", "version", "classifier", "type", "name",
            "description", "createdBy", "createdOn"};
        QueryBuilder query = FilterBuilders.notExistOrFalse("deleted");
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .fetchSource(fields, null)
                .query(query)
                .sort("name.raw", SortOrder.ASC)
                .size(200);
        List<Hit<Map<String,Object>,Void>> hits = listEntities("plugin", builder);
        List<PluginSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            PluginSummaryBean bean = EsMarshalling.unmarshallPluginSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listGateways()
     */
    @Override
    public List<GatewaySummaryBean> listGateways() throws StorageException {
        @SuppressWarnings("nls")
        String[] fields = {"id", "name", "description","type"};
        SearchSourceBuilder builder = new SearchSourceBuilder().fetchSource(fields, null).sort("name.raw", SortOrder.ASC).size(100); //$NON-NLS-1$
        List<Hit<Map<String,Object>,Void>> hits = listEntities("gateway", builder); //$NON-NLS-1$
        List<GatewaySummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            GatewaySummaryBean bean = EsMarshalling.unmarshallGatewaySummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findOrganizations(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<OrganizationSummaryBean> findOrganizations(SearchCriteriaBean criteria)
            throws StorageException {
        return find(criteria, "organization", new IUnmarshaller<OrganizationSummaryBean>() { //$NON-NLS-1$
            @Override
            public OrganizationSummaryBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallOrganizationSummary(source);
            }
        });
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findClients(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ClientSummaryBean> findClients(SearchCriteriaBean criteria)
            throws StorageException {
        return find(criteria, "client", new IUnmarshaller<ClientSummaryBean>() { //$NON-NLS-1$
            @Override
            public ClientSummaryBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallClientSummary(source);
            }
        });
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findApis(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ApiSummaryBean> findApis(SearchCriteriaBean criteria)
            throws StorageException {
        return find(criteria, "api", new IUnmarshaller<ApiSummaryBean>() { //$NON-NLS-1$
            @Override
            public ApiSummaryBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallApiSummary(source);
            }
        });
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findPlans(java.lang.String, io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<PlanSummaryBean> findPlans(String organizationId, SearchCriteriaBean criteria)
            throws StorageException {
        criteria.addFilter("organizationId", organizationId, SearchCriteriaFilterOperator.eq); //$NON-NLS-1$
        return find(criteria, "plan", new IUnmarshaller<PlanSummaryBean>() { //$NON-NLS-1$
            @Override
            public PlanSummaryBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallPlanSummary(source);
            }
        });
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#auditEntity(java.lang.String, java.lang.String, java.lang.String, java.lang.Class, io.apiman.manager.api.beans.search.PagingBean)
     */
    @Override
    public <T> SearchResultsBean<AuditEntryBean> auditEntity(String organizationId, String entityId,
            String entityVersion, Class<T> type, PagingBean paging) throws StorageException {
        SearchCriteriaBean criteria = new SearchCriteriaBean();
        if (paging != null) {
            criteria.setPaging(paging);
        } else {
            criteria.setPage(1);
            criteria.setPageSize(20);
        }
        criteria.setOrder("createdOn", false); //$NON-NLS-1$
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

        return find(criteria, "auditEntry", new IUnmarshaller<AuditEntryBean>() { //$NON-NLS-1$
            @Override
            public AuditEntryBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallAuditEntry(source);
            }
        });
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

        return find(criteria, "auditEntry", new IUnmarshaller<AuditEntryBean>() { //$NON-NLS-1$
            @Override
            public AuditEntryBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallAuditEntry(source);
            }
        });
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getOrgs(java.util.Set)
     */
    @Override
    public List<OrganizationSummaryBean> getOrgs(Set<String> organizationIds) throws StorageException {
        List<OrganizationSummaryBean> orgs = new ArrayList<>();
        if (organizationIds == null || organizationIds.isEmpty()) {
            return orgs;
        }
        @SuppressWarnings("nls")
        QueryBuilder query =  FilterBuilders.termsFilter("id", organizationIds.toArray(new String[organizationIds.size()]));
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort("name.raw", SortOrder.ASC)
                .query(query)
                .size(500);
        List<Hit<Map<String,Object>,Void>> hits = listEntities("organization", builder); //$NON-NLS-1$
        List<OrganizationSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            OrganizationSummaryBean bean = EsMarshalling.unmarshallOrganizationSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getClientsInOrgs(java.util.Set)
     */
    @Override
    public List<ClientSummaryBean> getClientsInOrgs(Set<String> organizationIds) throws StorageException {
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort("organizationName.raw", SortOrder.ASC)
                .sort("name.raw", SortOrder.ASC)
                .size(500);
        TermsQueryBuilder query = QueryBuilders.termsQuery("organizationId", organizationIds.toArray(new String[organizationIds.size()])); //$NON-NLS-1$
        builder.query(query);
        List<Hit<Map<String,Object>,Void>> hits = listEntities("client", builder); //$NON-NLS-1$
        List<ClientSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            ClientSummaryBean bean = EsMarshalling.unmarshallClientSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getClientsInOrg(java.lang.String)
     */
    @Override
    public List<ClientSummaryBean> getClientsInOrg(String organizationId) throws StorageException {
        Set<String> orgs = new HashSet<>();
        orgs.add(organizationId);
        return getClientsInOrgs(orgs);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getClientVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ClientVersionSummaryBean> getClientVersions(String organizationId,
            String clientId) throws StorageException {
        @SuppressWarnings("nls")
        QueryBuilder query =
            FilterBuilders.boolFilter(
                FilterBuilders.filter(
                    FilterBuilders.termFilter("organizationId", organizationId),
                    FilterBuilders.termFilter("clientId", clientId)
                )
            );
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort("createdOn", SortOrder.DESC)
                .query(query)
                .size(500);
        List<Hit<Map<String,Object>,Void>> hits = listEntities("clientVersion", builder); //$NON-NLS-1$
        List<ClientVersionSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            ClientVersionSummaryBean bean = EsMarshalling.unmarshallClientVersionSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getClientContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ContractSummaryBean> getClientContracts(String organizationId, String clientId,
            String version) throws StorageException {
        @SuppressWarnings("nls")
        QueryBuilder query =
            FilterBuilders.boolFilter(
                FilterBuilders.filter(
                    FilterBuilders.termFilter("clientOrganizationId", organizationId),
                    FilterBuilders.termFilter("clientId", clientId),
                    FilterBuilders.termFilter("clientVersion", version)
                )
            );
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder().sort("apiOrganizationId", SortOrder.ASC)
                .sort("apiId", SortOrder.ASC).query(query).size(500);
        List<Hit<Map<String,Object>,Void>> hits = listEntities("contract", builder); //$NON-NLS-1$
        List<ContractSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            ContractSummaryBean bean = EsMarshalling.unmarshallContractSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApiRegistry(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiRegistryBean getApiRegistry(String organizationId, String clientId, String version)
            throws StorageException {
        @SuppressWarnings("nls")
        QueryBuilder query =
          FilterBuilders.boolFilter(
              FilterBuilders.filter(
                  FilterBuilders.termFilter("clientOrganizationId", organizationId),
                  FilterBuilders.termFilter("clientId", clientId),
                  FilterBuilders.termFilter("clientVersion", version)
              )
          );
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder().sort("id", SortOrder.ASC).query(query)
                .size(500);
        List<Hit<Map<String,Object>,Void>> hits = listEntities("contract", builder); //$NON-NLS-1$
        ApiRegistryBean registry = new ApiRegistryBean();
        for (Hit<Map<String,Object>,Void> hit : hits) {
            ApiEntryBean bean = EsMarshalling.unmarshallApiEntry(hit.source);
            ApiVersionBean svb = getApiVersion(bean.getApiOrgId(), bean.getApiId(), bean.getApiVersion());
            Set<ApiGatewayBean> gateways = svb.getGateways();
            if (gateways != null && !gateways.isEmpty()) {
                ApiGatewayBean sgb = gateways.iterator().next();
                bean.setGatewayId(sgb.getGatewayId());
            }
            registry.getApis().add(bean);
        }
        return registry;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApisInOrgs(java.util.Set)
     */
    @Override
    public List<ApiSummaryBean> getApisInOrgs(Set<String> organizationIds) throws StorageException {
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort("organizationName.raw", SortOrder.ASC)
                .sort("name.raw", SortOrder.ASC)
                .size(500);
        TermsQueryBuilder query = QueryBuilders.termsQuery("organizationId", organizationIds.toArray(new String[organizationIds.size()])); //$NON-NLS-1$
        builder.query(query);

        List<Hit<Map<String,Object>,Void>> hits = listEntities("api", builder); //$NON-NLS-1$
        List<ApiSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            ApiSummaryBean bean = EsMarshalling.unmarshallApiSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApisInOrg(java.lang.String)
     */
    @Override
    public List<ApiSummaryBean> getApisInOrg(String organizationId) throws StorageException {
        Set<String> orgs = new HashSet<>();
        orgs.add(organizationId);
        return getApisInOrgs(orgs);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApiVersions(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings("nls")
    public List<ApiVersionSummaryBean> getApiVersions(String organizationId, String apiId)
            throws StorageException {
        QueryBuilder query =
            FilterBuilders.boolFilter(
                FilterBuilders.filter(
                    FilterBuilders.termFilter("organizationId", organizationId),
                    FilterBuilders.termFilter("apiId", apiId)
            )
        );

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort("createdOn", SortOrder.DESC)
                .query(query)
                .size(500);

        List<Hit<Map<String,Object>,Void>> hits = listEntities("apiVersion", builder);
        List<ApiVersionSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            ApiVersionSummaryBean bean = EsMarshalling.unmarshallApiVersionSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApiVersionPlans(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ApiPlanSummaryBean> getApiVersionPlans(String organizationId, String apiId,
            String version) throws StorageException {
        List<ApiPlanSummaryBean> rval = new ArrayList<>();
        ApiVersionBean versionBean = getApiVersion(organizationId, apiId, version);
        if (versionBean != null) {
            Set<ApiPlanBean> plans = versionBean.getPlans();
            if (plans != null) {
                for (ApiPlanBean spb : plans) {
                    PlanBean planBean = getPlan(organizationId, spb.getPlanId());
                    ApiPlanSummaryBean plan = new ApiPlanSummaryBean();
                    plan.setPlanId(spb.getPlanId());
                    plan.setVersion(spb.getVersion());
                    plan.setPlanName(planBean.getName());
                    plan.setPlanDescription(planBean.getDescription());
                    rval.add(plan);
                }
            }
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlansInOrgs(java.util.Set)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrgs(Set<String> organizationIds) throws StorageException {
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort("organizationName.raw", SortOrder.ASC)
                .sort("name.raw", SortOrder.ASC)
                .size(500);
        TermsQueryBuilder query = QueryBuilders.termsQuery("organizationId", organizationIds.toArray(new String[organizationIds.size()])); //$NON-NLS-1$
        builder.query(query);
        List<Hit<Map<String,Object>,Void>> hits = listEntities("plan", builder); //$NON-NLS-1$
        List<PlanSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            PlanSummaryBean bean = EsMarshalling.unmarshallPlanSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlansInOrg(java.lang.String)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrg(String organizationId) throws StorageException {
        Set<String> orgs = new HashSet<>();
        orgs.add(organizationId);
        return getPlansInOrgs(orgs);
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlanVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<PlanVersionSummaryBean> getPlanVersions(String organizationId, String planId)
            throws StorageException {
        @SuppressWarnings("nls")
        QueryBuilder query =
            FilterBuilders.boolFilter(
                FilterBuilders.filter(
                    FilterBuilders.termFilter("organizationId", organizationId),
                    FilterBuilders.termFilter("planId", planId)
                )
            );
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort("createdOn", SortOrder.DESC)
                .query(query)
                .size(500);
        List<Hit<Map<String,Object>,Void>> hits = listEntities("planVersion", builder); //$NON-NLS-1$
        List<PlanVersionSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            PlanVersionSummaryBean bean = EsMarshalling.unmarshallPlanVersionSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @Override
    public List<PolicySummaryBean> getPolicies(String organizationId, String entityId, String version,
            PolicyType type) throws StorageException {
        try {
            String docType = getPoliciesDocType(type);
            String pid = id(organizationId, entityId, version);
            List<PolicySummaryBean> rval = new ArrayList<>();
            Map<String, Object> source = getEntity(docType, pid);
            if (source == null) {
                return rval;
            }
            PoliciesBean policies = EsMarshalling.unmarshallPolicies(source);
            if (policies == null) return rval;
            List<PolicyBean> policyBeans = policies.getPolicies();
            if (policyBeans != null) {
                for (PolicyBean policyBean : policyBeans) {
                    PolicyDefinitionBean def = getPolicyDefinition(policyBean.getDefinition().getId());
                    policyBean.setDefinition(def);
                    PolicyTemplateUtil.generatePolicyDescription(policyBean);
                    PolicySummaryBean psb = new PolicySummaryBean();
                    psb.setCreatedBy(policyBean.getCreatedBy());
                    psb.setCreatedOn(policyBean.getCreatedOn());
                    psb.setDescription(policyBean.getDescription());
                    psb.setIcon(def.getIcon());
                    psb.setId(policyBean.getId());
                    psb.setName(policyBean.getName());
                    psb.setPolicyDefinitionId(def.getId());
                    rval.add(psb);
                }
            }
            return rval;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPolicyDefinitions()
     */
    @Override
    @SuppressWarnings("nls")
    public List<PolicyDefinitionSummaryBean> listPolicyDefinitions() throws StorageException {
        String[] fields = {"id", "policyImpl", "name", "description", "icon", "pluginId", "formType"};

        QueryBuilder query = FilterBuilders.notExistOrFalse("deleted");

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .fetchSource(fields, null)
                .query(query)
                .sort("name.raw", SortOrder.ASC).size(100); //$NON-NLS-1$
        List<Hit<Map<String,Object>,Void>> hits = listEntities("policyDef", builder); //$NON-NLS-1$
        List<PolicyDefinitionSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            PolicyDefinitionSummaryBean bean = EsMarshalling.unmarshallPolicyDefinitionSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getContracts(java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @Override
    @SuppressWarnings("nls")
    public List<ContractSummaryBean> getContracts(String organizationId, String apiId,
            String version, int page, int pageSize) throws StorageException {
        QueryBuilder query =
            FilterBuilders.boolFilter(
                    FilterBuilders.filter(
                            FilterBuilders.termFilter("apiOrganizationId", organizationId),
                            FilterBuilders.termFilter("apiId", apiId),
                            FilterBuilders.termFilter("apiVersion", version)
                    )
            );

        SearchSourceBuilder builder = new SearchSourceBuilder().sort("clientOrganizationId", SortOrder.ASC)
                .sort("clientId", SortOrder.ASC).query(query).size(500);
        List<Hit<Map<String,Object>,Void>> hits = listEntities("contract", builder); //$NON-NLS-1$
        List<ContractSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            ContractSummaryBean bean = EsMarshalling.unmarshallContractSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getMaxPolicyOrderIndex(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @Override
    public int getMaxPolicyOrderIndex(String organizationId, String entityId, String entityVersion,
            PolicyType type) throws StorageException {
        // We'll figure this out later, when adding a policy.
        return -1;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPluginPolicyDefs(java.lang.Long)
     */
    @Override
    public List<PolicyDefinitionSummaryBean> listPluginPolicyDefs(Long pluginId) throws StorageException {
        @SuppressWarnings("nls")
        QueryBuilder qb = FilterBuilders.termFilter("pluginId", pluginId);
        @SuppressWarnings("nls")
        String[] fields = {"id", "policyImpl", "name", "description", "icon", "pluginId", "formType"};
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .fetchSource(fields, null)
                .query(qb)
                .sort("name.raw", SortOrder.ASC).size(100); //$NON-NLS-1$
        List<Hit<Map<String,Object>,Void>> hits = listEntities("policyDef", builder); //$NON-NLS-1$
        List<PolicyDefinitionSummaryBean> rval = new ArrayList<>(hits.size());
        for (Hit<Map<String,Object>,Void> hit : hits) {
            PolicyDefinitionSummaryBean bean = EsMarshalling.unmarshallPolicyDefinitionSummary(hit.source);
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void createUser(UserBean user) throws StorageException {
        indexEntity("user", user.getUsername(), EsMarshalling.marshall(user)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getUser(java.lang.String)
     */
    @Override
    public UserBean getUser(String userId) throws StorageException {
        Map<String, Object> source = getEntity("user", userId); //$NON-NLS-1$
        return EsMarshalling.unmarshallUser(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void updateUser(UserBean user) throws StorageException {
        updateEntity("user", user.getUsername(), EsMarshalling.marshall(user)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findUsers(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException {
        return find(criteria, "user",  new IUnmarshaller<UserBean>() { //$NON-NLS-1$
            @Override
            public UserBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallUser(source);
            }
        });
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findRoles(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<RoleBean> findRoles(SearchCriteriaBean criteria) throws StorageException {
        return find(criteria, "role", new IUnmarshaller<RoleBean>() { //$NON-NLS-1$
            @Override
            public RoleBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallRole(source);
            }
        });
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createMembership(io.apiman.manager.api.beans.idm.RoleMembershipBean)
     */
    @Override
    public void createMembership(RoleMembershipBean membership) throws StorageException {
        membership.setId(generateGuid());
        String id = id(membership.getOrganizationId(), membership.getUserId(), membership.getRoleId());
        indexEntity("roleMembership", id, EsMarshalling.marshall(membership), true); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public RoleMembershipBean getMembership(String userId, String roleId, String organizationId) throws StorageException {
        String id = id(organizationId, userId, roleId);
        Map<String, Object> source = getEntity("roleMembership", id); //$NON-NLS-1$
        if (source == null) {
            return null;
        } else {
            return EsMarshalling.unmarshallRoleMembership(source);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException {
        String id = id(organizationId, userId, roleId);
        deleteEntity("roleMembership", id); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteMemberships(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings("nls")
    public void deleteMemberships(String userId, String organizationId) throws StorageException {
        QueryBuilder query =
            FilterBuilders.boolFilter(
                    FilterBuilders.filter(
                            FilterBuilders.termFilter("organizationId", organizationId),
                            FilterBuilders.termFilter("userId", userId)
                    )
            );
        try {
            String string = query.string();
            // Workaround for bug in FilteredQueryBuilder which does not (yet) wrap
            // the JSON in a query element
            if (string.indexOf("query") < 0 || string.indexOf("query") > 7) {
                string = "{ \"query\" : " + string + "}";
            }
            DeleteByQuery deleteByQuery = new DeleteByQuery.Builder(string).addIndex(getIndexName())
                    .addType("roleMembership").build();
            JestResult response = esClient.execute(deleteByQuery);
            if (!response.isSucceeded()) {
                throw new StorageException(response.getErrorMessage());
            }
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getUserMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException {
        try {
            @SuppressWarnings("nls")
            QueryBuilder qb = FilterBuilders.termFilter("userId", userId);
            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(500);
            List<Hit<Map<String,Object>,Void>> hits = listEntities("roleMembership", builder); //$NON-NLS-1$
            Set<RoleMembershipBean> rval = new HashSet<>();
            for (Hit<Map<String,Object>,Void> hit : hits) {
                RoleMembershipBean roleMembership = EsMarshalling.unmarshallRoleMembership(hit.source);
                rval.add(roleMembership);
            }
            return rval;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getUserMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId, String organizationId)
            throws StorageException {
        try {
            @SuppressWarnings("nls")
            QueryBuilder qb =
                FilterBuilders.boolFilter(
                        FilterBuilders.filter(
                                FilterBuilders.termFilter("userId", userId),
                                FilterBuilders.termFilter("organizationId", organizationId)
                            )
                );
            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(500);
            List<Hit<Map<String,Object>,Void>> hits = listEntities("roleMembership", builder); //$NON-NLS-1$
            Set<RoleMembershipBean> rval = new HashSet<>();
            for (Hit<Map<String,Object>,Void> hit : hits) {
                RoleMembershipBean roleMembership = EsMarshalling.unmarshallRoleMembership(hit.source);
                rval.add(roleMembership);
            }
            return rval;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getOrgMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException {
        try {
            @SuppressWarnings("nls")
            QueryBuilder qb = FilterBuilders.termFilter("organizationId", organizationId);
            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(500);
            List<Hit<Map<String,Object>,Void>> hits = listEntities("roleMembership", builder); //$NON-NLS-1$
            Set<RoleMembershipBean> rval = new HashSet<>();
            for (Hit<Map<String,Object>,Void> hit : hits) {
                RoleMembershipBean roleMembership = EsMarshalling.unmarshallRoleMembership(hit.source);
                rval.add(roleMembership);
            }
            return rval;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPermissions(java.lang.String)
     */
    @Override
    public Set<PermissionBean> getPermissions(String userId) throws StorageException {
        try {
            @SuppressWarnings("nls")
            QueryBuilder qb = FilterBuilders.termFilter("userId", userId);
            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(500);
            List<Hit<Map<String,Object>,Void>> hits = listEntities("roleMembership", builder); //$NON-NLS-1$
            Set<PermissionBean> rval = new HashSet<>(hits.size());
            if (!hits.isEmpty()) {
                for (Hit<Map<String,Object>,Void> hit : hits) {
                    Map<String, Object> source = hit.source;
                    String roleId = String.valueOf(source.get("roleId")); //$NON-NLS-1$
                    String qualifier = String.valueOf(source.get("organizationId")); //$NON-NLS-1$
                    RoleBean role = getRole(roleId);
                    if (role != null) {
                        for (PermissionType permission : role.getPermissions()) {
                            PermissionBean p = new PermissionBean();
                            p.setName(permission);
                            p.setOrganizationId(qualifier);
                            rval.add(p);
                        }
                    }
                }
            }
            return rval;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * Indexes an entity.
     * @param type
     * @param id
     * @param sourceEntity
     * @throws StorageException
     */
    private void indexEntity(String type, String id, XContentBuilder sourceEntity) throws StorageException {
        indexEntity(type, id, sourceEntity, false);
    }

    /**
     * Indexes an entity.
     * @param type
     * @param id
     * @param sourceEntity
     * @param refresh true if the operation should wait for a refresh before it returns
     * @throws StorageException
     */
    @SuppressWarnings("nls")
    private void indexEntity(String type, String id, XContentBuilder sourceEntity, boolean refresh)
            throws StorageException {
        try {
            String json = sourceEntity.string();
            JestResult response = esClient.execute(new Index.Builder(json).refresh(refresh).index(getIndexName())
                    .setParameter(Parameters.OP_TYPE, "create").type(type).id(id).build());
            if (!response.isSucceeded()) {
                throw new StorageException("Failed to index document " + id + " of type " + type + ": " + response.getErrorMessage());
            }
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * Gets an entity.  Callers must unmarshal the resulting map.
     * @param type
     * @param id
     * @throws StorageException
     */
    private Map<String, Object> getEntity(String type, String id) throws StorageException {
        try {
            JestResult response = esClient.execute(new Get.Builder(getIndexName(), id).type(type).build());
            if (!response.isSucceeded()) {
                return null;
            }
            return response.getSourceAsObject(Map.class);
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * Returns a list of entities.
     * @param type
     * @param searchSourceBuilder
     * @throws StorageException
     */
    private List<Hit<Map<String, Object>, Void>> listEntities(String type,
            SearchSourceBuilder searchSourceBuilder) throws StorageException {
        try {
            String query = searchSourceBuilder.string();
            Search search = new Search.Builder(query).addIndex(getIndexName()).addType(type).build();
            SearchResult response = esClient.execute(search);
            @SuppressWarnings({ "rawtypes", "unchecked" })
            List<Hit<Map<String, Object>, Void>> thehits = (List) response.getHits(Map.class);
            return thehits;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * Deletes an entity.
     * @param type
     * @param id
     * @throws StorageException
     */
    private void deleteEntity(String type, String id) throws StorageException {
        try {
            JestResult response = esClient.execute(new Delete.Builder(id).index(getIndexName()).type(type).build());
            if (!response.isSucceeded()) {
                throw new StorageException("Document could not be deleted because it did not exist:" + response.getErrorMessage()); //$NON-NLS-1$
            }
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * Updates a single entity.
     * @param type
     * @param id
     * @param source
     * @throws StorageException
     */
    private void updateEntity(String type, String id, XContentBuilder source) throws StorageException {
        try {
            String doc = source.string();
            /* JestResult response = */esClient.execute(new Index.Builder(doc)
                    .setParameter(Parameters.OP_TYPE, "index").index(getIndexName()).type(type).id(id).build()); //$NON-NLS-1$
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * Finds entities using a generic search criteria bean.
     * @param criteria
     * @param type
     * @param unmarshaller
     * @throws StorageException
     */
    private <T> SearchResultsBean<T> find(SearchCriteriaBean criteria, String type,
            IUnmarshaller<T> unmarshaller) throws StorageException {
        try {
            SearchResultsBean<T> rval = new SearchResultsBean<>();

            // Set some default in the case that paging information was not included in the request.
            PagingBean paging = criteria.getPaging();
            if (paging == null) {
                paging = new PagingBean();
                paging.setPage(1);
                paging.setPageSize(20);
            }
            int page = paging.getPage();
            int pageSize = paging.getPageSize();
            int start = (page - 1) * pageSize;

            SearchSourceBuilder builder = new SearchSourceBuilder().size(pageSize).from(start).fetchSource(true);

            // Sort order
            OrderByBean orderBy = criteria.getOrderBy();
            if (orderBy != null) {
                String name = orderBy.getName();
                if (name.equals("name") || name.equals("fullName")) { //$NON-NLS-1$ //$NON-NLS-2$
                    name += ".raw"; //$NON-NLS-1$
                }
                if (orderBy.isAscending()) {
                    builder.sort(name, SortOrder.ASC);
                } else {
                    builder.sort(name, SortOrder.DESC);
                }
            }

            // Now process the filter criteria
            List<SearchCriteriaFilterBean> filters = criteria.getFilters();

            QueryBuilder q = null;

            if (filters != null && !filters.isEmpty()) {
                FilterBuilder andFilter = FilterBuilders.filter();
                int filterCount = 0;
                for (SearchCriteriaFilterBean filter : filters) {
                    String propertyName = filter.getName();
                    if (filter.getOperator() == SearchCriteriaFilterOperator.eq) {
                        andFilter.add(FilterBuilders.termFilter(propertyName, filter.getValue()));
                        filterCount++;
                    } else if (filter.getOperator() == SearchCriteriaFilterOperator.like) {
                        q = QueryBuilders.wildcardQuery(propertyName, filter.getValue().toLowerCase().replace('%', '*'));
                    } else if (filter.getOperator() == SearchCriteriaFilterOperator.bool_eq) {
                        andFilter.add(FilterBuilders.termFilter(propertyName, "true".equals(filter.getValue()))); //$NON-NLS-1$
                        filterCount++;
                    }
                    // TODO implement the other filter operators here!
                }

                if (filterCount > 0) {
                    q = FilterBuilders.boolFilter(andFilter);
                }
            }
            builder.query(q);

            String query = builder.string();
            Search search = new Search.Builder(query)
                    .addIndex(getIndexName())
                    .addType(type)
                    .build();
            SearchResult response = esClient.execute(search);
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<Hit<Map<String, Object>, Void>> thehits = (List) response.getHits(Map.class);

            rval.setTotalSize((int)(long)response.getTotal());
            for (Hit<Map<String,Object>,Void> hit : thehits) {
                Map<String, Object> sourceAsMap = hit.source;
                T bean = unmarshaller.unmarshal(sourceAsMap);
                rval.getBeans().add(bean);
            }
            return rval;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    /**
     * Generates a (hopefully) unique ID.  Mimics JPA's auto-generated long ID column.
     */
    private static synchronized Long generateGuid() {
        StringBuilder builder = new StringBuilder();
        builder.append(System.currentTimeMillis());
        builder.append(guidCounter++);
        // Reset the counter if it gets too high.  It's always a number
        // between 100 and 999 so that the # of digits in the guid is
        // always the same.
        if (guidCounter > 999) {
            guidCounter = 100;
        }
        return Long.parseLong(builder.toString());
    }

    /**
     * Returns the policies document type to use given the policy type.
     * @param type
     */
    private static String getPoliciesDocType(PolicyType type) {
        String docType = "planPolicies"; //$NON-NLS-1$
        if (type == PolicyType.Api) {
            docType = "apiPolicies"; //$NON-NLS-1$
        } else if (type == PolicyType.Client) {
            docType = "clientPolicies"; //$NON-NLS-1$
        }
        return docType;
    }

    /**
     * A composite ID created from an organization ID and entity ID.
     * @param organizationId
     * @param entityId
     */
    private static String id(String organizationId, String entityId) {
        return organizationId + ":" + entityId; //$NON-NLS-1$
    }

    /**
     * A composite ID created from an organization ID, entity ID, and version.
     * @param organizationId
     * @param entityId
     * @param version
     */
    private static String id(String organizationId, String entityId, String version) {
        return organizationId + ':' + entityId + ':' + version;
    }

    @Override
    public Iterator<OrganizationBean> getAllOrganizations() throws StorageException {
        return getAll("organization", new IUnmarshaller<OrganizationBean>() { //$NON-NLS-1$
            @Override
            public OrganizationBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallOrganization(source);
            }
        });
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPlans(java.lang.String)
     */
    @Override
    public Iterator<PlanBean> getAllPlans(String organizationId) throws StorageException {
        return getAll("plan", new IUnmarshaller<PlanBean>() { //$NON-NLS-1$
            @Override
            public PlanBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallPlan(source);
            }
        }, matchOrgQuery(organizationId));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllClients(java.lang.String)
     */
    @Override
    public Iterator<ClientBean> getAllClients(String organizationId) throws StorageException {
        return getAll("client", new IUnmarshaller<ClientBean>() { //$NON-NLS-1$
            @Override
            public ClientBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallClient(source);
            }
        }, matchOrgQuery(organizationId));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllApis(java.lang.String)
     */
    @Override
    public Iterator<ApiBean> getAllApis(String organizationId) throws StorageException {
        return getAll("api", new IUnmarshaller<ApiBean>() { //$NON-NLS-1$
            @Override
            public ApiBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallApi(source);
            }
        }, matchOrgQuery(organizationId));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPlanVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<PlanVersionBean> getAllPlanVersions(String organizationId, String planId)
            throws StorageException {

        QueryBuilder qb =
            QueryBuilders.query(
                FilterBuilders.boolFilter(
                    FilterBuilders.filter(
                            FilterBuilders.termFilter("organizationId", organizationId),
                            FilterBuilders.termFilter("planId", planId)
                    )
                )
            );

        try {
            return getAll("planVersion", new IUnmarshaller<PlanVersionBean>() { //$NON-NLS-1$
                @Override
                public PlanVersionBean unmarshal(Map<String, Object> source) {
                    return EsMarshalling.unmarshallPlanVersion(source);
                }
            }, qb.string());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllApiVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<ApiVersionBean> getAllApiVersions(String organizationId, String apiId)
            throws StorageException {

        QueryBuilder qb =
            QueryBuilders.query(
                FilterBuilders.boolFilter(
                    FilterBuilders.filter(
                            FilterBuilders.termFilter("organizationId", organizationId),
                            FilterBuilders.termFilter("apiId", apiId)
                    )
                )
        );

        try {
            return getAll("apiVersion", new IUnmarshaller<ApiVersionBean>() { //$NON-NLS-1$
                @Override
                public ApiVersionBean unmarshal(Map<String, Object> source) {
                    return EsMarshalling.unmarshallApiVersion(source);
                }
            }, qb.string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllClientVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<ClientVersionBean> getAllClientVersions(String organizationId,
            String clientId) throws StorageException {
        QueryBuilder qb =
            QueryBuilders.query(
               FilterBuilders.boolFilter(
                   FilterBuilders.filter(
                       FilterBuilders.termFilter("organizationId", organizationId),
                       FilterBuilders.termFilter("clientId", clientId)
                   )
               )
            );

        try {
            return getAll("clientVersion", new IUnmarshaller<ClientVersionBean>() { //$NON-NLS-1$
                @Override
                public ClientVersionBean unmarshal(Map<String, Object> source) {
                    return EsMarshalling.unmarshallClientVersion(source);
                }
            }, qb.string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<ContractBean> getAllContracts(String organizationId, String clientId, String version) throws StorageException {

        try {
            QueryBuilder qb =
                FilterBuilders.boolFilter(
                    FilterBuilders.filter(
                            FilterBuilders.termFilter("clientOrganizationId", organizationId),
                            FilterBuilders.termFilter("clientId", clientId),
                            FilterBuilders.termFilter("clientVersion", version)
                    )
                );

            String query = new SearchSourceBuilder().query(qb).string();

            return getAll("contract", new IUnmarshaller<ContractBean>() { //$NON-NLS-1$
                @Override
                public ContractBean unmarshal(Map<String, Object> source) {
                    ContractBean contract = EsMarshalling.unmarshallContract(source);
                    String apiOrgId = (String) source.get("apiOrganizationId");
                    String apiId = (String) source.get("apiId");
                    String apiVersion = (String) source.get("apiVersion");
                    String planId = (String) source.get("planId");
                    String planVersion = (String) source.get("planVersion");

                    ApiVersionBean svb = new ApiVersionBean();
                    svb.setVersion(apiVersion);
                    svb.setApi(new ApiBean());
                    svb.getApi().setOrganization(new OrganizationBean());
                    svb.getApi().setId(apiId);
                    svb.getApi().getOrganization().setId(apiOrgId);

                    PlanVersionBean pvb = new PlanVersionBean();
                    pvb.setVersion(planVersion);
                    pvb.setPlan(new PlanBean());
                    pvb.getPlan().setOrganization(new OrganizationBean());
                    pvb.getPlan().setId(planId);
                    pvb.getPlan().getOrganization().setId(apiOrgId);

                    contract.setPlan(pvb);
                    contract.setApi(svb);
                    return contract;
                }
            }, query);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @Override
    public Iterator<PolicyBean> getAllPolicies(String organizationId, String entityId, String version,
            PolicyType type) throws StorageException {
        try {
            String docType = getPoliciesDocType(type);
            String pid = id(organizationId, entityId, version);
            Map<String, Object> source = getEntity(docType, pid);
            PoliciesBean policies = EsMarshalling.unmarshallPolicies(source);
            if (policies == null || policies.getPolicies() == null) {
                return new ArrayList<PolicyBean>().iterator();
            }
            List<PolicyBean> policyBeans = policies.getPolicies();
            // TODO resolve the policy def, since we know we'll only have the definition ID here
            for (PolicyBean policyBean : policyBeans) {
                PolicyDefinitionBean def = getPolicyDefinition(policyBean.getDefinition().getId());
                if (def != null) {
                    policyBean.setDefinition(def);
                }
            }
            return policyBeans.iterator();
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @Override
    public Iterator<GatewayBean> getAllGateways() throws StorageException {
        return getAll("gateway", new IUnmarshaller<GatewayBean>() { //$NON-NLS-1$
            @Override
            public GatewayBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallGateway(source);
            }
        });
    }

    @Override
    public Iterator<UserBean> getAllUsers() throws StorageException {
        return getAll("user", new IUnmarshaller<UserBean>() { //$NON-NLS-1$
            @Override
            public UserBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallUser(source);
            }
        });
    }

    @Override
    public Iterator<RoleBean> getAllRoles() throws StorageException {
        return getAll("role", new IUnmarshaller<RoleBean>() { //$NON-NLS-1$
            @Override
            public RoleBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallRole(source);
            }
        });
    }

    @Override
    public Iterator<ContractBean> getAllContracts(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAll("contract", EsMarshalling::unmarshallContract, matchOrgQuery(organizationBean.getId())); //$NON-NLS-1$
    }

    @Override
    public Iterator<ClientVersionBean> getAllClientVersions(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAll("clientVersion", EsMarshalling::unmarshallClientVersion, matchOrgQuery(organizationBean.getId())); //$NON-NLS-1$
    }

    @Override
    public Iterator<ClientVersionBean> getAllClientVersions(OrganizationBean organizationBean, ClientStatus status, int lim) throws StorageException {
        return getAll("clientVersion", EsMarshalling::unmarshallClientVersion, matchOrgAndStatusQuery(organizationBean.getId(), status.name())); //$NON-NLS-1$
    }

    @Override
    public Iterator<ApiVersionBean> getAllApiVersions(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAll("apiVersion", EsMarshalling::unmarshallApiVersion, matchOrgQuery(organizationBean.getId())); //$NON-NLS-1$
    }

    @Override
    public Iterator<ApiVersionBean> getAllApiVersions(OrganizationBean organizationBean, ApiStatus status, int lim) throws StorageException {
        return getAll("apiVersion", EsMarshalling::unmarshallApiVersion, matchOrgAndStatusQuery(organizationBean.getId(), status.name())); //$NON-NLS-1$
    }

    @Override
    public Iterator<PlanVersionBean> getAllPlanVersions(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAll("planVersion", EsMarshalling::unmarshallPlanVersion, matchOrgQuery(organizationBean.getId())); //$NON-NLS-1$
    }

    @Override
    public Iterator<PlanVersionBean> getAllPlanVersions(OrganizationBean organizationBean, PlanStatus status, int lim) throws StorageException {
        return getAll("planVersion", EsMarshalling::unmarshallPlanVersion, matchOrgAndStatusQuery(organizationBean.getId(), status.name())); //$NON-NLS-1$
    }

    @Override
    public Iterator<RoleMembershipBean> getAllMemberships(String organizationId) throws StorageException {
        return getAll("roleMembership", new IUnmarshaller<RoleMembershipBean>() { //$NON-NLS-1$
            @Override
            public RoleMembershipBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallRoleMembership(source);
            }
        }, matchOrgQuery(organizationId));
    }

    @Override
    @SuppressWarnings("nls")
    public Iterator<AuditEntryBean> getAllAuditEntries(String organizationId) throws StorageException {
        return getAll("auditEntry", new IUnmarshaller<AuditEntryBean>() {
            @Override
            public AuditEntryBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallAuditEntry(source);
            }
        }, matchOrgQuery(organizationId), new Sort("id"));
    }

    @Override
    public Iterator<PluginBean> getAllPlugins() throws StorageException {
        return getAll("plugin", new IUnmarshaller<PluginBean>() { //$NON-NLS-1$
            @Override
            public PluginBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallPlugin(source);
            }
        });
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllPolicyDefinitions()
     */
    @Override
    public Iterator<PolicyDefinitionBean> getAllPolicyDefinitions() throws StorageException {
        return getAll("policyDef", new IUnmarshaller<PolicyDefinitionBean>() { //$NON-NLS-1$
            @Override
            public PolicyDefinitionBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallPolicyDefinition(source);
            }
        });
    }

    /**
     * Returns an iterator over all instances of the given entity type.
     * @param entityType
     * @param unmarshaller
     * @throws StorageException
     */
    private <T> Iterator<T> getAll(String entityType, IUnmarshaller<T> unmarshaller) throws StorageException {
        String query = matchAllQuery();
        return getAll(entityType, unmarshaller, query);
    }

    /**
     * Returns an iterator over all instances of the given entity type with index sort (_doc).
     * @param entityType
     * @param unmarshaller
     * @param query
     * @throws StorageException
     */
    private <T> Iterator<T> getAll(String entityType, IUnmarshaller<T> unmarshaller, String query) throws StorageException {
        return new EntityIterator<>(entityType, unmarshaller, query);
    }

    /**
     * Returns an iterator over all instances of the given entity type with the provided sort.
     * @param entityType
     * @param unmarshaller
     * @param query
     * @param sort
     * @return
     * @throws StorageException
     */
    private <T> Iterator<T> getAll(String entityType, IUnmarshaller<T> unmarshaller, String query, Sort sort) throws StorageException {
        return new EntityIterator<>(entityType, unmarshaller, query, sort);
    }

    /**
     * A simple, internal unmarshaller interface.
     * @author eric.wittmann@redhat.com
     */
    private static interface IUnmarshaller<T> {
        /**
         * Unmarshal the source map into an entity.
         * @param source the source map
         * @return the unmarshalled instance of <T>
         */
        public T unmarshal(Map<String, Object> source);
    }

    /**
     * Allows iterating over all entities of a given type.
     * @author eric.wittmann@redhat.com
     */
    @SuppressWarnings("nls")
    private class EntityIterator<T> implements Iterator<T> {

        private final String query;
        private final Sort sort;
        private final String entityType;
        private final IUnmarshaller<T> unmarshaller;
        private String scrollId = null;
        private List<Hit<Map<String, Object>, Void>> hits;
        private int nextHitIdx;

        /**
         * Constructor.
         * @param entityType the entity type
         * @param unmarshaller the unmarshaller
         * @param query the query
         * @throws StorageException when storage fails
         */
        public EntityIterator(String entityType, IUnmarshaller<T> unmarshaller, String query) throws StorageException {
            this.entityType = entityType;
            this.unmarshaller = unmarshaller;
            this.query = query;
            this.sort = new Sort("_doc");
            initScroll();
            this.nextHitIdx = 0;
        }

        public EntityIterator(String entityType, IUnmarshaller<T> unmarshaller, String query, Sort sort) throws StorageException {
            this.entityType = entityType;
            this.unmarshaller = unmarshaller;
            this.query = query;
            this.sort = sort;
            initScroll();
            this.nextHitIdx = 0;
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            if (hits == null || this.nextHitIdx >= hits.size()) {
                try {
                    fetch();
                } catch (StorageException e) {
                    throw new RuntimeException(e);
                }
                this.nextHitIdx = 0;
            }
            return !hits.isEmpty();
        }

        /**
         * @see java.util.Iterator#next()
         */
        @Override
        public T next() {
            Hit<Map<String, Object>, Void> hit = hits.get(nextHitIdx++);
            return unmarshaller.unmarshal(hit.source);
        }

        /**
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            // Not implemented.
        }

        private void initScroll() throws StorageException {
            try {
                Search search = new Search.Builder(query)
                        .addIndex(getIndexName())
                        .addType(entityType)
                        .setParameter(Parameters.SCROLL, "1m")
                        .addSort(sort)
                        .build();
                SearchResult response = esClient.execute(search);
                if (!response.isSucceeded()) {
                    throw new StorageException("Scrolled query failed " + response.getErrorMessage());
                }
                scrollId = response.getJsonObject().get("_scroll_id").getAsString();
                this.hits = (List) response.getHits(Map.class);
            } catch (IOException e) {
                throw new StorageException(e);
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void fetch() throws StorageException {
            try {
                Builder builder = new SearchScroll.Builder(scrollId, "1m");
                SearchScroll scroll = new SearchScroll(builder) {
                    @Override
                    public JestResult createNewElasticSearchResult(String responseBody, int statusCode,
                            String reasonPhrase, Gson gson) {
                        return createNewElasticSearchResult(new SearchResult(gson), responseBody, statusCode, reasonPhrase, gson);
                    }
                };
                SearchResult response = (SearchResult) esClient.execute(scroll);
                if (!response.isSucceeded()) {
                    throw new StorageException("Scrolled fetch failed " + response.getErrorMessage());
                }
                this.hits = (List) response.getHits(Map.class);
            } catch (IOException e) {
                throw new StorageException(e);
            }
        }

    }

    @SuppressWarnings("nls")
    private String matchOrgAndStatusQuery(String organizationId, String status) {
        return  "{" +
                "    \"query\": {" +
                "        \"bool\": {" +
                "            \"filter\": [" +
                "                { \"term\": { \"organizationId\": \"" + organizationId + "\" } }, " +
                "                { \"term\": { \"status\": \"" + status + "\" } }" +
                "            ]" +
                "        }" +
                "    }" +
                "}";
    }

    /**
     * @return an ES query to match all documents
     */
    @SuppressWarnings("nls")
    private String matchAllQuery() {
        return "{" +
                "  \"query\": {" +
                "    \"match_all\": {}" +
                "  }" +
                "}";
    }

    @SuppressWarnings("nls")
    private String matchOrgQuery(String organizationId) {
        return "{" +
                "  \"query\": {" +
                "    \"bool\": { " +
                "      \"filter\": {" +
                "        \"term\": { \"organizationId\": \"" + organizationId + "\" }" +
                "      }" +
                "    }" +
                "  }" +
                "}";
    }

    /**
     * @return the indexName
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * @param indexName the indexName to set
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

}
