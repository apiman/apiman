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
package io.apiman.gateway.engine.impl;

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
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.i18n.Messages;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * An in-memory implementation of the registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class InMemoryRegistry implements IRegistry {

    private final Map<String, Object> map = new ConcurrentHashMap<>();
    private final Object mutex = new Object();

    /**
     * Constructor.
     */
    public InMemoryRegistry() {
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#publishApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        synchronized (mutex) {
            String apiIdx = getApiIndex(api);
            getMap().put(apiIdx, api);
        }
        handler.handle(AsyncResultImpl.create((Void) null));
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#retireApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        String apiIdx = getApiIndex(api);
        Exception error = null;
        synchronized (mutex) {
            Api removedApi = (Api) getMap().remove(apiIdx);
            if (removedApi == null) {
                error = new ApiNotFoundException(Messages.i18n.format("InMemoryRegistry.ApiNotFound")); //$NON-NLS-1$
            }
        }
        if (error == null) {
            handler.handle(AsyncResultImpl.create((Void) null));
        } else {
            handler.handle(AsyncResultImpl.create(error, Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#registerClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> handler) {
        Exception error = null;
        synchronized (mutex) {
            // Validate the client first - we need to be able to resolve all the contracts.
            for (Contract contract : client.getContracts()) {
                String apiIdx = getApiIndex(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
                if (!getMap().containsKey(apiIdx)) {
                    error = new ApiNotFoundException(Messages.i18n.format("InMemoryRegistry.ApiNotFoundInOrg", //$NON-NLS-1$
                            contract.getApiId(), contract.getApiOrgId()));
                    break;
                }
            }

            if (error == null) {
                // Unregister the client (if it exists)
                unregisterClientInternal(client, true);

                // Now, register the client.
                String clientIdx = getClientIndex(client);
                getMap().put(clientIdx, client);
                getMap().put(client.getApiKey(), client);

                handler.handle(AsyncResultImpl.create((Void) null));
            } else {
                handler.handle(AsyncResultImpl.create(error, Void.class));
            }
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        try {
            unregisterClientInternal(client, false);
            handler.handle(AsyncResultImpl.create((Void) null));
        } catch (RegistrationException e) {
            handler.handle(AsyncResultImpl.create(e, Void.class));
        }
    }

    /**
     * @param client
     * @param silent
     */
    protected void unregisterClientInternal(Client client, boolean silent) throws RegistrationException {
        synchronized (mutex) {
            String clientIdx = getClientIndex(client);
            Client oldClient = (Client) getMap().remove(clientIdx);
            if (oldClient == null) {
                if (!silent) {
                    throw new ClientNotFoundException(Messages.i18n.format("InMemoryRegistry.ClientNotFound")); //$NON-NLS-1$
                }
            } else {
                getMap().remove(oldClient.getApiKey());
            }
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getClient(java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getClient(String apiKey, IAsyncResultHandler<Client> handler) {
        Client client = getClientInternal(apiKey);
        handler.handle(AsyncResultImpl.create(client));
    }

    @Override
    public void getClient(String organizationId, String clientId, String clientVersion, IAsyncResultHandler<Client> handler) {
        String clientIdx = getClientIndex(organizationId, clientId, clientVersion);
        Client client = getClientInternal(clientIdx);
        handler.handle(AsyncResultImpl.create(client));
    }

    /**
     * Gets the client and returns it.
     * @param idx the index
     */
    protected Client getClientInternal(String idx) {
        Client client;
        synchronized (mutex) {
            client = (Client) getMap().get(idx);
        }
        return client;
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(java.lang.String, java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(String apiOrganizationId, String apiId, String apiVersion, String apiKey,
            IAsyncResultHandler<ApiContract> handler) {
        Client client = null;
        Api api = null;

        String apiIdx = getApiIndex(apiOrganizationId, apiId, apiVersion);
        synchronized (mutex) {
            client = (Client) getMap().get(apiKey);
            api = (Api) getMap().get(apiIdx);
        }
        if (client == null) {
            Exception error = new ClientNotFoundException(Messages.i18n.format("InMemoryRegistry.NoClientForAPIKey", apiKey)); //$NON-NLS-1$
            handler.handle(AsyncResultImpl.create(error, ApiContract.class));
            return;
        }
        if (api == null) {
            Exception error = new ApiRetiredException(Messages.i18n.format("InMemoryRegistry.ApiWasRetired", //$NON-NLS-1$
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
            Exception error = new NoContractFoundException(Messages.i18n.format("InMemoryRegistry.NoContractFound", //$NON-NLS-1$
                    client.getClientId(), api.getApiId()));
            handler.handle(AsyncResultImpl.create(error, ApiContract.class));
            return;
        }

        ApiContract contract = new ApiContract(api, client, matchedContract.getPlan(), matchedContract.getPolicies());
        handler.handle(AsyncResultImpl.create(contract));
    }

    @Override
    public void listClients(String organizationId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        // For now, ignore paging, but it's there for future. Would need to ensure stable ordering.
        List<String> res = map.values().stream()
                .filter(entity -> entity instanceof Client)
                .map(entity -> (Client) entity)
                .filter(client -> client.getOrganizationId().equals(organizationId))
                .map(Client::getClientId)
                .distinct()
                .collect(Collectors.toList());
        handler.handle(AsyncResultImpl.create(res));
    }

    @Override
    public void listClientVersions(String organizationId, String clientId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        // For now, ignore paging, but it's there for future. Would need to ensure stable ordering.
        List<String> res = map.values().stream()
                .filter(entity -> entity instanceof Client)
                .map(entity -> (Client) entity)
                .filter(client -> client.getOrganizationId().equals(organizationId) && client.getClientId().equals(clientId))
                .map(Client::getVersion)
                .distinct()
                .collect(Collectors.toList());
        handler.handle(AsyncResultImpl.create(res));
    }

    @Override
    public void listApis(String organizationId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        // For now, ignore paging, but it's there for future. Would need to ensure stable ordering.
        List<String> res = map.values().stream()
                .filter(entity -> entity instanceof Api)
                .map(entity -> (Api) entity)
                .filter(api -> api.getOrganizationId().equals(organizationId))
                .map(Api::getApiId)
                .distinct()
                .collect(Collectors.toList());
        handler.handle(AsyncResultImpl.create(res));
    }

    @Override
    public void listApiVersions(String organizationId, String apiId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        // For now, ignore paging, but it's there for future. Would need to ensure stable ordering.
        List<String> res = map.values().stream()
                .filter(entity -> entity instanceof Api)
                .map(entity -> (Api) entity)
                .filter(api -> api.getOrganizationId().equals(organizationId) && api.getApiId().equals(apiId))
                .map(Api::getVersion)
                .distinct()
                .collect(Collectors.toList());
        handler.handle(AsyncResultImpl.create(res));
    }

    @Override
    public void listOrgs(IAsyncResultHandler<List<String>> handler) {
        // TODO: We should track set of OrgId -> AtomicCounter if this API has meaningfully high usage.
        List<String> res = map.values().stream()
                .map(elem -> {
                    if (elem instanceof Api) {
                        return ((Api) elem).getOrganizationId();
                    } else {
                        return ((Client) elem).getOrganizationId();
                    }
                })
                .distinct()
                .collect(Collectors.toList());
        handler.handle(AsyncResultImpl.create(res));
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getApi(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getApi(String organizationId, String apiId, String apiVersion,
            IAsyncResultHandler<Api> handler) {
        Api api = getApiInternal(organizationId, apiId, apiVersion);
        handler.handle(AsyncResultImpl.create(api));
    }

    /**
     * Gets an API by its unique identifying info (orgid, id, version).
     *
     * @return an Api or null if not found
     */
    private Api getApiInternal(String apiOrgId, String apiId, String apiVersion) {
        String key = getApiIndex(apiOrgId, apiId, apiVersion);
        Api api;
        synchronized (mutex) {
            api = (Api) getMap().get(key);
        }
        return api;
    }

    /**
     * Generates an in-memory key for an api, used to index the client for later quick
     * retrieval.
     * @param api an api
     * @return a api key
     */
    protected String getApiIndex(Api api) {
        return getApiIndex(api.getOrganizationId(), api.getApiId(), api.getVersion());
    }

    /**
     * Generates an in-memory key for an api, used to index the client for later quick
     * retrieval.
     *
     * @return an api key index
     */
    private String getApiIndex(String orgId, String apiId, String version) {
        return "API::" + orgId + "|" + apiId + "|" + version; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Generates an in-memory key for an client, used to index the client for later quick
     * retrieval.
     * @param client an client
     * @return a client key
     */
    protected String getClientIndex(Client client) {
        return getClientIndex(client.getOrganizationId(), client.getClientId(), client.getVersion());
    }

    private String getClientIndex(String orgId, String clientId, String version) {
        return "CLIENT::" + orgId + "|" + clientId + "|" + version; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * @return the map to use when storing stuff
     */
    public Map<String, Object> getMap() {
        return map;
    }

}
