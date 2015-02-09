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

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
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
import io.apiman.manager.api.beans.search.OrderByBean;
import io.apiman.manager.api.beans.search.PagingBean;
import io.apiman.manager.api.beans.search.SearchCriteriaBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterBean;
import io.apiman.manager.api.beans.search.SearchCriteriaFilterOperator;
import io.apiman.manager.api.beans.search.SearchResultsBean;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.beans.summary.ApiRegistryBean;
import io.apiman.manager.api.beans.summary.ApplicationSummaryBean;
import io.apiman.manager.api.beans.summary.ApplicationVersionSummaryBean;
import io.apiman.manager.api.beans.summary.ContractSummaryBean;
import io.apiman.manager.api.beans.summary.GatewaySummaryBean;
import io.apiman.manager.api.beans.summary.OrganizationSummaryBean;
import io.apiman.manager.api.beans.summary.PlanSummaryBean;
import io.apiman.manager.api.beans.summary.PlanVersionSummaryBean;
import io.apiman.manager.api.beans.summary.PluginSummaryBean;
import io.apiman.manager.api.beans.summary.PolicyDefinitionSummaryBean;
import io.apiman.manager.api.beans.summary.PolicySummaryBean;
import io.apiman.manager.api.beans.summary.ServicePlanSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceSummaryBean;
import io.apiman.manager.api.beans.summary.ServiceVersionSummaryBean;
import io.apiman.manager.api.core.IIdmStorage;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;

import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

