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

import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.common.es.util.EsConstants;
import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.manager.api.beans.apis.*;
import io.apiman.manager.api.beans.audit.AuditEntityType;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.clients.ClientBean;
import io.apiman.manager.api.beans.clients.ClientStatus;
import io.apiman.manager.api.beans.clients.ClientVersionBean;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.developers.DeveloperBean;
import io.apiman.manager.api.beans.download.DownloadBean;
import io.apiman.manager.api.beans.gateways.GatewayBean;
import io.apiman.manager.api.beans.idm.*;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanStatus;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.plugins.PluginBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyDefinitionBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.search.*;
import io.apiman.manager.api.beans.summary.*;
import io.apiman.manager.api.beans.system.MetadataBean;
import io.apiman.manager.api.core.IStorage;
import io.apiman.manager.api.core.IStorageQuery;
import io.apiman.manager.api.core.exceptions.StorageException;
import io.apiman.manager.api.core.util.PolicyTemplateUtil;
import io.apiman.manager.api.es.beans.ApiDefinitionBean;
import io.apiman.manager.api.es.beans.PoliciesBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * An implementation of the API Manager persistence layer that uses git to store
 * the entities.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped @Alternative
public class EsStorage extends AbstractEsComponent implements IStorage, IStorageQuery {

    private static final String DEFAULT_INDEX_NAME = EsConstants.MANAGER_INDEX_NAME;
    private String indexPrefix = DEFAULT_INDEX_NAME;

    private static int guidCounter = 100;

    @Inject IDataEncrypter encrypter;

    @PostConstruct
    public void postConstruct() {
        // Kick the encrypter, causing it to be loaded/resolved in CDI
        encrypter.encrypt("", new DataEncryptionContext()); //$NON-NLS-1$
    }

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public EsStorage(Map<String, String> config) {
        super(config);
    }

