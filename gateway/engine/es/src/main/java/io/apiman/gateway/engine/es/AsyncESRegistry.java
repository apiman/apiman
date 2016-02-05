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
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.es.i18n.Messages;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Delete;
import io.searchbox.core.DeleteByQuery;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.params.Parameters;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
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
public class AsyncESRegistry extends AbstractESComponent implements IRegistry {

    /**
     * Constructor.
     * @param config map of configuration options
     */
    public AsyncESRegistry(Map<String, String> config) {
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
            getClient().executeAsync(index, new JestResultHandler<JestResult>() {
                @Override
                public void completed(JestResult result) {
                    if (!result.isSucceeded()) {
                        handler.handle(AsyncResultImpl.create(
                                new PublishingException(Messages.i18n.format("ESRegistry.ApiAlreadyPublished")),  //$NON-NLS-1$
                                Void.class));
                    } else {
                        handler.handle(AsyncResultImpl.create((Void) null));
                    }
                }
                @Override
                public void failed(Exception e) {
                    handler.handle(AsyncResultImpl.create(
                            new PublishingException(Messages.i18n.format("ESRegistry.ErrorPublishingApi"), e),  //$NON-NLS-1$
                            Void.class));
                }
            });
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

        Delete delete = new Delete.Builder(id).index(getIndexName()).type("api").build(); //$NON-NLS-1$
        getClient().executeAsync(delete, new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                if (result.isSucceeded()) {
                    handler.handle(AsyncResultImpl.create((Void) null));
                } else {
                    handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ApiNotFound")), Void.class)); //$NON-NLS-1$
                }
            }
            @Override
            public void failed(Exception e) {
                handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ErrorRetiringApi"), e), Void.class)); //$NON-NLS-1$
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#registerClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerClient(final Client client, final IAsyncResultHandler<Void> handler) {
        final Map<String, Api> apiMap = new HashMap<>();
        validateClient(client, apiMap, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isError()) {
                    handler.handle(result);
                } else {
                    String id = getClientId(client);
                    try {
                        Index index = new Index.Builder(ESRegistryMarshalling.marshall(client).string())
                                .refresh(false).index(getIndexName())
                                .setParameter(Parameters.OP_TYPE, "index") //$NON-NLS-1$
                                .type("client").id(id).build(); //$NON-NLS-1$
                        getClient().executeAsync(index, new JestResultHandler<JestResult>() {
                            @Override
                            public void completed(JestResult result) {
                                if (!result.isSucceeded()) {
                                    handler.handle(AsyncResultImpl.create(
                                            new IOException(result.getErrorMessage()),
                                            Void.class));
                                } else {
                                    unregisterApiContracts(client, new IAsyncResultHandler<Void>() {
                                        @Override
                                        public void handle(IAsyncResult<Void> result) {
                                            if (result.isError()) {
                                                handler.handle(result);
                                            } else {
                                                Iterator<Contract> iterator = client.getContracts().iterator();
                                                client.setContracts(null);
                                                registerContracts(client, iterator, apiMap, handler);
                                            }
                                        }
                                    });
                                }
                            }
                            @Override
                            public void failed(Exception e) {
                                handler.handle(AsyncResultImpl.create(
                                        new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringClient"), e),  //$NON-NLS-1$
                                        Void.class));
                            }
                        });
                    } catch (Exception e) {
                        handler.handle(AsyncResultImpl.create(
                                new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringClient"), e),  //$NON-NLS-1$
                                Void.class));
                    }
                }
            }
        });
    }

    /**
     * Validate that the client should be registered.
     * @param client
     * @param apiMap
     * @param iAsyncResultHandler
     */
    private void validateClient(Client client, Map<String, Api> apiMap, IAsyncResultHandler<Void> handler) {
        Set<Contract> contracts = client.getContracts();
        if (contracts.isEmpty()) {
            handler.handle(AsyncResultImpl.create(
                    new RegistrationException(Messages.i18n.format("ESRegistry.NoContracts")), Void.class)); //$NON-NLS-1$
            return;
        }
        final Iterator<Contract> iterator = contracts.iterator();
        validateApiExists(iterator, apiMap, handler);
    }

    /**
     * Ensures that the api referenced by the Contract at the head of
     * the iterator actually exists (is published).
     * @param iterator
     * @param apiMap
     * @param handler
     */
    private void validateApiExists(final Iterator<Contract> iterator, final Map<String, Api> apiMap,
            final IAsyncResultHandler<Void> handler) {
        if (!iterator.hasNext()) {
            handler.handle(AsyncResultImpl.create((Void) null));
        } else {
            final Contract contract = iterator.next();
            final String apiId = getApiId(contract);
            getApi(apiId, new IAsyncResultHandler<Api>() {
                @Override
                public void handle(IAsyncResult<Api> result) {
                    if (result.isError()) {
                        handler.handle(AsyncResultImpl.create(
                                new RegistrationException(
                                        Messages.i18n.format("ESRegistry.ErrorValidatingClient"),  //$NON-NLS-1$
                                        result.getError()),
                                Void.class));
                    } else {
                        Api api = result.getResult();
                        if (api == null) {
                            String apiId = contract.getApiId();
                            String orgId = contract.getApiOrgId();
                            handler.handle(AsyncResultImpl.create(
                                    new RegistrationException(Messages.i18n.format("ESRegistry.ApiNotFoundInOrg", apiId, orgId)),  //$NON-NLS-1$
                                    Void.class));
                        } else {
                            api.setApiPolicies(null);
                            apiMap.put(apiId, api);
                            validateApiExists(iterator, apiMap, handler);
                        }
                    }
                }
            });
        }
    }

    /**
     * Register all the contracts in ES so they can be looked up quickly by
     * their ID by all nodes in the cluster.
     * @param client
     * @param contracts
     * @param apiMap
     * @param handler
     */
    private void registerContracts(final Client client, final Iterator<Contract> contracts,
            final Map<String, Api> apiMap, final IAsyncResultHandler<Void> handler) {
        try {
            if (!contracts.hasNext()) {
                handler.handle(AsyncResultImpl.create((Void) null));
            } else {
                Contract contract = contracts.next();

                String apiId = getApiId(contract);
                Api api = apiMap.get(apiId);
                ApiContract sc = new ApiContract(contract.getApiKey(), api, client,
                        contract.getPlan(), contract.getPolicies());
                final String contractId = getContractId(contract);

                Index index = new Index.Builder(ESRegistryMarshalling.marshall(sc).string()).refresh(false)
                        .setParameter(Parameters.OP_TYPE, "create") //$NON-NLS-1$
                        .index(getIndexName()).type("apiContract").id(contractId).build(); //$NON-NLS-1$
                getClient().executeAsync(index, new JestResultHandler<JestResult>() {
                    @Override
                    public void completed(JestResult result) {
                        if (!result.isSucceeded()) {
                            handler.handle(AsyncResultImpl.create(
                                    new RegistrationException(Messages.i18n.format("ESRegistry.ContractAlreadyPublished", contractId)),  //$NON-NLS-1$
                                    Void.class));
                        } else {
                            registerContracts(client, contracts, apiMap, handler);
                        }
                    }
                    @Override
                    public void failed(Exception e) {
                        handler.handle(AsyncResultImpl.create(
                                new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringContract"), e),  //$NON-NLS-1$
                                Void.class));
                    }
                });
            }
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(
                    new RegistrationException(Messages.i18n.format("ESRegistry.ErrorRegisteringContract"), e),  //$NON-NLS-1$
                    Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterClient(final Client client, final IAsyncResultHandler<Void> handler) {
        final String id = getClientId(client);

        Delete delete = new Delete.Builder(id).index(getIndexName()).type("client").build(); //$NON-NLS-1$
        getClient().executeAsync(delete, new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                if (result.isSucceeded()) {
                    unregisterApiContracts(client, handler);
                } else {
                    handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ClientNotFound")), Void.class)); //$NON-NLS-1$
                }
            }
            @Override
            public void failed(Exception e) {
                handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ErrorUnregisteringClient"), e), Void.class)); //$NON-NLS-1$
            }
        });
    }

    /**
     * Removes all of the api contracts from ES.
     * @param client
     * @param handler
     */
    protected void unregisterApiContracts(Client client, final IAsyncResultHandler<Void> handler) {
        QueryBuilder qb = QueryBuilders.filteredQuery(
                QueryBuilders.matchAllQuery(),
                FilterBuilders.andFilter(
                        FilterBuilders.termFilter("client.organizationId", client.getOrganizationId()), //$NON-NLS-1$
                        FilterBuilders.termFilter("client.clientId", client.getClientId()), //$NON-NLS-1$
                        FilterBuilders.termFilter("client.version", client.getVersion()) //$NON-NLS-1$
                )
            );
        @SuppressWarnings("nls")
        String dquery = "{\"query\" : " + qb.toString() + "}";
        DeleteByQuery delete = new DeleteByQuery.Builder(dquery).addIndex(getIndexName()).addType("apiContract").build(); //$NON-NLS-1$
        getClient().executeAsync(delete, new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                handler.handle(AsyncResultImpl.create((Void) null));
            }
            @Override
            public void failed(Exception e) {
                handler.handle(AsyncResultImpl.create(new PublishingException(Messages.i18n.format("ESRegistry.ErrorUnregisteringClient"), e), Void.class)); //$NON-NLS-1$
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(final ApiRequest request, final IAsyncResultHandler<ApiContract> handler) {
        final String id = getContractId(request);

        Get get = new Get.Builder(getIndexName(), id).type("apiContract").build(); //$NON-NLS-1$
        getClient().executeAsync(get, new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                if (!result.isSucceeded()) {
                    Exception error = new InvalidContractException(Messages.i18n.format("ESRegistry.NoContractForAPIKey", id)); //$NON-NLS-1$
                    handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                } else {
                    Map<String, Object> source = result.getSourceAsObject(Map.class);
                    ApiContract contract = ESRegistryMarshalling.unmarshallApiContract(source);
                    checkApi(contract, handler);
                }
            }
            @Override
            public void failed(Exception e) {
                handler.handle(AsyncResultImpl.create(e, ApiContract.class));
            }
        });
    }

    /**
     * Ensure that the api still exists.  If not, it was retired.
     * @param contract
     * @param handler
     */
    protected void checkApi(final ApiContract contract, final IAsyncResultHandler<ApiContract> handler) {
        final Api api = contract.getApi();
        String id = getApiId(api);

        Get get = new Get.Builder(getIndexName(), id).type("api").build(); //$NON-NLS-1$
        getClient().executeAsync(get, new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                if (result.isSucceeded()) {
                    handler.handle(AsyncResultImpl.create(contract));
                } else {
                    Exception error = new InvalidContractException(Messages.i18n.format("ESRegistry.ApiWasRetired", //$NON-NLS-1$
                            api.getApiId(), api.getOrganizationId()));
                    handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                }
            }
            @Override
            public void failed(Exception e) {
                handler.handle(AsyncResultImpl.create(e, ApiContract.class));
            }
        });
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
        Get get = new Get.Builder(getIndexName(), id).type("api").build(); //$NON-NLS-1$
        getClient().executeAsync(get, new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                if (result.isSucceeded()) {
                    Map<String, Object> source = result.getSourceAsObject(Map.class);
                    Api api = ESRegistryMarshalling.unmarshallApi(source);
                    handler.handle(AsyncResultImpl.create(api));
                } else {
                    handler.handle(AsyncResultImpl.create((Api) null));
                }
            }
            @Override
            public void failed(Exception e) {
                handler.handle(AsyncResultImpl.create(e, Api.class));
            }
        });
    }

    /**
     * Generates a valid document ID for a API, used to index the API in ES.
     * @param api an API
     * @return a API key
     */
    private String getApiId(Api api) {
        return getApiId(api.getOrganizationId(), api.getApiId(), api.getVersion());
    }

    /**
     * Generates a valid document ID for a API referenced by a contract, used to
     * retrieve the API from ES.
     * @param contract
     */
    private String getApiId(Contract contract) {
        return getApiId(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
    }

    /**
     * Generates a valid document ID for a API, used to index the API in ES.
     * @param orgId
     * @param apiId
     * @param version
     * @return a API key
     */
    private String getApiId(String orgId, String apiId, String version) {
        return orgId + ":" + apiId + ":" + version; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Generates a valid document ID for an client, used to index the app in ES.
     * @param client an client
     * @return an client key
     */
    private String getClientId(Client client) {
        return client.getOrganizationId() + ":" + client.getClientId() + ":" + client.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$
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
