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

import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.es.i18n.Messages;
import io.searchbox.client.JestResult;
import io.searchbox.core.Delete;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.params.Parameters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * An implementation of the Registry that uses elasticsearch as a storage
 * mechanism.
 *
 * @author eric.wittmann@redhat.com
 */
public class ESRegistry extends AbstractESComponent implements IRegistry {

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public ESRegistry(Map<String, String> config) {
        super(config);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#publishApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishApi(final Api api, final IAsyncResultHandler<Void> handler) {
        try {
            String id = getApiId(api);
            Index index = new Index.Builder(ESRegistryMarshalling.marshall(api).string()).refresh(false)
                    .index(getIndexName()).setParameter(Parameters.OP_TYPE, "create") //$NON-NLS-1$
                    .type("api").id(id).build(); //$NON-NLS-1$
            JestResult result = getClient().execute(index);
            if (!result.isSucceeded()) {
                handler.handle(AsyncResultImpl.create(
                        new PublishingException(Messages.i18n.format("ESRegistry.ApiAlreadyPublished")),  //$NON-NLS-1$
                        Void.class));
            } else {
                handler.handle(AsyncResultImpl.create((Void) null));
            }
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(
                    new PublishingException(Messages.i18n.format("ESRegistry.ErrorPublishingApi"), e),  //$NON-NLS-1$
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
            Delete delete = new Delete.Builder(id).index(getIndexName()).type("api").build(); //$NON-NLS-1$
            JestResult result = getClient().execute(delete);
            if (result.isSucceeded()) {
                handler.handle(AsyncResultImpl.create((Void) null));
            } else {
                handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ApiNotFound")), Void.class)); //$NON-NLS-1$
            }
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ErrorRetiringApi"), e), Void.class)); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#registerApplication(io.apiman.gateway.engine.beans.Application, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerApplication(final Application application, final IAsyncResultHandler<Void> handler) {
        final Map<String, Api> apiMap = new HashMap<>();

        try {
            // Validate the app and populate the api map with apis found during validation.
            validateApplication(application, apiMap);
            String id = getApplicationId(application);
            Index index = new Index.Builder(ESRegistryMarshalling.marshall(application).string())
                    .refresh(false).index(getIndexName())
                    .setParameter(Parameters.OP_TYPE, "index") //$NON-NLS-1$
                    .type("application").id(id).build(); //$NON-NLS-1$
            JestResult result = getClient().execute(index);
            if (!result.isSucceeded()) {
                throw new IOException(result.getErrorMessage());
            } else {
                // Remove all the api contracts, then re-add them
                unregisterApiContracts(application);

                // Register all the api contracts.
                Set<Contract> contracts = application.getContracts();
                application.setContracts(null);
                for (Contract contract : contracts) {
                    registerContract(application, contract, apiMap);
                }
                handler.handle(AsyncResultImpl.create((Void) null));
            }
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(
                    new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringApplication"), e),  //$NON-NLS-1$
                    Void.class));
        } catch (RegistrationException re) {
            handler.handle(AsyncResultImpl.create(re, Void.class));
        }
    }

    /**
     * Validate that the application should be registered.
     * @param application
     * @param apiMap
     */
    private void validateApplication(Application application, Map<String, Api> apiMap) throws RegistrationException {
        Set<Contract> contracts = application.getContracts();
        if (contracts.isEmpty()) {
            throw new RegistrationException(Messages.i18n.format("ESRegistry.NoContracts")); //$NON-NLS-1$
        }
        for (Contract contract : contracts) {
            validateContract(contract, apiMap);
        }
    }

    /**
     * Ensures that the api referenced by the Contract at the head of
     * the iterator actually exists (is published).
     * @param contract
     * @param apiMap
     */
    private void validateContract(final Contract contract, final Map<String, Api> apiMap)
            throws RegistrationException {

        final String id = getApiId(contract);

        try {
            Get get = new Get.Builder(getIndexName(), id).type("api").build(); //$NON-NLS-1$
            JestResult result = getClient().execute(get);
            if (result.isSucceeded()) {
                Map<String, Object> source = result.getSourceAsObject(Map.class);
                Api api = ESRegistryMarshalling.unmarshallApi(source);
                api.setApiPolicies(null);
                apiMap.put(id, api);
            } else {
                String apiId = contract.getApiId();
                String orgId = contract.getApiOrgId();
                throw new RegistrationException(Messages.i18n.format("ESRegistry.ApiNotFoundInOrg", apiId, orgId));  //$NON-NLS-1$
            }
        } catch (IOException e) {
            throw new RegistrationException(Messages.i18n.format("ESRegistry.ErrorValidatingApp"), e); //$NON-NLS-1$
        }
    }

    /**
     * Register all the contracts in ES so they can be looked up quickly by
     * their ID by all nodes in the cluster.
     * @param application
     * @param contracts
     * @param apiMap
     */
    private void registerContract(final Application application, final Contract contract,
            final Map<String, Api> apiMap) throws RegistrationException {
        try {
            String apiId = getApiId(contract);
            Api api = apiMap.get(apiId);
            ApiContract sc = new ApiContract(contract.getApiKey(), api, application,
                    contract.getPlan(), contract.getPolicies());
            final String contractId = getContractId(contract);

            Index index = new Index.Builder(ESRegistryMarshalling.marshall(sc).string()).refresh(false)
                    .setParameter(Parameters.OP_TYPE, "create") //$NON-NLS-1$
                    .index(getIndexName()).type("apiContract").id(contractId).build(); //$NON-NLS-1$
            JestResult result = getClient().execute(index);
            if (!result.isSucceeded()) {
                throw new RegistrationException(Messages.i18n.format("ESRegistry.ContractAlreadyPublished", contractId)); //$NON-NLS-1$
            }
        } catch (Exception e) {
            throw new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringContract"), e);  //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterApplication(io.apiman.gateway.engine.beans.Application, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterApplication(final Application application, final IAsyncResultHandler<Void> handler) {
        final String id = getApplicationId(application);

        try {
            Delete delete = new Delete.Builder(id).index(getIndexName()).type("application").build(); //$NON-NLS-1$
            JestResult result = getClient().execute(delete);
            if (result.isSucceeded()) {
                unregisterApiContracts(application);
                handler.handle(AsyncResultImpl.create((Void) null));
            } else {
                handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.AppNotFound")), Void.class)); //$NON-NLS-1$
            }
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ErrorUnregisteringApp"), e), Void.class)); //$NON-NLS-1$
        }
    }

    /**
     * Removes all of the api contracts from ES.
     * @param application
     * @throws IOException
     */
    protected void unregisterApiContracts(Application application) throws IOException {
        QueryBuilder qb = QueryBuilders.filteredQuery(
                QueryBuilders.matchAllQuery(),
                FilterBuilders.andFilter(
                        FilterBuilders.termFilter("application.organizationId", application.getOrganizationId()), //$NON-NLS-1$
                        FilterBuilders.termFilter("application.applicationId", application.getApplicationId()), //$NON-NLS-1$
                        FilterBuilders.termFilter("application.version", application.getVersion()) //$NON-NLS-1$
                )
            );
        @SuppressWarnings("nls")
        String dquery = "{\"query\" : " + qb.toString() + "}";
        DeleteByQuery delete = new DeleteByQuery.Builder(dquery).addIndex(getIndexName()).addType("apiContract").build(); //$NON-NLS-1$
        getClient().execute(delete);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(final ApiRequest request, final IAsyncResultHandler<ApiContract> handler) {
        final String id = getContractId(request);

        try {
            Get get = new Get.Builder(getIndexName(), id).type("apiContract").build(); //$NON-NLS-1$
            JestResult result = getClient().execute(get);
            if (!result.isSucceeded()) {
                Exception error = new InvalidContractException(Messages.i18n.format("ESRegistry.NoContractForAPIKey", id)); //$NON-NLS-1$
                handler.handle(AsyncResultImpl.create(error, ApiContract.class));
            } else {
                Map<String, Object> source = result.getSourceAsObject(Map.class);
                ApiContract contract = ESRegistryMarshalling.unmarshallApiContract(source);
                checkApi(contract);
                handler.handle(AsyncResultImpl.create(contract));
            }
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(e, ApiContract.class));
        }
    }

    /**
     * Ensure that the api still exists.  If not, it was retired.
     * @param contract
     * @throws InvalidContractException
     * @throws IOException
     */
    protected void checkApi(final ApiContract contract) throws InvalidContractException, IOException {
        final Api api = contract.getApi();
        String id = getApiId(api);

        Get get = new Get.Builder(getIndexName(), id).type("api").build(); //$NON-NLS-1$
        JestResult result = getClient().execute(get);
        if (!result.isSucceeded()) {
            throw new InvalidContractException(Messages.i18n.format("ESRegistry.ApiWasRetired", //$NON-NLS-1$
                    api.getApiId(), api.getOrganizationId()));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getApi(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getApi(String organizationId, String apiId, String apiVersion,
            IAsyncResultHandler<Api> handler) {
        String id = getApiId(organizationId, apiId, apiVersion);
        getApi(id, handler);
    }

    /**
     * Asynchronously gets a api.
     * @param id
     * @param handler
     */
    protected void getApi(String id, final IAsyncResultHandler<Api> handler) {
        try {
            Api api = getApi(id);
            handler.handle(AsyncResultImpl.create(api));
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(e, Api.class));
        }
    }

    /**
     * Gets the api synchronously.
     * @param id
     * @throws IOException
     */
    protected Api getApi(String id) throws IOException {
        Get get = new Get.Builder(getIndexName(), id).type("api").build(); //$NON-NLS-1$
        JestResult result = getClient().execute(get);
        if (result.isSucceeded()) {
            Map<String, Object> source = result.getSourceAsObject(Map.class);
            Api api = ESRegistryMarshalling.unmarshallApi(source);
            return api;
        } else {
            return null;
        }
    }

    /**
     * Generates a valid document ID for a api, used to index the api in ES.
     * @param api an api
     * @return a api key
     */
    private String getApiId(Api api) {
        return getApiId(api.getOrganizationId(), api.getApiId(), api.getVersion());
    }

    /**
     * Generates a valid document ID for a api referenced by a contract, used to
     * retrieve the api from ES.
     * @param contract
     */
    private String getApiId(Contract contract) {
        return getApiId(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
    }

    /**
     * Generates a valid document ID for a api, used to index the api in ES.
     * @param orgId
     * @param apiId
     * @param version
     * @return a api key
     */
    protected String getApiId(String orgId, String apiId, String version) {
        return orgId + ":" + apiId + ":" + version; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Generates a valid document ID for an application, used to index the app in ES.
     * @param app an application
     * @return an application key
     */
    protected String getApplicationId(Application app) {
        return app.getOrganizationId() + ":" + app.getApplicationId() + ":" + app.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Generates a valid document ID for a contract, used to index the contract in ES.
     * @param request
     */
    private String getContractId(ApiRequest request) {
        return request.getApiKey();
    }

    /**
     * Generates a valid document ID for a contract, used to index the contract in ES.
     * @param contract
     */
    private String getContractId(Contract contract) {
        return contract.getApiKey();
    }

    /**
     * @see io.apiman.gateway.engine.es.AbstractESComponent#getIndexName()
     */
    @Override
    protected String getIndexName() {
        return ESConstants.GATEWAY_INDEX_NAME;
    }

}
