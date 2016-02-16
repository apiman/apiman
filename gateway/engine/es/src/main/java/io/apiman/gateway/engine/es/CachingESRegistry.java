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

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.es.i18n.Messages;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Get;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extends the {@link ESRegistry} to provide single-node caching.  This caching solution
 * will not work in a cluster.  If looking for cluster support, either go with the core
 * ESRegistry or find/implement a caching registry that works in a cluster (e.g. leverage
 * jgroups?).
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class CachingESRegistry extends ESRegistry {

    private Map<String, ApiContract> contractCache = new ConcurrentHashMap<>();
    private Map<String, Api> apiCache = new HashMap<>();
    private Map<String, Client> clientCache = new HashMap<>();
    private Object mutex = new Object();

    /**
     * Constructor.
     */
    public CachingESRegistry(Map<String, String> config) {
        super(config);
    }

    /**
     * Called to invalidate the cache - clearing it so that subsequent calls to getApi()
     * or getContract() will trigger a new fetch from the ES store.
     */
    protected void invalidateCache() {
        synchronized (mutex) {
            contractCache.clear();
            apiCache.clear();
            clientCache.clear();
        }
    }

    /**
     * @see io.apiman.gateway.engine.es.ESRegistry#getContract(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(final ApiRequest request, final IAsyncResultHandler<ApiContract> handler) {
        ApiContract contract;

        String contractKey = getContractKey(request);
        synchronized (mutex) {
            contract = contractCache.get(contractKey);
        }

        try {
            if (contract == null) {
                super.getContract(request, new IAsyncResultHandler<ApiContract>() {
                    @Override
                    public void handle(IAsyncResult<ApiContract> result) {
                        if (result.isSuccess()) {
                            loadAndCacheClient(result.getResult().getClient());
                        }
                        handler.handle(result);
                    }
                });
            } else {
                Api api = getApi(request.getApiOrgId(), request.getApiId(), request.getApiVersion());
                if (api == null) {
                    throw new InvalidContractException(Messages.i18n.format("ESRegistry.ApiWasRetired", //$NON-NLS-1$
                            request.getApiId(), request.getApiOrgId()));
                }
                contract.setApi(api);
                handler.handle(AsyncResultImpl.create(contract));
            }
        } catch (Throwable e) {
            handler.handle(AsyncResultImpl.create(e, ApiContract.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.es.ESRegistry#getApi(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getApi(final String organizationId, final String apiId, final String apiVersion,
            final IAsyncResultHandler<Api> handler) {
        try {
            Api api = getApi(organizationId, apiId, apiVersion);
            handler.handle(AsyncResultImpl.create(api));
        } catch (IOException e) {
            handler.handle(AsyncResultImpl.create(e, Api.class));
        }
    }

    /**
     * Gets the api either from the cache or from ES.
     * @param orgId
     * @param apiId
     * @param version
     */
    protected Api getApi(String orgId, String apiId, String version) throws IOException {
        String apiKey = getApiKey(orgId, apiId, version);
        Api api;
        synchronized (mutex) {
            api = apiCache.get(apiKey);
        }

        if (api == null) {
            api = super.getApi(getApiId(orgId, apiId, version));
            synchronized (mutex) {
                if (api != null) {
                    apiCache.put(apiKey, api);
                }
            }
        }

        return api;
    }

    /**
     * @see io.apiman.gateway.engine.es.ESRegistry#checkApi(io.apiman.gateway.engine.beans.ApiContract)
     */
    @Override
    protected void checkApi(ApiContract contract) throws InvalidContractException, IOException {
        Api api = getApi(contract.getApi().getOrganizationId(),
                contract.getApi().getApiId(),
                contract.getApi().getVersion());
        if (api == null) {
            throw new InvalidContractException(Messages.i18n.format("ESRegistry.ApiWasRetired", //$NON-NLS-1$
                    contract.getApi().getApiId(), contract.getApi().getOrganizationId()));
        }
    }

    /**
     * @param client
     */
    protected void cacheClient(Client client) {
        String clientKey = getClientKey(client);
        synchronized (mutex) {
            clientCache.put(clientKey, client);
            if (client.getContracts() != null) {
                for (Contract contract : client.getContracts()) {
                    ApiContract sc = new ApiContract(contract.getApiKey(), null, client, contract.getPlan(), contract.getPolicies());
                    String contractKey = getContractKey(contract);
                    contractCache.put(contractKey, sc);
                }
            }
        }
    }

    /**
     * @param client
     */
    protected void loadAndCacheClient(Client client) {
        String id = getClientId(client);
        Get get = new Get.Builder(getIndexName(), id).type("client").build(); //$NON-NLS-1$
        getClient().executeAsync(get, new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                if (result.isSucceeded()) {
                    Client app = result.getSourceAsObject(Client.class);
                    cacheClient(app);
                }
            }
            @Override
            public void failed(Exception e) {
            }
        });
    }

    /**
     * Generates an in-memory key for an API, used to index the app for later quick
     * retrieval.
     * @param orgId
     * @param apiId
     * @param version
     * @return a API key
     */
    private String getApiKey(String orgId, String apiId, String version) {
        return "API::" + orgId + "|" + apiId + "|" + version; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Generates an in-memory key for an client, used to index the app for later quick
     * retrieval.
     * @param client an client
     * @return an client key
     */
    private String getClientKey(Client client) {
        return "CLIENT::" + client.getOrganizationId() + "|" + client.getClientId() + "|" + client.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Generates an in-memory key for a contract.
     * @param request
     */
    private String getContractKey(ApiRequest request) {
        return "CONTRACT::" + request.getApiKey(); //$NON-NLS-1$
    }

    /**
     * Generates an in-memory key for a API contract, used to index the app for later quick
     * retrieval.
     * @param contract
     */
    private String getContractKey(Contract contract) {
        return "CONTRACT::" + contract.getApiKey(); //$NON-NLS-1$
    }

}