    /**
     * Called to initialize the storage.
     */
    @Override
    @SuppressWarnings("nls")
    public void initialize() {
        //noop for elasticsearch
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
     * @see io.apiman.manager.api.core.IStorage#createDeveloper(DeveloperBean)
     */
    @Override
    public void createDeveloper(DeveloperBean developerBean) throws StorageException {
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_DEVELOPER, developerBean.getId(), EsMarshalling.marshall(developerBean));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void createOrganization(OrganizationBean organization) throws StorageException {
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_ORGANIZATION, organization.getId(), EsMarshalling.marshall(organization));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createClient(io.apiman.manager.api.beans.clients.ClientBean)
     */
    @Override
    public void createClient(ClientBean client) throws StorageException {
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT, id(client.getOrganization().getId(), client.getId()), EsMarshalling.marshall(client));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createClientVersion(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public void createClientVersion(ClientVersionBean version) throws StorageException {
        ClientBean client = version.getClient();
        String id = id(client.getOrganization().getId(), client.getId(), version.getVersion());
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION, id, EsMarshalling.marshall(version));
        PoliciesBean policies = PoliciesBean.from(PolicyType.Client, client.getOrganization().getId(),
                client.getId(), version.getVersion());
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_POLICIES, id, EsMarshalling.marshall(policies));
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
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT, String.valueOf(contract.getId()), EsMarshalling.marshall(contract));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApi(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    public void createApi(ApiBean api) throws StorageException {
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_API, id(api.getOrganization().getId(), api.getId()), EsMarshalling.marshall(api));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createApiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void createApiVersion(ApiVersionBean version) throws StorageException {
        ApiBean api = version.getApi();
        String id = id(api.getOrganization().getId(), api.getId(), version.getVersion());
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION, id, EsMarshalling.marshall(version));
        PoliciesBean policies = PoliciesBean.from(PolicyType.Api, api.getOrganization().getId(),
                api.getId(), version.getVersion());
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_API_POLICIES, id, EsMarshalling.marshall(policies));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    public void createPlan(PlanBean plan) throws StorageException {
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLAN, id(plan.getOrganization().getId(), plan.getId()), EsMarshalling.marshall(plan));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void createPlanVersion(PlanVersionBean version) throws StorageException {
        PlanBean plan = version.getPlan();
        String id = id(plan.getOrganization().getId(), plan.getId(), version.getVersion());
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION, id, EsMarshalling.marshall(version));
        PoliciesBean policies = PoliciesBean.from(PolicyType.Plan, plan.getOrganization().getId(),
                plan.getId(), version.getVersion());
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLAN_POLICIES, id, EsMarshalling.marshall(policies));
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
     * @see io.apiman.manager.api.core.IStorage#createMetadata(MetadataBean)
     */
    @Override
    public void createMetadata(MetadataBean metadata) throws StorageException {
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_METADATA, String.valueOf(metadata.getId()), EsMarshalling.marshall(metadata));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getMetadata(Long)
     */
    @Override
    public MetadataBean getMetadata(Long id) throws StorageException {
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_METADATA, String.valueOf(id));
        return EsMarshalling.unmarshallMetadata(source);
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
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_GATEWAY, gateway.getId(), EsMarshalling.marshall(gateway));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void createPlugin(PluginBean plugin) throws StorageException {
        plugin.setId(generateGuid());
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLUGIN, String.valueOf(plugin.getId()), EsMarshalling.marshall(plugin));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createDownload(io.apiman.manager.api.beans.download.DownloadBean)
     */
    @Override
    public void createDownload(DownloadBean download) throws StorageException {
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_DOWNLOAD, download.getId(), EsMarshalling.marshall(download));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createPolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void createPolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_POLICY_DEF, policyDef.getId(), EsMarshalling.marshall(policyDef));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void createRole(RoleBean role) throws StorageException {
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_ROLE, role.getId(), EsMarshalling.marshall(role));
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
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_AUDIT_ENTRY, String.valueOf(entry.getId()), EsMarshalling.marshall(entry));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    public void updateOrganization(OrganizationBean organization) throws StorageException {
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_ORGANIZATION, organization.getId(), EsMarshalling.marshall(organization));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateClient(io.apiman.manager.api.beans.clients.ClientBean)
     */
    @Override
    public void updateClient(ClientBean client) throws StorageException {
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT, id(client.getOrganization().getId(), client.getId()), EsMarshalling.marshall(client));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateClientVersion(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public void updateClientVersion(ClientVersionBean version) throws StorageException {
        ClientBean client = version.getClient();
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION, id(client.getOrganization().getId(), client.getId(), version.getVersion()),
                EsMarshalling.marshall(version));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApi(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    public void updateApi(ApiBean api) throws StorageException {
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_API, id(api.getOrganization().getId(), api.getId()), EsMarshalling.marshall(api));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateApiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void updateApiVersion(ApiVersionBean version) throws StorageException {
        ApiBean api = version.getApi();
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION, id(api.getOrganization().getId(), api.getId(), version.getVersion()),
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
                indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_API_DEFINITION, id, EsMarshalling.marshall(definition));
            } else {
                updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_API_DEFINITION, id, EsMarshalling.marshall(definition));
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
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLAN, id(plan.getOrganization().getId(), plan.getId()), EsMarshalling.marshall(plan));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlanVersion(io.apiman.manager.api.beans.plans.PlanVersionBean)
     */
    @Override
    public void updatePlanVersion(PlanVersionBean version) throws StorageException {
        PlanBean plan = version.getPlan();
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION, id(plan.getOrganization().getId(), plan.getId(), version.getVersion()),
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
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_GATEWAY, gateway.getId(), EsMarshalling.marshall(gateway));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void updatePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_POLICY_DEF, policyDef.getId(), EsMarshalling.marshall(policyDef));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updatePlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void updatePlugin(PluginBean pluginBean) throws StorageException {
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLUGIN, String.valueOf(pluginBean.getId()), EsMarshalling.marshall(pluginBean));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateDeveloper(DeveloperBean)
     */
    @Override
    public void updateDeveloper(DeveloperBean developer) throws StorageException {
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_DEVELOPER, developer.getId(), EsMarshalling.marshall(developer));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void updateRole(RoleBean role) throws StorageException {
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_ROLE, role.getId(), EsMarshalling.marshall(role));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteOrganization(io.apiman.manager.api.beans.orgs.OrganizationBean)
     */
    @Override
    @SuppressWarnings("nls")
    public void deleteOrganization(OrganizationBean organization) throws StorageException {
        try {
            String orgId = organization.getId().replace('"', '_');

            BoolQueryBuilder qb = QueryBuilders.boolQuery();
            List<QueryBuilder> shouldFilter = qb.should();
            shouldFilter.add(QueryBuilders.termQuery("organizationId", orgId));
            shouldFilter.add(QueryBuilders.termQuery("clientOrganizationId", orgId));
            shouldFilter.add(QueryBuilders.termQuery("apiOrganizationId", orgId));

            String[] indexNames = {
                    EsConstants.INDEX_MANAGER_POSTFIX_API,
                    EsConstants.INDEX_MANAGER_POSTFIX_API_POLICIES,
                    EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION,
                    EsConstants.INDEX_MANAGER_POSTFIX_AUDIT_ENTRY,
                    EsConstants.INDEX_MANAGER_POSTFIX_CLIENT,
                    EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_POLICIES,
                    EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION,
                    EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT,
                    EsConstants.INDEX_MANAGER_POSTFIX_PLAN,
                    EsConstants.INDEX_MANAGER_POSTFIX_PLAN_POLICIES,
                    EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION,
                    EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP
            };

            BulkByScrollResponse response = getDeleteByQueryResponse(qb, indexNames);

            if (response.getStatus().getSuccessfullyProcessed() != response.getStatus().getTotal()) {
                throw new StorageException("Could not delete all plan entries by query");
            }
            deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_ORGANIZATION, orgId);
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

        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = qb.filter();

        // contract
        BoolQueryBuilder shouldMatchClientOrgAndOrgId = QueryBuilders.boolQuery();
        shouldMatchClientOrgAndOrgId.should().add(QueryBuilders.termQuery("clientOrganizationId", orgId));
        shouldMatchClientOrgAndOrgId.should().add(QueryBuilders.termQuery("organizationId", orgId));

        BoolQueryBuilder shouldMatchTypesAndEntityIds = QueryBuilders.boolQuery();
        List<QueryBuilder> shouldMatchCombination = shouldMatchTypesAndEntityIds.should();

        // all audit entries with given clientId
        BoolQueryBuilder shouldMatchEntityIdAndEntityType = QueryBuilders.boolQuery();
        shouldMatchEntityIdAndEntityType.filter().add(QueryBuilders.termQuery("entityId", clientId));
        shouldMatchEntityIdAndEntityType.filter().add(QueryBuilders.termQuery("entityType", AuditEntityType.Client.name()));
        shouldMatchCombination.add(shouldMatchEntityIdAndEntityType);

        BoolQueryBuilder shouldMatchEntityIdAndType = QueryBuilders.boolQuery();
        shouldMatchEntityIdAndType.filter().add(QueryBuilders.termQuery("entityId", clientId));
        shouldMatchEntityIdAndType.filter().add(QueryBuilders.termQuery("type", AuditEntityType.Client.name()));
        shouldMatchCombination.add(shouldMatchEntityIdAndType);

        shouldMatchCombination.add(QueryBuilders.termQuery("clientId", clientId));

        filter.add(shouldMatchClientOrgAndOrgId);
        filter.add(shouldMatchTypesAndEntityIds);

        try {

            String[] indexNames = {EsConstants.INDEX_MANAGER_POSTFIX_AUDIT_ENTRY, EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION, EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_POLICIES, EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT};
            BulkByScrollResponse response = getDeleteByQueryResponse(qb, indexNames);

            if (response.getStatus().getSuccessfullyProcessed() != response.getStatus().getTotal()) {
                throw new StorageException("Could not delete all client entries by query");
            }
        } catch (Exception e) {
            throw new StorageException(e);
        }
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT, id(orgId, clientId));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteClientVersion(io.apiman.manager.api.beans.clients.ClientVersionBean)
     */
    @Override
    public void deleteClientVersion(ClientVersionBean version) throws StorageException {
        ClientBean client = version.getClient();
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION, id(client.getOrganization().getId(), client.getId(), version.getVersion()));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteContract(io.apiman.manager.api.beans.contracts.ContractBean)
     */
    @Override
    public void deleteContract(ContractBean contract) throws StorageException {
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT, String.valueOf(contract.getId()));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApi(io.apiman.manager.api.beans.apis.ApiBean)
     */
    @Override
    @SuppressWarnings("nls")
    public void deleteApi(ApiBean api) throws StorageException {
        String apiId = api.getId().replace('"', '_');
        String orgId = api.getOrganization().getId().replace('"', '_');

        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = qb.filter();

        BoolQueryBuilder shouldMatchApiOrgAndOrgId = QueryBuilders.boolQuery();
        shouldMatchApiOrgAndOrgId.should().add(QueryBuilders.termQuery("apiOrganizationId", orgId));
        shouldMatchApiOrgAndOrgId.should().add(QueryBuilders.termQuery("organizationId", orgId));

        BoolQueryBuilder shouldMatchTypesAndEntityIds = QueryBuilders.boolQuery();
        List<QueryBuilder> shouldMatchCombination = shouldMatchTypesAndEntityIds.should();

        BoolQueryBuilder shouldMatchEntityIdAndEntityType = QueryBuilders.boolQuery();
        shouldMatchEntityIdAndEntityType.filter().add(QueryBuilders.termQuery("entityId", apiId));
        shouldMatchEntityIdAndEntityType.filter().add(QueryBuilders.termQuery("entityType", AuditEntityType.Api.name()));
        shouldMatchCombination.add(shouldMatchEntityIdAndEntityType);

        BoolQueryBuilder shouldMatchEntityIdAndType = QueryBuilders.boolQuery();
        shouldMatchEntityIdAndType.filter().add(QueryBuilders.termQuery("entityId", apiId));
        shouldMatchEntityIdAndType.filter().add(QueryBuilders.termQuery("type", AuditEntityType.Api.name()));
        shouldMatchCombination.add(shouldMatchEntityIdAndType);

        // part 213
        shouldMatchCombination.add(QueryBuilders.termQuery("apiId", apiId));

        filter.add(shouldMatchApiOrgAndOrgId);
        filter.add(shouldMatchTypesAndEntityIds);


        try {

            String[] indexNames = {EsConstants.INDEX_MANAGER_POSTFIX_AUDIT_ENTRY, EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION, EsConstants.INDEX_MANAGER_POSTFIX_API_POLICIES, EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT};
            BulkByScrollResponse response = getDeleteByQueryResponse(qb, indexNames);

            if (response.getStatus().getSuccessfullyProcessed() != response.getStatus().getTotal()) {
                throw new StorageException("Could not delete all api entries by query");
            }
        } catch (Exception e) {
            throw new StorageException(e);
        }
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_API, id(orgId, apiId));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApiVersion(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void deleteApiVersion(ApiVersionBean version) throws StorageException {
        deleteApiDefinition(version);
        ApiBean api = version.getApi();
        String id = id(api.getOrganization().getId(), api.getId(), version.getVersion());
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION, id);
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_API_POLICIES, id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteApiDefinition(io.apiman.manager.api.beans.apis.ApiVersionBean)
     */
    @Override
    public void deleteApiDefinition(ApiVersionBean version) throws StorageException {
        String id = id(version.getApi().getOrganization().getId(), version.getApi().getId(), version.getVersion()) + ":def"; //$NON-NLS-1$
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_API_DEFINITION, id);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlan(io.apiman.manager.api.beans.plans.PlanBean)
     */
    @Override
    @SuppressWarnings("nls")
    public void deletePlan(PlanBean plan) throws StorageException {
        String planId = plan.getId().replace('"', '_');
        String orgId = plan.getOrganization().getId().replace('"', '_');

        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        List<QueryBuilder> shouldFilters = qb.should();

        BoolQueryBuilder subBoolQuery1 = QueryBuilders.boolQuery();
        List<QueryBuilder> subBoolQuery1Filter = subBoolQuery1.filter();
        subBoolQuery1Filter.add(QueryBuilders.termQuery("entityId", planId));
        subBoolQuery1Filter.add(QueryBuilders.termQuery("entityType", AuditEntityType.Plan.name()));
        subBoolQuery1Filter.add(QueryBuilders.termQuery("organizationId", orgId));
        shouldFilters.add(subBoolQuery1);

        BoolQueryBuilder subBoolQuery2 = QueryBuilders.boolQuery();
        List<QueryBuilder> subBoolQuery2Filter = subBoolQuery2.filter();
        subBoolQuery2Filter.add(QueryBuilders.termQuery("planId", planId));
        subBoolQuery2Filter.add(QueryBuilders.termQuery("organizationId", orgId));
        shouldFilters.add(subBoolQuery2);

        BoolQueryBuilder subBoolQuery3 = QueryBuilders.boolQuery();
        List<QueryBuilder> subBoolQuery3Filter = subBoolQuery3.filter();
        subBoolQuery3Filter.add(QueryBuilders.termQuery("entityId", planId));
        subBoolQuery3Filter.add(QueryBuilders.termQuery("type", AuditEntityType.Plan.name()));
        shouldFilters.add(subBoolQuery3);

        try {
            String[] indexNames = {EsConstants.INDEX_MANAGER_POSTFIX_AUDIT_ENTRY, EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION, EsConstants.INDEX_MANAGER_POSTFIX_PLAN_POLICIES};
            BulkByScrollResponse response = getDeleteByQueryResponse(qb, indexNames);

            if (response.getStatus().getSuccessfullyProcessed() != response.getStatus().getTotal()) {
                throw new StorageException("Could not delete all plan entries by query");
            }
        } catch (Exception e) {
            throw new StorageException(e);
        }
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLAN, id(plan.getOrganization().getId(), plan.getId()));
    }

    private BulkByScrollResponse getDeleteByQueryResponse(BoolQueryBuilder query, String[] indexNames) throws IOException {
        for (int i = 0; i < indexNames.length; i++) {
            indexNames[i] = getFullIndexName(indexNames[i]);
        }

        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(indexNames);
        deleteByQueryRequest.setQuery(query);
        deleteByQueryRequest.setRefresh(true);

        return getClient().deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
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
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION, id(plan.getOrganization().getId(), plan.getId(), version.getVersion()));
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
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_GATEWAY, gateway.getId());
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePlugin(io.apiman.manager.api.beans.plugins.PluginBean)
     */
    @Override
    public void deletePlugin(PluginBean plugin) throws StorageException {
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLUGIN, String.valueOf(plugin.getId()));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteDownload(io.apiman.manager.api.beans.download.DownloadBean)
     */
    @Override
    public void deleteDownload(DownloadBean download) throws StorageException {
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_DOWNLOAD, download.getId());
    }

    @Override
    public void deleteDeveloper(DeveloperBean developer) throws StorageException {
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_DEVELOPER, developer.getId());
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deletePolicyDefinition(io.apiman.manager.api.beans.policies.PolicyDefinitionBean)
     */
    @Override
    public void deletePolicyDefinition(PolicyDefinitionBean policyDef) throws StorageException {
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_POLICY_DEF, policyDef.getId());
    }

    /* (non-Javadoc)
     * @see io.apiman.manager.api.core.IStorage#deleteRole(io.apiman.manager.api.beans.idm.RoleBean)
     */
    @Override
    public void deleteRole(RoleBean role) throws StorageException {
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_ROLE, role.getId());
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getOrganization(java.lang.String)
     */
    @Override
    public OrganizationBean getOrganization(String id) throws StorageException {
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_ORGANIZATION, id);
        return EsMarshalling.unmarshallOrganization(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getClient(java.lang.String, java.lang.String)
     */
    @Override
    public ClientBean getClient(String organizationId, String id) throws StorageException {
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT, id(organizationId, id));
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
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION, id(organizationId, clientId, version));
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
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT, String.valueOf(id));
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
        ClientVersionBean cvb = getClientVersion(clientOrgId, clientId, clientVersion);
        ApiVersionBean svb = getApiVersion(apiOrgId, apiId, apiVersion);
        PlanVersionBean pvb = getPlanVersion(apiOrgId, planId, planVersion);
        contract.setClient(cvb);
        contract.setPlan(pvb);
        contract.setApi(svb);
        return contract;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getApi(java.lang.String, java.lang.String)
     */
    @Override
    public ApiBean getApi(String organizationId, String id) throws StorageException {
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_API, id(organizationId, id));
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
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION, id(organizationId, apiId, version));
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
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_API_DEFINITION, id);
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
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLAN, id(organizationId, id));
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
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION, id(organizationId, planId, version));
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
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_GATEWAY, id);
        return EsMarshalling.unmarshallGateway(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getDownload(java.lang.String)
     */
    @Override
    public DownloadBean getDownload(String id) throws StorageException {
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_DOWNLOAD, id);
        return EsMarshalling.unmarshallDownload(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getDeveloper(String)
     */
    @Override
    public DeveloperBean getDeveloper(String id) throws StorageException {
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_DEVELOPER, id);
        if (source == null) {
            return null;
        }
        DeveloperBean developerBean = EsMarshalling.unmarshallDeveloper(source);
        return developerBean;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlugin(long)
     */
    @Override
    public PluginBean getPlugin(long id) throws StorageException {
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_PLUGIN, String.valueOf(id));
        return EsMarshalling.unmarshallPlugin(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getPlugin(java.lang.String, java.lang.String)
     */
    @Override
    public PluginBean getPlugin(String groupId, String artifactId) throws StorageException {
        try {
            @SuppressWarnings("nls")
            BoolQueryBuilder qb = QueryBuilders.boolQuery();
            List<QueryBuilder> filter = qb.filter();
            filter.add(QueryBuilders.termQuery("groupId", groupId));
            filter.add(QueryBuilders.termQuery("artifactId", artifactId));

            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(50);
            List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_PLUGIN, builder);
            if (hits.size() == 1) {
                Map<String,Object> hit = hits.iterator().next().getSourceAsMap();
                return EsMarshalling.unmarshallPlugin(hit);
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
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_POLICY_DEF, id);
        return EsMarshalling.unmarshallPolicyDefinition(source);

    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getRole(java.lang.String)
     */
    @Override
    public RoleBean getRole(String id) throws StorageException {
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_ROLE, id);
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
        QueryBuilder query =  QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("deleted", false));
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .fetchSource(fields, null)
                .query(query)
                .sort(new FieldSortBuilder("name").order(SortOrder.ASC))
                .size(200);
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_PLUGIN, builder);
        List<PluginSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            PluginSummaryBean bean = EsMarshalling.unmarshallPluginSummary(hit.getSourceAsMap());
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
        String[] fields = {"id", "name", "description", "type"};
        SearchSourceBuilder builder = new SearchSourceBuilder().fetchSource(fields, null)
                .sort(new FieldSortBuilder("name").order(SortOrder.ASC)).size(100); //$NON-NLS-1$
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_GATEWAY, builder);
        List<GatewaySummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            GatewaySummaryBean bean = EsMarshalling.unmarshallGatewaySummary(hit.getSourceAsMap());
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
        return find(criteria, EsConstants.INDEX_MANAGER_POSTFIX_ORGANIZATION, new IUnmarshaller<OrganizationSummaryBean>() {
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
        return find(criteria, EsConstants.INDEX_MANAGER_POSTFIX_CLIENT, new IUnmarshaller<ClientSummaryBean>() {
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
        return find(criteria, EsConstants.INDEX_MANAGER_POSTFIX_API, new IUnmarshaller<ApiSummaryBean>() {
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
        criteria.addFilter("organizationId", organizationId, SearchCriteriaFilterOperator.eq);
        return find(criteria, EsConstants.INDEX_MANAGER_POSTFIX_PLAN, new IUnmarshaller<PlanSummaryBean>() {
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

        return find(criteria, EsConstants.INDEX_MANAGER_POSTFIX_AUDIT_ENTRY, new IUnmarshaller<AuditEntryBean>() {
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

        return find(criteria, EsConstants.INDEX_MANAGER_POSTFIX_AUDIT_ENTRY, new IUnmarshaller<AuditEntryBean>() {
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
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = query.should();
        for (String orgId : organizationIds.toArray(new String[organizationIds.size()])) {
            filter.add(QueryBuilders.termQuery("id", orgId));
        }

        @SuppressWarnings("nls")
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort(new FieldSortBuilder("name").order(SortOrder.ASC))
                .query(query)
                .size(500);
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_ORGANIZATION, builder);
        List<OrganizationSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            OrganizationSummaryBean bean = EsMarshalling.unmarshallOrganizationSummary(hit.getSourceAsMap());
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
                .sort("organizationName", SortOrder.ASC)
                .sort("name", SortOrder.ASC)
                .size(500);
        TermsQueryBuilder query = QueryBuilders.termsQuery("organizationId", organizationIds.toArray(new String[organizationIds.size()])); //$NON-NLS-1$
        builder.query(query);
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT, builder);
        List<ClientSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            ClientSummaryBean bean = EsMarshalling.unmarshallClientSummary(hit.getSourceAsMap());
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
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = query.filter();
        filter.add(QueryBuilders.termQuery("organizationId", organizationId));
        filter.add(QueryBuilders.termQuery("clientId", clientId));
        @SuppressWarnings("nls")

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort(new FieldSortBuilder("createdOn").order(SortOrder.DESC))
                .query(query)
                .size(500);
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION, builder);
        List<ClientVersionSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            ClientVersionSummaryBean bean = EsMarshalling.unmarshallClientVersionSummary(hit.getSourceAsMap());
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
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = query.filter();
        filter.add(QueryBuilders.termQuery("clientOrganizationId", organizationId));
        filter.add(QueryBuilders.termQuery("clientId", clientId));
        filter.add(QueryBuilders.termQuery("clientVersion", version));
        @SuppressWarnings("nls")

        SearchSourceBuilder builder = new SearchSourceBuilder().query(query)
                .sort(new FieldSortBuilder("apiOrganizationId").order(SortOrder.ASC))
                .sort(new FieldSortBuilder("apiId").order(SortOrder.ASC))
                .size(500);
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT, builder);
        List<ContractSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            ContractSummaryBean bean = EsMarshalling.unmarshallContractSummary(hit.getSourceAsMap());
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
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = query.filter();
        filter.add(QueryBuilders.termQuery("clientOrganizationId", organizationId));
        filter.add(QueryBuilders.termQuery("clientId", clientId));
        filter.add(QueryBuilders.termQuery("clientVersion", version));
        @SuppressWarnings("nls")

        SearchSourceBuilder builder = new SearchSourceBuilder().query(query)
                .sort(new FieldSortBuilder("id").order(SortOrder.ASC))
                .size(500);
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT, builder);
        ApiRegistryBean registry = new ApiRegistryBean();
        for (SearchHit hit : hits) {
            ApiEntryBean bean = EsMarshalling.unmarshallApiEntry(hit.getSourceAsMap());
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
                .sort(new FieldSortBuilder("organizationName").order(SortOrder.ASC))
                .sort(new FieldSortBuilder("name").order(SortOrder.ASC))
                .size(500);
        TermsQueryBuilder query = QueryBuilders.termsQuery("organizationId", organizationIds.toArray(new String[organizationIds.size()])); //$NON-NLS-1$
        builder.query(query);

        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_API, builder);
        List<ApiSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            ApiSummaryBean bean = EsMarshalling.unmarshallApiSummary(hit.getSourceAsMap());
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
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = query.filter();
        filter.add(QueryBuilders.termQuery("organizationId", organizationId));
        filter.add(QueryBuilders.termQuery("apiId", apiId));

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort(new FieldSortBuilder("createdOn").order(SortOrder.ASC))
                .query(query)
                .size(500);

        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION, builder);
        List<ApiVersionSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            ApiVersionSummaryBean bean = EsMarshalling.unmarshallApiVersionSummary(hit.getSourceAsMap());
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
                .sort(new FieldSortBuilder("organizationName").order(SortOrder.ASC))
                .sort(new FieldSortBuilder("name").order(SortOrder.ASC))
                .size(500);
        TermsQueryBuilder query = QueryBuilders.termsQuery("organizationId", organizationIds.toArray(new String[organizationIds.size()])); //$NON-NLS-1$
        builder.query(query);
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_PLAN, builder);
        List<PlanSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            PlanSummaryBean bean = EsMarshalling.unmarshallPlanSummary(hit.getSourceAsMap());
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
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = query.filter();
        filter.add(QueryBuilders.termQuery("organizationId", organizationId));
        filter.add(QueryBuilders.termQuery("planId", planId));
        @SuppressWarnings("nls")

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .sort(new FieldSortBuilder("createdOn").order(SortOrder.DESC)) //$NON-NLS-1$
                .query(query)
                .size(500);
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION, builder); //$NON-NLS-1$
        List<PlanVersionSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            PlanVersionSummaryBean bean = EsMarshalling.unmarshallPlanVersionSummary(hit.getSourceAsMap());
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

        QueryBuilder query =  QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("deleted", false));

        SearchSourceBuilder builder = new SearchSourceBuilder()
                .fetchSource(fields, null)
                .query(query)
                .sort(new FieldSortBuilder("name").order(SortOrder.ASC)) //$NON-NLS-1$
                .size(100);
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_POLICY_DEF, builder); //$NON-NLS-1$
        List<PolicyDefinitionSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            PolicyDefinitionSummaryBean bean = EsMarshalling.unmarshallPolicyDefinitionSummary(hit.getSourceAsMap());
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

        BoolQueryBuilder query = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = query.filter();
        filter.add(QueryBuilders.termQuery("apiOrganizationId", organizationId));
        filter.add(QueryBuilders.termQuery("apiId", apiId));
        filter.add(QueryBuilders.termQuery("apiVersion", version));

        SearchSourceBuilder builder = new SearchSourceBuilder().query(query)
                .sort(new FieldSortBuilder("clientOrganizationId").order(SortOrder.ASC))
                .sort(new FieldSortBuilder("clientId").order(SortOrder.ASC))
                .size(500);
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT, builder); //$NON-NLS-1$
        List<ContractSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            ContractSummaryBean bean = EsMarshalling.unmarshallContractSummary(hit.getSourceAsMap());
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
        QueryBuilder qb = QueryBuilders.termQuery("pluginId", pluginId);
        @SuppressWarnings("nls")
        String[] fields = {"id", "policyImpl", "name", "description", "icon", "pluginId", "formType"};
        SearchSourceBuilder builder = new SearchSourceBuilder()
                .fetchSource(fields, null)
                .query(qb)
                .sort(new FieldSortBuilder("name").order(SortOrder.ASC))
                .size(100); //$NON-NLS-1$
        List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_POLICY_DEF, builder); //$NON-NLS-1$
        List<PolicyDefinitionSummaryBean> rval = new ArrayList<>(hits.size());
        for (SearchHit hit : hits) {
            PolicyDefinitionSummaryBean bean = EsMarshalling.unmarshallPolicyDefinitionSummary(hit.getSourceAsMap());
            rval.add(bean);
        }
        return rval;
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#createUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void createUser(UserBean user) throws StorageException {
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_USER, user.getUsername(), EsMarshalling.marshall(user)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getUser(java.lang.String)
     */
    @Override
    public UserBean getUser(String userId) throws StorageException {
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_USER, userId); //$NON-NLS-1$
        return EsMarshalling.unmarshallUser(source);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#updateUser(io.apiman.manager.api.beans.idm.UserBean)
     */
    @Override
    public void updateUser(UserBean user) throws StorageException {
        updateEntity(EsConstants.INDEX_MANAGER_POSTFIX_USER, user.getUsername(), EsMarshalling.marshall(user)); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorageQuery#findUsers(io.apiman.manager.api.beans.search.SearchCriteriaBean)
     */
    @Override
    public SearchResultsBean<UserBean> findUsers(SearchCriteriaBean criteria) throws StorageException {
        return find(criteria, EsConstants.INDEX_MANAGER_POSTFIX_USER,  new IUnmarshaller<UserBean>() { //$NON-NLS-1$
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
        return find(criteria, EsConstants.INDEX_MANAGER_POSTFIX_ROLE, new IUnmarshaller<RoleBean>() { //$NON-NLS-1$
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
        indexEntity(EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP, id, EsMarshalling.marshall(membership));
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getMembership(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public RoleMembershipBean getMembership(String userId, String roleId, String organizationId) throws StorageException {
        String id = id(organizationId, userId, roleId);
        Map<String, Object> source = getEntity(EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP, id);
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
        deleteEntity(EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP, id); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#deleteMemberships(java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings("nls")
    public void deleteMemberships(String userId, String organizationId) throws StorageException {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = query.filter();
        filter.add(QueryBuilders.termQuery("organizationId", organizationId));
        filter.add(QueryBuilders.termQuery("userId", userId));

        try {
            String[] indexNames = {EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP};
            BulkByScrollResponse response = getDeleteByQueryResponse(query, indexNames);

            if (response.getStatus().getSuccessfullyProcessed() != response.getStatus().getTotal()) {
                throw new StorageException("Could not delete all org membership entries by query");
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
            QueryBuilder qb = QueryBuilders.termQuery("userId", userId);

            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(500);
            List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP, builder); //$NON-NLS-1$
            Set<RoleMembershipBean> rval = new HashSet<>();
            for (SearchHit hit : hits) {
                RoleMembershipBean roleMembership = EsMarshalling.unmarshallRoleMembership(hit.getSourceAsMap());
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
            BoolQueryBuilder qb = QueryBuilders.boolQuery();
            List<QueryBuilder> filter = qb.filter();
            filter.add(QueryBuilders.termQuery("userId", userId));
            filter.add(QueryBuilders.termQuery("organizationId", organizationId));
            @SuppressWarnings("nls")
            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(500);
            List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP, builder); //$NON-NLS-1$
            Set<RoleMembershipBean> rval = new HashSet<>();
            for (SearchHit hit : hits) {
                RoleMembershipBean roleMembership = EsMarshalling.unmarshallRoleMembership(hit.getSourceAsMap());
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
            QueryBuilder qb = QueryBuilders.termQuery("organizationId", organizationId);
            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(500);
            List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP, builder); //$NON-NLS-1$
            Set<RoleMembershipBean> rval = new HashSet<>();
            for (SearchHit hit : hits) {
                RoleMembershipBean roleMembership = EsMarshalling.unmarshallRoleMembership(hit.getSourceAsMap());
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
            QueryBuilder qb = QueryBuilders.termQuery("userId", userId);
            SearchSourceBuilder builder = new SearchSourceBuilder().query(qb).size(500);
            List<SearchHit> hits = listEntities(EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP, builder); //$NON-NLS-1$
            Set<PermissionBean> rval = new HashSet<>(hits.size());
            if (!hits.isEmpty()) {
                for (SearchHit hit : hits) {
                    Map<String, Object> source = hit.getSourceAsMap();
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
     * @param type the entity type
     * @param id the entity id
     * @param sourceEntity the source entity
     * @throws StorageException thows a Storage Exception if something goes wrong during storing the data
     */
    @SuppressWarnings("nls")
    private void indexEntity(String type, String id, XContentBuilder sourceEntity)
            throws StorageException {
        try {
            String json = Strings.toString(sourceEntity);
            String fullIndexName = getFullIndexName(type);

            IndexRequest indexRequest = new IndexRequest(fullIndexName).id(id).source(json, XContentType.JSON);
            // WAIT_UNTIL same as "wait_for" => Leave this request open until a refresh has made the contents of this request visible to search
            indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

            IndexResponse indexResponse = getClient().index(indexRequest, RequestOptions.DEFAULT);

            if (!indexResponse.status().equals(RestStatus.CREATED) && !indexResponse.status().equals(RestStatus.OK)) {
                throw new StorageException("Failed to index document " + id + " of type " + type + " - " + "status: " + indexResponse.status());
            }
        } catch (StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    private String getFullIndexName(String indexPostFix) {
        return (getIndexPrefix() + "_" + indexPostFix).toLowerCase();
    }

    /**
     * Gets an entity.  Callers must unmarshal the resulting map.
     * @param type
     * @param id
     * @throws StorageException
     */
    private Map<String, Object> getEntity(String type, String id) throws StorageException {
        try {
            GetRequest getRequest = new GetRequest(getFullIndexName(type), id);
            GetResponse response = getClient().get(getRequest, RequestOptions.DEFAULT);

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
     * @param searchSourceBuilder
     * @throws StorageException
     * @return
     */
    private List<SearchHit> listEntities(String type,
                                     SearchSourceBuilder searchSourceBuilder) throws StorageException {
        try {
            String fullIndexName = getFullIndexName(type);
            SearchResponse response = getClient().search(new SearchRequest(fullIndexName).source(searchSourceBuilder), RequestOptions.DEFAULT);
            return Arrays.asList(response.getHits().getHits());
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
            final DeleteRequest deleteRequest = new DeleteRequest(getFullIndexName(type), id);
            // WAIT_UNTIL same as "wait_for" => Leave this request open until a refresh has made the contents of this request visible to search
            deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

            DeleteResponse response = getClient().delete(deleteRequest, RequestOptions.DEFAULT);
            if (!response.status().equals(RestStatus.OK)) {
                throw new StorageException("Document could not be deleted because it did not exist - expected Status Code " + RestStatus.OK + " given: " + response.status()); //$NON-NLS-1$
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
            String doc = Strings.toString(source);
            String fullIndexName = getFullIndexName(type);
            IndexRequest indexRequest = new IndexRequest(fullIndexName).id(id).source(doc, XContentType.JSON);
            // WAIT_UNTIL same as "wait_for" => Leave this request open until a refresh has made the contents of this request visible to search
            indexRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
            getClient().index(indexRequest, RequestOptions.DEFAULT);
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
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                List<QueryBuilder> andFilter = boolQuery.filter();
                int filterCount = 0;
                for (SearchCriteriaFilterBean filter : filters) {
                    String propertyName = filter.getName();
                    if (filter.getOperator() == SearchCriteriaFilterOperator.eq) {
                        andFilter.add(QueryBuilders.termQuery(propertyName, filter.getValue()));
                        filterCount++;
                    } else if (filter.getOperator() == SearchCriteriaFilterOperator.like) {
                        q = QueryBuilders.wildcardQuery(propertyName, filter.getValue().toLowerCase().replace('%', '*'));
                    } else if (filter.getOperator() == SearchCriteriaFilterOperator.bool_eq) {
                        andFilter.add(QueryBuilders.termQuery(propertyName, "true".equals(filter.getValue()))); //$NON-NLS-1$
                        filterCount++;
                    }
                    // TODO implement the other filter operators here!
                }

                if (filterCount > 0) {
                    q = boolQuery;
                }
            }

            builder.query(q);

            String fullIndexName = getFullIndexName(type);
            SearchResponse response = getClient().search(new SearchRequest(fullIndexName).source(builder), RequestOptions.DEFAULT);

            SearchHits thehits = response.getHits();
            rval.setTotalSize((int) thehits.getTotalHits().value);

            for (SearchHit hit : thehits.getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
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
        String docType = EsConstants.INDEX_MANAGER_POSTFIX_PLAN_POLICIES; //$NON-NLS-1$
        if (type == PolicyType.Api) {
            docType = EsConstants.INDEX_MANAGER_POSTFIX_API_POLICIES; //$NON-NLS-1$
        } else if (type == PolicyType.Client) {
            docType = EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_POLICIES; //$NON-NLS-1$
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
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_ORGANIZATION, new IUnmarshaller<OrganizationBean>() { //$NON-NLS-1$
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
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_PLAN, new IUnmarshaller<PlanBean>() { //$NON-NLS-1$
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
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT, new IUnmarshaller<ClientBean>() { //$NON-NLS-1$
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
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_API, new IUnmarshaller<ApiBean>() { //$NON-NLS-1$
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
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = qb.filter();
        filter.add(QueryBuilders.termQuery("organizationId", organizationId));
        filter.add(QueryBuilders.termQuery("planId", planId));
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION, new IUnmarshaller<PlanVersionBean>() { //$NON-NLS-1$
            @Override
            public PlanVersionBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallPlanVersion(source);
            }
        }, qb);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllApiVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<ApiVersionBean> getAllApiVersions(String organizationId, String apiId)
            throws StorageException {
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = qb.filter();
        filter.add(QueryBuilders.termQuery("organizationId", organizationId));
        filter.add(QueryBuilders.termQuery("apiId", apiId));

        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION, new IUnmarshaller<ApiVersionBean>() { //$NON-NLS-1$
            @Override
            public ApiVersionBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallApiVersion(source);
            }
        }, qb);
    }

    /**
     * Refreshes an index
     * @param indexPostfix the index postfix name
     */
    public void refresh(String indexPostfix) throws IOException {
        getClient().indices().refresh(new RefreshRequest(getFullIndexName(indexPostfix)), RequestOptions.DEFAULT); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllClientVersions(java.lang.String, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<ClientVersionBean> getAllClientVersions(String organizationId,
            String clientId) throws StorageException {
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = qb.filter();
        filter.add(QueryBuilders.termQuery("organizationId", organizationId));
        filter.add(QueryBuilders.termQuery("clientId", clientId));

        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION, new IUnmarshaller<ClientVersionBean>() { //$NON-NLS-1$
            @Override
            public ClientVersionBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallClientVersion(source);
            }
        }, qb);
    }

    /**
     * @see io.apiman.manager.api.core.IStorage#getAllContracts(java.lang.String, java.lang.String, java.lang.String)
     */
    @SuppressWarnings("nls")
    @Override
    public Iterator<ContractBean> getAllContracts(String organizationId, String clientId, String version) throws StorageException {

        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = qb.filter();
        filter.add(QueryBuilders.termQuery("clientOrganizationId", organizationId));
        filter.add(QueryBuilders.termQuery("clientId", clientId));
        filter.add(QueryBuilders.termQuery("clientVersion", version));

        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT, new IUnmarshaller<ContractBean>() { //$NON-NLS-1$
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
        }, qb);
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
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_GATEWAY, new IUnmarshaller<GatewayBean>() { //$NON-NLS-1$
            @Override
            public GatewayBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallGateway(source);
            }
        });
    }

    @Override
    public Iterator<UserBean> getAllUsers() throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_USER, new IUnmarshaller<UserBean>() { //$NON-NLS-1$
            @Override
            public UserBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallUser(source);
            }
        });
    }

    @Override
    public Iterator<RoleBean> getAllRoles() throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_ROLE, new IUnmarshaller<RoleBean>() { //$NON-NLS-1$
            @Override
            public RoleBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallRole(source);
            }
        });
    }

    @Override
    public Iterator<DeveloperBean> getDevelopers() throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_DEVELOPER, new IUnmarshaller<DeveloperBean>() {
            @Override
            public DeveloperBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallDeveloper(source);
            }
        });
    }

    @Override
    public Iterator<ContractBean> getAllContracts(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_CONTRACT, EsMarshalling::unmarshallContract, matchOrgQuery(organizationBean.getId())); //$NON-NLS-1$
    }

    @Override
    public Iterator<ClientVersionBean> getAllClientVersions(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION, EsMarshalling::unmarshallClientVersion, matchOrgQuery(organizationBean.getId())); //$NON-NLS-1$
    }

    @Override
    public Iterator<ClientVersionBean> getAllClientVersions(OrganizationBean organizationBean, ClientStatus status, int lim) throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_CLIENT_VERSION, EsMarshalling::unmarshallClientVersion, matchOrgAndStatusQuery(organizationBean.getId(), status.name())); //$NON-NLS-1$
    }

    @Override
    public Iterator<ApiVersionBean> getAllApiVersions(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION, EsMarshalling::unmarshallApiVersion, matchOrgQuery(organizationBean.getId())); //$NON-NLS-1$
    }

    @Override
    public Iterator<ApiVersionBean> getAllApiVersions(OrganizationBean organizationBean, ApiStatus status, int lim) throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION, EsMarshalling::unmarshallApiVersion, matchOrgAndStatusQuery(organizationBean.getId(), status.name())); //$NON-NLS-1$
    }

    @Override
    public Iterator<PlanVersionBean> getAllPlanVersions(OrganizationBean organizationBean, int lim) throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION, EsMarshalling::unmarshallPlanVersion, matchOrgQuery(organizationBean.getId())); //$NON-NLS-1$
    }

    @Override
    public Iterator<PlanVersionBean> getAllPlanVersions(OrganizationBean organizationBean, PlanStatus status, int lim) throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_PLAN_VERSION, EsMarshalling::unmarshallPlanVersion, matchOrgAndStatusQuery(organizationBean.getId(), status.name())); //$NON-NLS-1$
    }

    @Override
    public Iterator<ApiVersionBean> getAllPublicApiVersions() throws StorageException {
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = qb.filter();
        filter.add(QueryBuilders.termQuery("publicAPI", true));

        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_API_VERSION, new IUnmarshaller<ApiVersionBean>() { //$NON-NLS-1$
            @Override
            public ApiVersionBean unmarshal(Map<String, Object> source) {
                ApiVersionBean apiVersion = EsMarshalling.unmarshallApiVersion(source);
                String organizationId = String.valueOf(source.get("organizationId"));
                String apiId = String.valueOf(source.get("apiId"));
                try {
                    apiVersion.setApi(getApi(organizationId, apiId));
                } catch (StorageException e) {
                    // Do nothing
                }
                return apiVersion;
            }
        }, qb);
    }

    @Override
    public Iterator<RoleMembershipBean> getAllMemberships(String organizationId) throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_ROLE_MEMBERSHIP, new IUnmarshaller<RoleMembershipBean>() { //$NON-NLS-1$
            @Override
            public RoleMembershipBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallRoleMembership(source);
            }
        }, matchOrgQuery(organizationId));
    }

    @Override
    @SuppressWarnings("nls")
    public Iterator<AuditEntryBean> getAllAuditEntries(String organizationId) throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_AUDIT_ENTRY, new IUnmarshaller<AuditEntryBean>() {
            @Override
            public AuditEntryBean unmarshal(Map<String, Object> source) {
                return EsMarshalling.unmarshallAuditEntry(source);
            }
        }, matchOrgQuery(organizationId), "id");
    }

    @Override
    public Iterator<PluginBean> getAllPlugins() throws StorageException {
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_PLUGIN, new IUnmarshaller<PluginBean>() { //$NON-NLS-1$
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
        return getAll(EsConstants.INDEX_MANAGER_POSTFIX_POLICY_DEF, new IUnmarshaller<PolicyDefinitionBean>() { //$NON-NLS-1$
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
        QueryBuilder query = matchAllQuery();
        return getAll(entityType, unmarshaller, query);
    }

    /**
     * Returns an iterator over all instances of the given entity type with index sort (_doc).
     * @param entityType
     * @param unmarshaller
     * @param query
     * @throws StorageException
     */
    private <T> Iterator<T> getAll(String entityType, IUnmarshaller<T> unmarshaller, QueryBuilder query) throws StorageException {
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
    private <T> Iterator<T> getAll(String entityType, IUnmarshaller<T> unmarshaller, QueryBuilder query, String sort) throws StorageException {
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

        private final QueryBuilder query;
        private final String sort;
        private final String entityType;
        private final IUnmarshaller<T> unmarshaller;
        private String scrollId = null;
        private List<SearchHit> hits;
        private int nextHitIdx;

        /**
         * Constructor.
         * @param entityType the entity type
         * @param unmarshaller the unmarshaller
         * @param query the query
         * @throws StorageException when storage fails
         */
        public EntityIterator(String entityType, IUnmarshaller<T> unmarshaller, QueryBuilder query) throws StorageException {
            this.entityType = entityType;
            this.unmarshaller = unmarshaller;
            this.query = query;
            this.sort = "_doc";
            initScroll();
            this.nextHitIdx = 0;
        }

        public EntityIterator(String entityType, IUnmarshaller<T> unmarshaller, QueryBuilder query, String sort) throws StorageException {
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
            SearchHit hit = hits.get(nextHitIdx++);
            return unmarshaller.unmarshal(hit.getSourceAsMap());
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
                SearchSourceBuilder builder = new SearchSourceBuilder();
                builder.query(query);
                builder.sort(sort);

                String fullIndexName = getFullIndexName(entityType);
                SearchRequest searchRequest = new SearchRequest(fullIndexName);
                searchRequest.source(builder);
                searchRequest.scroll(TimeValue.timeValueMinutes(1L));

                SearchResponse response = getClient().search(searchRequest, RequestOptions.DEFAULT);

                if (!response.status().equals(RestStatus.OK)) {
                    throw new StorageException("Scrolled query failed - status expected " + RestStatus.OK + " but was " + response.status());
                }
                this.scrollId = response.getScrollId();
                this.hits = Arrays.asList (response.getHits().getHits());

            } catch (IOException e) {
                throw new StorageException(e);
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private void fetch() throws StorageException {
            try {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(this.scrollId);
                scrollRequest.scroll(TimeValue.timeValueMinutes(1L));

                SearchResponse response = getClient().scroll(scrollRequest, RequestOptions.DEFAULT);

                if (!response.status().equals(RestStatus.OK)) {
                    throw new StorageException("Scrolled fetch failed - status expected " + RestStatus.OK + " but was " + response.status());
                }

                this.hits = Arrays.asList(response.getHits().getHits());
            } catch (IOException e) {
                throw new StorageException(e);
            }
        }

    }

    @SuppressWarnings("nls")
    private QueryBuilder matchOrgAndStatusQuery(String organizationId, String status) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        List<QueryBuilder> filter = queryBuilder.filter();
        filter.add(QueryBuilders.termQuery("organizationId", organizationId));
        filter.add(QueryBuilders.termQuery("status", status));
        return queryBuilder;
    }

    /**
     * @return an ES query to match all documents
     */
    @SuppressWarnings("nls")
    private QueryBuilder matchAllQuery() {
        return QueryBuilders.matchAllQuery();
    }

    @SuppressWarnings("nls")
    private QueryBuilder matchOrgQuery(String organizationId) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.filter().add(QueryBuilders.termQuery("organizationId", organizationId));
        return queryBuilder;
    }

    /**
     * @see AbstractEsComponent#getDefaultIndexPrefix()
     */
    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.MANAGER_INDEX_NAME;
    }

    /**
     * @see AbstractEsComponent#getDefaultIndices()
     * @return default indices
     */
    @Override
    protected List<String> getDefaultIndices() {
        return Arrays.asList(EsConstants.MANAGER_INDEX_POSTFIXES);
    }

    /**
     * @return the indexName
     */
    public String getIndexPrefix() {
        return indexPrefix;
    }

    /**
     * @param indexPrefix the indexName to set
     */
    public void setIndexPrefix(String indexPrefix) {
        this.indexPrefix = indexPrefix;
    }

}
