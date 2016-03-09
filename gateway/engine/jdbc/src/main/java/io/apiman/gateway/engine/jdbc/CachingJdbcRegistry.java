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
package io.apiman.gateway.engine.jdbc;

import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.jdbc.i18n.Messages;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extends the {@link JdbcRegistry} to provide single-node caching.  This caching solution
 * will not work in a cluster.  If looking for cluster support, either go with the core
 * {@link JdbcRegistry} or use {@link CachingJdbcRegistry}.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class CachingJdbcRegistry extends JdbcRegistry {

    private Map<String, Api> apiCache = new ConcurrentHashMap<>();
    private Map<String, Client> clientCache = new ConcurrentHashMap<>();
    private Object mutex = new Object();

    /**
     * Constructor.
     */
    public CachingJdbcRegistry(Map<String, String> config) {
        super(config);
    }

    /**
     * Called to invalidate the cache - clearing it so that subsequent calls to getApi()
     * or getContract() will trigger a new fetch from the ES store.
     */
    protected void invalidateCache() {
        synchronized (mutex) {
            clientCache.clear();
            apiCache.clear();
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.jdbc.JdbcRegistry#getContract(java.lang.String, java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(String apiOrganizationId, String apiId, String apiVersion, String apiKey,
            IAsyncResultHandler<ApiContract> handler) {
        Client client = null;
        Api api = null;
        
        try {
            synchronized (mutex) {
                client = getClient(apiKey);
                api = getApi(apiOrganizationId, apiId, apiVersion);
            }
            if (client == null) {
                Exception error = new InvalidContractException(Messages.i18n.format("JdbcRegistry.NoClientForAPIKey", apiKey)); //$NON-NLS-1$
                handler.handle(AsyncResultImpl.create(error, ApiContract.class));
                return;
            }
            if (api == null) {
                throw new InvalidContractException(Messages.i18n.format("JdbcRegistry.ApiWasRetired", //$NON-NLS-1$
                        apiId, apiOrganizationId));
            }
            
            Contract matchedContract = null;
            for (Contract contract : client.getContracts()) {
                if (contract.matches(apiOrganizationId, apiId, apiVersion)) {
                    matchedContract = contract;
                    break;
                }
            }
            
            if (matchedContract == null) {
                throw new InvalidContractException(Messages.i18n.format("JdbcRegistry.NoContractFound", //$NON-NLS-1$
                        client.getClientId(), api.getApiId()));
            }
            
            ApiContract contract = new ApiContract(api, client, matchedContract.getPlan(), matchedContract.getPolicies());
            handler.handle(AsyncResultImpl.create(contract));
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.create(e, ApiContract.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.jdbc.JdbcRegistry#getApi(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getApi(final String organizationId, final String apiId, final String apiVersion,
            final IAsyncResultHandler<Api> handler) {
        try {
            Api api = getApi(organizationId, apiId, apiVersion);
            handler.handle(AsyncResultImpl.create(api));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e, Api.class));
        }
    }

    /**
     * Gets the api either from the cache or from ES.
     * @param orgId
     * @param apiId
     * @param version
     */
    protected Api getApi(String orgId, String apiId, String version) throws SQLException {
        String apiIdx = getApiId(orgId, apiId, version);
        Api api;
        synchronized (mutex) {
            api = apiCache.get(apiIdx);
        }

        if (api == null) {
            api = super.getApiInternal(orgId, apiId, version);
            synchronized (mutex) {
                if (api != null) {
                    apiCache.put(apiIdx, api);
                }
            }
        }

        return api;
    }
    
    /**
     * @see io.apiman.gateway.engine.jdbc.JdbcRegistry#getClient(java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getClient(String apiKey, IAsyncResultHandler<Client> handler) {
        try {
            Client client = getClient(apiKey);
            handler.handle(AsyncResultImpl.create(client));
        } catch (SQLException e) {
            handler.handle(AsyncResultImpl.create(e, Client.class));
        }
    }

    /**
     * Gets the client either from the cache or from ES.
     * @param orgId
     * @param clientId
     * @param version
     */
    protected Client getClient(String apiKey) throws SQLException {
        Client client;
        synchronized (mutex) {
            client = clientCache.get(apiKey);
        }

        if (client == null) {
            client = super.getClientInternal(apiKey);
            synchronized (mutex) {
                if (client != null) {
                    clientCache.put(apiKey, client);
                }
            }
        }

        return client;
    }
    
}
