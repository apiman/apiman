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
package io.apiman.gateway.engine.es;

import io.apiman.common.es.util.AbstractEsComponent;
import io.apiman.common.es.util.EsConstants;
import io.apiman.common.es.util.EsUtils;
import io.apiman.common.es.util.builder.index.EsIndexProperties;
import io.apiman.common.logging.DefaultDelegateFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.exceptions.ApiNotFoundException;
import io.apiman.gateway.engine.beans.exceptions.ApiRetiredException;
import io.apiman.gateway.engine.beans.exceptions.ClientNotFoundException;
import io.apiman.gateway.engine.beans.exceptions.NoContractFoundException;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.es.i18n.Messages;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.stream.Collectors;

import static io.apiman.gateway.engine.storage.util.BackingStoreUtil.JSON_MAPPER;

/**
 * An implementation of the Registry that uses elasticsearch as a storage
 * mechanism.
 *
 * @author eric.wittmann@redhat.com
 */
public class EsRegistry extends AbstractEsComponent implements IRegistry {

    private IApimanLogger logger = new DefaultDelegateFactory().createLogger(EsRegistry.class);

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public EsRegistry(Map<String, String> config) {
        super(config);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#publishApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishApi(final Api api, final IAsyncResultHandler<Void> handler) {
        try {
            String id = getApiId(api);
            IndexRequest indexRequest = new IndexRequest(getIndexPrefix() + EsConstants.INDEX_APIS)
                    .id(id)
                    .source(JSON_MAPPER.writeValueAsBytes(api), XContentType.JSON)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

            IndexResponse response = getClient().index(indexRequest, RequestOptions.DEFAULT);

            if (response.status().equals(RestStatus.CREATED) || response.status().equals(RestStatus.OK)) {
                handler.handle(AsyncResultImpl.create((Void) null));
            } else {
                throw new IOException(response.getResult().toString());
            }
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(
                    new PublishingException(Messages.i18n.format("EsRegistry.ErrorPublishingApi"), e),  //$NON-NLS-1$
                    Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#retireApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireApi(Api api, final IAsyncResultHandler<Void> handler) {
        final String id = getApiId(api);

        try {
            DeleteRequest deleteRequest = new DeleteRequest(getIndexPrefix() + EsConstants.INDEX_APIS)
                    .id(id)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

            DeleteResponse response = getClient().delete(deleteRequest, RequestOptions.DEFAULT);

            if (response.status().equals(RestStatus.OK)) {
                handler.handle(AsyncResultImpl.create((Void) null));
            } else {
                handler.handle(AsyncResultImpl.create(new ApiNotFoundException(Messages.i18n.format("EsRegistry.ApiNotFound")))); //$NON-NLS-1$
            }
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("EsRegistry.ErrorRetiringApi"), e))); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#registerClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerClient(final Client client, final IAsyncResultHandler<Void> handler) {
        try {
            // Validate the client and populate the api map with apis found during validation.
            validateClient(client);

            String id = getClientId(client);
            IndexRequest indexRequest = new IndexRequest(getIndexPrefix() + EsConstants.INDEX_CLIENTS)
                    .source(JSON_MAPPER.writeValueAsBytes(client), XContentType.JSON)
                    .id(id)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

            IndexResponse response = getClient().index(indexRequest, RequestOptions.DEFAULT);

            if(!response.status().equals(RestStatus.CREATED) && !response.status().equals(RestStatus.OK)) {
                throw new IOException("Response status was " + response.status() + " instead of " + RestStatus.CREATED + " or " + RestStatus.OK);
            } else {
                handler.handle(AsyncResultImpl.create((Void) null));
            }
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(
                    new RegistrationException(Messages.i18n.format("EsRegistry.ErrorRegisteringClient"), e),  //$NON-NLS-1$
                    Void.class));
        } catch (RuntimeException re) {
            handler.handle(AsyncResultImpl.create(re, Void.class));
        }
    }

    /**
     * Validate that the client should be registered.
     * @param client the elasticsearch client
     */
    private void validateClient(Client client) throws RegistrationException {
        Set<Contract> contracts = client.getContracts();
        if (contracts.isEmpty()) {
            throw new NoContractFoundException(Messages.i18n.format("EsRegistry.NoContracts")); //$NON-NLS-1$
        }
        for (Contract contract : contracts) {
            validateContract(contract);
        }
    }

    /**
     * Ensures that the api referenced by the Contract at the head of
     * the iterator actually exists (is published).
     * @param contract the contract between client and api
     */
    private void validateContract(final Contract contract)
            throws RegistrationException {
        final String id = getApiId(contract);

        try {
            GetRequest getRequest = new GetRequest(getIndexPrefix() + EsConstants.INDEX_APIS).id(id);
            GetResponse response = getClient().get(getRequest, RequestOptions.DEFAULT);

            if (!response.isExists()) {
                String apiId = contract.getApiId();
                String orgId = contract.getApiOrgId();
                throw new ApiNotFoundException(Messages.i18n.format("EsRegistry.ApiNotFoundInOrg", apiId, orgId));  //$NON-NLS-1$
            }
        } catch (IOException e) {
            throw new RegistrationException(Messages.i18n.format("EsRegistry.ErrorValidatingClient"), e); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterClient(final Client client, final IAsyncResultHandler<Void> handler) {
        try {
            final Client lclient = lookupClient(client.getOrganizationId(), client.getClientId(), client.getVersion());
            final String id = getClientId(lclient);

            DeleteRequest deleteRequest = new DeleteRequest(getIndexPrefix() + EsConstants.INDEX_CLIENTS)
                    .id(id)
                    .setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);

            DeleteResponse response = getClient().delete(deleteRequest, RequestOptions.DEFAULT);
            if (response.status().equals(RestStatus.OK)) {
                handler.handle(AsyncResultImpl.create((Void) null));
            } else {
                handler.handle(AsyncResultImpl.create(new ApiNotFoundException(Messages.i18n.format("EsRegistry.ClientNotFound")))); //$NON-NLS-1$
            }
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("EsRegistry.ErrorUnregisteringClient"), e), Void.class)); //$NON-NLS-1$
        } catch (RuntimeException e) {
            handler.handle(AsyncResultImpl.create(e));
        }
    }

    /**
     * Searches for a client by its orgid:clientId:version and returns it.
     * @param orgId the organization id
     * @param clientId the client id
     * @param version the version
     */
    @SuppressWarnings("nls") // Do beans need escaping or will that be done 'automatically'. Test it. Strings do, but probably only quotes?
    private Client lookupClient(String orgId, String clientId, String version) {
        String query = "{" +
                "  \"query\": {" +
                "        \"bool\": {" +
                "            \"filter\": [{" +
                "                    \"term\": {" +
                "                        \"organizationId\": \"{{organizationId}}\" " + // orgId
                "                    }" +
                "          }," +
                "          {" +
                "                    \"term\": {" +
                "                        \"clientId\": \"{{clientId}}\" " + // clientId
                "                    }" +
                "          }," +
                "          {" +
                "                    \"term\": {" +
                "                        \"version\": \"{{version}}\" " + // version
                "          }" +
                "      }" +
                "            ]" +
                "    }" +
                "  }" +
                "}";

        SearchTemplateRequest searchTemplateRequest = new SearchTemplateRequest();
        searchTemplateRequest.setRequest(new SearchRequest(getIndexPrefix() + EsConstants.INDEX_CLIENTS));
        searchTemplateRequest.setScriptType(ScriptType.INLINE);
        searchTemplateRequest.setScript(query);

        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("organizationId", orgId);
        scriptParams.put("clientId", clientId);
        scriptParams.put("version", version);

        searchTemplateRequest.setScriptParams(scriptParams);

        Client client;
        try {
            SearchTemplateResponse response = getClient().searchTemplate(searchTemplateRequest, RequestOptions.DEFAULT);
            SearchResponse searchResponse = response.getResponse();
            SearchHits hits = searchResponse.getHits();

            if (hits.getTotalHits().value == 0) {
                throw new IOException();
            }
            String sourceAsString = response.getResponse().getHits().getAt(0).getSourceAsString();
            client = JSON_MAPPER.readValue(sourceAsString, Client.class);

        } catch (IOException e) {
            throw new ClientNotFoundException(Messages.i18n.format("EsRegistry.ClientNotFound"), e);  //$NON-NLS-1$
        }

        return client;
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getApi(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getApi(String organizationId, String apiId, String apiVersion,
            IAsyncResultHandler<Api> handler) {
        String id = getApiId(organizationId, apiId, apiVersion);
        try {
            Api api = getApi(id);
            handler.handle(AsyncResultImpl.create(api));
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(e, Api.class));
        }
    }

    @Override
    public void getClient(String organizationId, String clientId, String clientVersion,
            IAsyncResultHandler<Client> handler) {
        try {
            Client client = lookupClient(organizationId, clientId, clientVersion);
            handler.handle(AsyncResultImpl.create(client));
        } catch (ClientNotFoundException e) {
            handler.handle(AsyncResultImpl.create(e, Client.class));
        }
    }

    /**
     * Gets the api synchronously.
     * @param id the api id
     * @throws IOException
     */
    protected Api getApi(String id) throws IOException {
        GetRequest getRequest = new GetRequest(getIndexPrefix() + EsConstants.INDEX_APIS, id);
        GetResponse result = getClient().get(getRequest, RequestOptions.DEFAULT);
        if (result.isExists()) {
            Api api = JSON_MAPPER.readValue(result.getSourceAsString(), Api.class);
            return api;
        } else {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getClient(java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getClient(String apiKey, IAsyncResultHandler<Client> handler) {
        String id = apiKey;
        try {
            Client client = getClient(id);
            handler.handle(AsyncResultImpl.create(client));
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(e, Client.class));
        }
    }

    /**
     * Gets the client synchronously.
     * @param id the client id
     * @throws IOException
     */
    protected Client getClient(String id) throws IOException {
        GetRequest getRequest = new GetRequest(getIndexPrefix() + EsConstants.INDEX_CLIENTS, id);
        GetResponse result = getClient().get(getRequest, RequestOptions.DEFAULT);

        if (result.isExists()) {
            Client client = JSON_MAPPER.readValue(result.getSourceAsString(), Client.class);
            return client;
        } else {
            return null;
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(java.lang.String, java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(String apiOrganizationId, String apiId, String apiVersion, String apiKey,
            IAsyncResultHandler<ApiContract> handler) {

        try {
            Client client = getClient(apiKey);
            Api api = getApi(getApiId(apiOrganizationId, apiId, apiVersion));

            if (client == null) {
                Exception error = new ClientNotFoundException(Messages.i18n.format("EsRegistry.NoClientForAPIKey", apiKey)); //$NON-NLS-1$
                handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                return;
            }
            if (api == null) {
                Exception error = new ApiRetiredException(Messages.i18n.format("EsRegistry.ApiWasRetired", //$NON-NLS-1$
                        apiId, apiOrganizationId));
                handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                return;
            }

            Contract matchedContract = null;
            for (Contract contract : client.getContracts()) {
                if (contract.matches(apiOrganizationId, apiId, apiVersion)) {
                    matchedContract = contract;
                    break;
                }
            }

            if (matchedContract == null) {
                Exception error = new NoContractFoundException(Messages.i18n.format("EsRegistry.NoContractFound", //$NON-NLS-1$
                        client.getClientId(), api.getApiId()));
                handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                return;
            }

            ApiContract contract = new ApiContract(api, client, matchedContract.getPlan(), matchedContract.getPolicies());
            handler.handle(AsyncResultImpl.create(contract));
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(e, ApiContract.class));
        }

    }

    @Override
    @SuppressWarnings("nls")
    public void listClients(String organizationId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // only records matching the organizationId
            MatchQueryBuilder query = QueryBuilders.matchQuery("organizationId", organizationId);
            // only records containing an apiId field:
            TermsAggregationBuilder aggregation = AggregationBuilders.terms("clients").field("clientId");

            // keep searching in specific api mgmt indices to avoid search in foreign indices beside specific api-mgmt ones
            SearchResponse searchResponse = getClient().search(new SearchRequest(getIndexPrefix() + EsConstants.INDEX_CLIENTS)
                    .source(searchSourceBuilder.query(query).aggregation(aggregation)), RequestOptions.DEFAULT);

            List terms = ((ParsedTerms) searchResponse.getAggregations().asMap().get("clients")).getBuckets();
            // Grab only the name of each aggregation (we don't care about count
            List<String> results = (List<String>) terms.stream()
                    .map(o -> ((ParsedTerms.ParsedBucket) o).getKey())
                    .collect(Collectors.toList());

            handler.handle(AsyncResultImpl.create(results));
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("nls")
    @Override
    public void listApis(String organizationId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // only records matching the organizationId
            MatchQueryBuilder query = QueryBuilders.matchQuery("organizationId", organizationId);
            // only records containing an apiId field:
            TermsAggregationBuilder aggregation = AggregationBuilders.terms("apis").field("apiId");

            // keep searching in specific api mgmt indices to avoid search in foreign indices beside specific api-mgmt ones
            SearchRequest searchRequest = new SearchRequest(getIndexPrefix() + EsConstants.INDEX_APIS)
                    .source(searchSourceBuilder.query(query).aggregation(aggregation));
            SearchResponse searchResponse = getClient().search(searchRequest, RequestOptions.DEFAULT);

            List terms = ((ParsedTerms) searchResponse.getAggregations().asMap().get("apis")).getBuckets();
            // Grab only the name of each aggregation (we don't care about count
            List<String> results = (List<String>) terms.stream()
                    .map(o -> ((ParsedTerms.ParsedBucket) o).getKey())
                    .collect(Collectors.toList());

            handler.handle(AsyncResultImpl.create(results));
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("nls")
    public void listOrgs(IAsyncResultHandler<List<String>> handler) {
        try {
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            // i.e. only records containing an organizationId field:
            TermsAggregationBuilder aggregation = AggregationBuilders.terms("all_orgs").field("organizationId");

            // keep searching in specific api mgmt indices to avoid search in foreign indices beside specific api-mgmt ones
            String[] indices = {getIndexPrefix() + EsConstants.INDEX_APIS, getIndexPrefix() + EsConstants.INDEX_CLIENTS};
            SearchRequest searchRequest = new SearchRequest(indices)
                    .source(searchSourceBuilder.aggregation(aggregation));
            SearchResponse searchResponse = getClient().search(searchRequest, RequestOptions.DEFAULT);

            List terms = ((ParsedTerms) searchResponse.getAggregations().asMap().get("all_orgs")).getBuckets();
            // Grab only the name of each aggregation (we don't care about count
            List<String> results = (List<String>) terms.stream()
                    .map(o -> ((ParsedTerms.ParsedBucket) o).getKey())
                    .collect(Collectors.toList());

            handler.handle(AsyncResultImpl.create(results));
        }
        catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("nls")
    public void listClientVersions(String organizationId, String clientId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        String query = "{" +
                "  \"query\": {" +
                "    \"bool\": {" +
                "      \"filter\": [" +
                "        {" +
                "          \"term\": {" +
                "            \"organizationId\": \"{{organizationId}}\"" +  // organizationId
                "          }" +
                "        }," +
                "        {" +
                "          \"term\": {" +
                "            \"clientId\": \"{{clientId}}\"" + // clientId
                "          }" +
                "        }" +
                "      ]" +
                "    }" +
                "  }," +
                "    \"aggs\": {" +
                "      \"client_versions\": {" +
                "        \"terms\": {" +
                "          \"field\": \"version\"" + // only return version fields of clients
                "        }" +
                "      }" +
                "    }" +
                "}";

        SearchTemplateRequest searchTemplateRequest = new SearchTemplateRequest();
        searchTemplateRequest.setRequest(new SearchRequest(getIndexPrefix() + EsConstants.INDEX_CLIENTS));
        searchTemplateRequest.setScriptType(ScriptType.INLINE);
        searchTemplateRequest.setScript(query);

        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("organizationId", organizationId);
        scriptParams.put("clientId", clientId);

        searchTemplateRequest.setScriptParams(scriptParams);

        try {
            SearchTemplateResponse response = getClient().searchTemplate(searchTemplateRequest, RequestOptions.DEFAULT);
            SearchResponse searchResponse = response.getResponse();

            List terms = ((ParsedTerms) searchResponse.getAggregations().asMap().get("client_versions")).getBuckets();
            // Grab only the name of each aggregation (we don't care about count
            List<String> results = (List<String>) terms.stream()
                    .map(o -> ((ParsedTerms.ParsedBucket) o).getKey())
                    .collect(Collectors.toList());

            handler.handle(AsyncResultImpl.create(results));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("nls")
    public void listApiVersions(String organizationId, String apiId, int page, int pageSize,
                                IAsyncResultHandler<List<String>> handler) {
        String query = "{" +
        "  \"query\": {" +
        "    \"bool\": {" +
        "      \"filter\": [" +
        "        {" +
        "          \"term\": {" +
        "            \"organizationId\": \"{{organizationId}}\"" + // organizationId
        "          }" +
        "        }," +
        "        {" +
        "          \"term\": {" +
        "            \"apiId\": \"{{apiId}}\"" + // apiId
        "          }" +
        "        }" +
        "      ]" +
        "    }" +
        "  }," +
        "    \"aggs\": {" +
        "      \"api_versions\": {" +
        "        \"terms\": {" +
        "          \"field\": \"version\"" + // only return version fields of APIs
        "        }" +
        "      }" +
        "    }" +
        "}";

        SearchTemplateRequest searchTemplateRequest = new SearchTemplateRequest();
        searchTemplateRequest.setRequest(new SearchRequest(getIndexPrefix() + EsConstants.INDEX_APIS));
        searchTemplateRequest.setScriptType(ScriptType.INLINE);
        searchTemplateRequest.setScript(query);

        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("organizationId", organizationId);
        scriptParams.put("apiId", apiId);

        searchTemplateRequest.setScriptParams(scriptParams);

        try {
            SearchTemplateResponse response = getClient().searchTemplate(searchTemplateRequest, RequestOptions.DEFAULT);
            SearchResponse searchResponse = response.getResponse();

            List terms = ((ParsedTerms) searchResponse.getAggregations().asMap().get("api_versions")).getBuckets();
            // Grab only the name of each aggregation (we don't care about count
            List<String> results = (List<String>) terms.stream()
                    .map(o -> ((ParsedTerms.ParsedBucket) o).getKey())
                    .collect(Collectors.toList());

            handler.handle(AsyncResultImpl.create(results));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Generates a valid document ID for a api, used to index the api in ES.
     * @param api the api
     * @return an api key
     */
    private String getApiId(Api api) {
        return getApiId(api.getOrganizationId(), api.getApiId(), api.getVersion());
    }

    /**
     * Generates a valid document ID for a api referenced by a contract, used to
     * retrieve the api from ES.
     * @param contract the contract between client and api
     */
    private String getApiId(Contract contract) {
        return getApiId(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
    }

    /**
     * Generates a valid document ID for a api, used to index the api in ES.
     * @param orgId the organization id
     * @param apiId the api id
     * @param version the api version
     * @return a api id
     */
    protected String getApiId(String orgId, String apiId, String version) {
        return EsUtils.escape(orgId + ":" + apiId + ":" + version); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Generates a valid document ID for the client - used to index the client in ES.
     * @param client the client
     * @return a client id
     */
    protected String getClientId(Client client) {
        return client.getApiKey();
    }

    /**
     * @see AbstractEsComponent#getDefaultIndexPrefix()
     */
    @Override
    protected String getDefaultIndexPrefix() {
        return EsConstants.GATEWAY_INDEX_NAME;
    }

    @Override
    public Map<String, EsIndexProperties> getEsIndices() {
        Map<String, EsIndexProperties> indexMap = new HashMap<>();
        indexMap.put(EsConstants.INDEX_APIS, EsRegistryIndexes.GATEWAY_APIS);
        indexMap.put(EsConstants.INDEX_CLIENTS, EsRegistryIndexes.GATEWAY_CLIENTS);
        return indexMap;
    }

}