/**
 * An implementation of the API Manager persistence layer that uses git to store
 * the entities.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class EsStorage implements IStorage, IStorageQuery, IIdmStorage {
    
    private static final String INDEX_NAME = "apiman_manager"; //$NON-NLS-1$
    private static final SecureRandom random = new SecureRandom();
    
    @Inject
    TransportClient esClient;
    
    /**
     * Constructor.
     */
    public EsStorage() {
    }
    
    /**
     * Called to initialize the storage.
     */
    public void initialize() {
        try {
            IndicesExistsRequest request = new IndicesExistsRequest(INDEX_NAME);
            IndicesExistsResponse response = esClient.admin().indices().exists(request).get();
            if (!response.isExists()) {
                createIndex(INDEX_NAME);
            }
            esClient.admin().cluster().prepareHealth().setWaitForYellowStatus().execute().actionGet(5000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param indexName
     * @throws Exception 
     */
    private void createIndex(String indexName) throws Exception {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        URL settings = getClass().getResource("index-settings.json"); //$NON-NLS-1$
        String source = IOUtils.toString(settings);
        request.source(source);
        CreateIndexResponse response = esClient.admin().indices().create(request).get();
        if (!response.isAcknowledged()) {
            throw new StorageException("Failed to create index: " + indexName);
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
        indexEntity("organization", organization.getId(), EsMarshalling.marshall(organization)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApplication(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void createApplication(ApplicationBean application) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApplicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void createApplicationVersion(ApplicationVersionBean version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void createContract(ContractBean contract) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createService(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void createService(ServiceBean service) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createServiceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void createServiceVersion(ServiceVersionBean version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void createPlan(PlanBean plan) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void createPlanVersion(PlanVersionBean version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void createPolicy(PolicyBean policy) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
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
        indexEntity("plugin", String.valueOf(plugin.getId()), EsMarshalling.marshall(plugin)); //$NON-NLS-1$
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
     * @see io.apiman.manager.api.core.IStorage#updateApplication(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void updateApplication(ApplicationBean application) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApplicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void updateApplicationVersion(ApplicationVersionBean version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void updateContract(ContractBean contract) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateService(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void updateService(ServiceBean service) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateServiceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void updateServiceVersion(ServiceVersionBean version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void updatePlan(PlanBean plan) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void updatePlanVersion(PlanVersionBean version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void updatePolicy(PolicyBean policy) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
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
    public void deleteOrganization(OrganizationBean organization) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApplication(io.apiman.manager.api.beans.apps.ApplicationBean)
     */
    @Override
    public void deleteApplication(ApplicationBean application) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApplicationVersion(io.apiman.manager.api.beans.apps.ApplicationVersionBean)
     */
    @Override
    public void deleteApplicationVersion(ApplicationVersionBean version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void deleteContract(ContractBean contract) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteService(io.apiman.manager.api.beans.services.ServiceBean)
     */
    @Override
    public void deleteService(ServiceBean service) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteServiceVersion(io.apiman.manager.api.beans.services.ServiceVersionBean)
     */
    @Override
    public void deleteServiceVersion(ServiceVersionBean version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void deletePlan(PlanBean plan) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void deletePlanVersion(PlanVersionBean version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePolicy(io.apiman.manager.api.beans.policies.PolicyBean)
     */
    @Override
    public void deletePolicy(PolicyBean policy) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
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
     * @see io.apiman.manager.api.core.IStorage#deletePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        deleteEntity("policyDef", policyDef.getId()); //$NON-NLS-1$
    }

    /**
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
     * @see io.apiman.manager.api.core.IStorage#getApplication(java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationBean getApplication(String organizationId, String id) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApplicationVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApplicationVersionBean getApplicationVersion(String organizationId, String applicationId,
            String version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getContract(java.lang.Long)
     */
    @Override
    public ContractBean getContract(Long id) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getService(java.lang.String, java.lang.String)
     */
    @Override
    public ServiceBean getService(String organizationId, String id) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getServiceVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ServiceVersionBean getServiceVersion(String organizationId, String serviceId, String version)
            throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlan(java.lang.String, java.lang.String)
     */
    @Override
    public PlanBean getPlan(String organizationId, String id) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlanVersion(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public PlanVersionBean getPlanVersion(String organizationId, String planId, String version)
            throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPolicy(java.lang.Long)
     */
    @Override
    public PolicyBean getPolicy(Long id) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
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
            QueryBuilder qb = QueryBuilders.filteredQuery(
                    QueryBuilders.matchAllQuery(),
                    FilterBuilders.andFilter(
                            FilterBuilders.termFilter("groupId", groupId),
                            FilterBuilders.termFilter("artifactId", artifactId)
                    )
                );
            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(2);
            SearchRequest request = new SearchRequest(INDEX_NAME);
            request.types("plugin"); //$NON-NLS-1$
            request.source(builder);
            SearchResponse response = esClient.search(request).get();
            SearchHits hits = response.getHits();
            if (hits.totalHits() == 1) {
                SearchHit hit = hits.iterator().next();
                return EsMarshalling.unmarshallPlugin(hit.getSource());
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
    public List<PluginSummaryBean> listPlugins() throws StorageException {
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .fields("id", "artifactId", "groupId", "version", "classifier", "type", "name",
                        "description", "createdBy", "createdOn").sort("name.raw", SortOrder.ASC).size(200);
        SearchHits hits = listEntities("plugin", builder); //$NON-NLS-1$
        List<PluginSummaryBean> rval = new ArrayList<>((int) hits.totalHits());
        for (SearchHit hit : hits) {
            PluginSummaryBean bean = EsMarshalling.unmarshallPluginSummary(hit.getFields());
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
        SearchSourceBuilder builder = new SearchSourceBuilder().fields("id", "name", "description",
                "type").sort("name.raw", SortOrder.ASC).size(100);
        SearchHits hits = listEntities("gateway", builder); //$NON-NLS-1$
        List<GatewaySummaryBean> rval = new ArrayList<>((int) hits.totalHits());
        for (SearchHit hit : hits) {
            GatewaySummaryBean bean = EsMarshalling.unmarshallGatewaySummary(hit.getFields());
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
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findApplications(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ApplicationSummaryBean> findApplications(SearchCriteriaBean criteria)
            throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findServices(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<ServiceSummaryBean> findServices(SearchCriteriaBean criteria)
            throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findPlans(java.lang.String, io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<PlanSummaryBean> findPlans(String organizationId, SearchCriteriaBean criteria)
            throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#auditEntity(java.lang.String, java.lang.String, java.lang.String, java.lang.Class, io.apiman.manager.api.beans.search.PagingBean)
     */
    @Override
    public <T> SearchResultsBean<AuditEntryBean> auditEntity(String organizationId, String entityId,
            String entityVersion, Class<T> type, PagingBean paging) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#auditUser(java.lang.String, io.apiman.manager.api.beans.search.PagingBean)
     */
    @Override
    public <T> SearchResultsBean<AuditEntryBean> auditUser(String userId, PagingBean paging)
            throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getOrgs(java.util.Set)
     */
    @Override
    public List<OrganizationSummaryBean> getOrgs(Set<String> orgIds) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationsInOrgs(java.util.Set)
     */
    @Override
    public List<ApplicationSummaryBean> getApplicationsInOrgs(Set<String> orgIds) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationsInOrg(java.lang.String)
     */
    @Override
    public List<ApplicationSummaryBean> getApplicationsInOrg(String orgId) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ApplicationVersionSummaryBean> getApplicationVersions(String organizationId,
            String applicationId) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApplicationContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ContractSummaryBean> getApplicationContracts(String organizationId, String applicationId,
            String version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getApiRegistry(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public ApiRegistryBean getApiRegistry(String organizationId, String applicationId, String version)
            throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServicesInOrgs(java.util.Set)
     */
    @Override
    public List<ServiceSummaryBean> getServicesInOrgs(Set<String> orgIds) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServicesInOrg(java.lang.String)
     */
    @Override
    public List<ServiceSummaryBean> getServicesInOrg(String orgId) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServiceVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<ServiceVersionSummaryBean> getServiceVersions(String orgId, String serviceId)
            throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServiceVersionPlans(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ServicePlanSummaryBean> getServiceVersionPlans(String organizationId, String serviceId,
            String version) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlansInOrgs(java.util.Set)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrgs(Set<String> orgIds) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlansInOrg(java.lang.String)
     */
    @Override
    public List<PlanSummaryBean> getPlansInOrg(String orgId) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPlanVersions(java.lang.String, java.lang.String)
     */
    @Override
    public List<PlanVersionSummaryBean> getPlanVersions(String organizationId, String planId)
            throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getPolicies(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @Override
    public List<PolicySummaryBean> getPolicies(String organizationId, String entityId, String version,
            PolicyType type) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPolicyDefinitions()
     */
    @Override
    public List<PolicyDefinitionSummaryBean> listPolicyDefinitions() throws StorageException {
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .fields("id", "policyImpl", "name", "description", "icon", "pluginId", "formType")
                .sort("name.raw", SortOrder.ASC).size(100);
        SearchHits hits = listEntities("policyDef", builder); //$NON-NLS-1$
        List<PolicyDefinitionSummaryBean> rval = new ArrayList<>((int) hits.totalHits());
        for (SearchHit hit : hits) {
            PolicyDefinitionSummaryBean bean = EsMarshalling.unmarshallPolicyDefinitionSummary(hit.getFields());
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getServiceContracts(java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public List<ContractSummaryBean> getServiceContracts(String organizationId, String serviceId,
            String version, int page, int pageSize) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#getMaxPolicyOrderIndex(java.lang.String, java.lang.String, java.lang.String, io.apiman.manager.api.beans.policies.PolicyType)
     */
    @Override
    public int getMaxPolicyOrderIndex(String organizationId, String entityId, String entityVersion,
            PolicyType type) throws StorageException {
        throw new StorageException("Not yet implemented."); //$NON-NLS-1$ TODO Auto-generated method stub
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#listPluginPolicyDefs(java.lang.Long)
     */
    @Override
    public List<PolicyDefinitionSummaryBean> listPluginPolicyDefs(Long pluginId) throws StorageException {
        @SuppressWarnings("nls")
        QueryBuilder qb = QueryBuilders.filteredQuery(
                QueryBuilders.matchAllQuery(),
                FilterBuilders.termFilter("pluginId", pluginId)
            );
        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .fields("id", "policyImpl", "name", "description", "icon", "pluginId", "formType")
                .query(qb)
                .sort("name.raw", SortOrder.ASC).size(100);
        SearchHits hits = listEntities("policyDef", builder); //$NON-NLS-1$
        List<PolicyDefinitionSummaryBean> rval = new ArrayList<>((int) hits.totalHits());
        for (SearchHit hit : hits) {
            PolicyDefinitionSummaryBean bean = EsMarshalling.unmarshallPolicyDefinitionSummary(hit.getFields());
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#createUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void createUser(UserBean user) throws StorageException {
        indexEntity("user", user.getUsername(), EsMarshalling.marshall(user)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUser(java.lang.String)
     */
    @Override
    public UserBean getUser(String userId) throws StorageException {
        Map<String, Object> source = getEntity("user", userId); //$NON-NLS-1$
        return EsMarshalling.unmarshallUser(source);
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#updateUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void updateUser(UserBean user) throws StorageException {
        updateEntity("user", user.getUsername(), EsMarshalling.marshall(user)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#findUsers(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException {
        return find(criteria, "user", new IUnmarshaller<UserBean>() { //$NON-NLS-1$
            @Override
            public UserBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallUser(source);
            }
        });
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#findRoles(io.apiman.manager.api.beans.search.SearchCriteriaBean)
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
     * @see io.apiman.manager.api.core.IIdmStorage#createMembership(io.apiman.manager.api.beans.idm.RoleMembershipBean)
     */
    @Override
    public void createMembership(RoleMembershipBean membership) throws StorageException {
        membership.setId(generateGuid());
        indexEntity("roleMembership", String.valueOf(membership.getId()), EsMarshalling.marshall(membership)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMembership(String userId, String roleId, String organizationId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#deleteMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteMemberships(String userId, String organizationId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUserMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getUserMemberships(java.lang.String, java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getUserMemberships(String userId, String organizationId)
            throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getOrgMemberships(java.lang.String)
     */
    @Override
    public Set<RoleMembershipBean> getOrgMemberships(String organizationId) throws StorageException {
        throw new StorageException("Not yet implemented"); // TODO Auto-generated method stub
        
    }

    /**
     * @see io.apiman.manager.api.core.IIdmStorage#getPermissions(java.lang.String)
     */
    @Override
    public Set<PermissionBean> getPermissions(String userId) throws StorageException {
        try {
            @SuppressWarnings("nls")
            QueryBuilder qb = QueryBuilders.filteredQuery(
                    QueryBuilders.matchAllQuery(),
                    FilterBuilders.termFilter("userId", userId)
                );
            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(500);
            SearchRequest request = new SearchRequest(INDEX_NAME);
            request.types("roleMembership"); //$NON-NLS-1$
            request.source(builder);
            SearchResponse response = esClient.search(request).get();
            SearchHits hits = response.getHits();
            Set<PermissionBean> rval = new HashSet<>((int) hits.totalHits());
            if (hits.totalHits() > 0) {
                for (SearchHit hit : hits) {
                    Map<String, Object> source = hit.getSource();
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
     * @param entitySource
     * @throws StorageException
     */
    private void indexEntity(String type, String id, XContentBuilder entitySource) throws StorageException {
        try {
            IndexRequest request = new IndexRequest(INDEX_NAME, type, id);
            request.create(true);
            request.contentType(XContentType.JSON);
            request.source(entitySource);
            
            ActionFuture<IndexResponse> future = esClient.index(request);
            
            IndexResponse response = future.get();
            if (!response.isCreated()) {
                throw new StorageException("Failed to index document {0} / {1}" /*, type, id */);
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
            GetRequest request = new GetRequest(INDEX_NAME, type, id);
            ActionFuture<GetResponse> future = esClient.get(request);
            GetResponse response = future.get();
            if (!response.isExists()) {
                return null;
            }
            return response.getSourceAsMap();
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
    
    /**
     * Returns a list of entities.
     * @param type
     * @param search
     * @throws StorageException
     */
    private SearchHits listEntities(String type, SearchSourceBuilder search) throws StorageException {
        try {
            SearchRequest request = new SearchRequest(INDEX_NAME);
            request.types(type);
            request.source(search);
            SearchResponse response = esClient.search(request).get();
            SearchHits hits = response.getHits();
            return hits;
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
            DeleteRequest request = new DeleteRequest(INDEX_NAME, type, id);
            ActionFuture<DeleteResponse> future = esClient.delete(request);
            DeleteResponse response = future.get();
            if (!response.isFound()) {
                throw new StorageException("Document could not be deleted because it did not exist.");
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
            IndexRequest request = new IndexRequest(INDEX_NAME, type, id);
            request.create(false);
            request.contentType(XContentType.JSON);
            request.source(source);
            
            ActionFuture<IndexResponse> future = esClient.index(request);
            future.get();
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
            SearchResultsBean<T> rval = new SearchResultsBean<T>();

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
            BaseQueryBuilder q = QueryBuilders.matchAllQuery();
            if (filters != null && !filters.isEmpty()) {
                
                AndFilterBuilder andFilter = FilterBuilders.andFilter();
                int filterCount = 0;
                for (SearchCriteriaFilterBean filter : filters) {
                    if (filter.getOperator() == SearchCriteriaFilterOperator.eq) {
                        andFilter.add(FilterBuilders.termFilter(filter.getName(), filter.getValue()));
                        filterCount++;
                    } else if (filter.getOperator() == SearchCriteriaFilterOperator.like) {
                        q = QueryBuilders.wildcardQuery(filter.getName(), filter.getValue());
                    } else if (filter.getOperator() == SearchCriteriaFilterOperator.bool_eq) {
                        andFilter.add(FilterBuilders.termFilter(filter.getName(), "true".equals(filter.getValue()))); //$NON-NLS-1$
                        filterCount++;
                    }
                    // TODO implement the other filter operators here!
                }
                
                if (filterCount > 0) {
                    q = QueryBuilders.filteredQuery(q, andFilter);
                }
            }
            builder.query(q);
            
            SearchRequest request = new SearchRequest(INDEX_NAME);
            request.types(type);
            request.source(builder);
            SearchResponse response = esClient.search(request).get();
            SearchHits hits = response.getHits();
            rval.setTotalSize((int) hits.totalHits());
            for (SearchHit hit : hits) {
                Map<String, Object> sourceAsMap = hit.sourceAsMap();
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
    private static Long generateGuid() {
        StringBuilder builder = new StringBuilder();
        builder.append(System.currentTimeMillis());
        builder.append(random.nextInt(10000));
        return Long.parseLong(builder.toString());
    }
    
    private static interface IUnmarshaller<T> {
        /**
         * Unmarshal the source map into an entity.
         * @param source
         */
        public T unmarshal(Map<String, Object> source);
    }
}
