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
import io.apiman.gateway.engine.i18n.Messages;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An in-memory implementation of the registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class InMemoryRegistry implements IRegistry {

    private Map<String, Object> map = new ConcurrentHashMap<>();
    private Object mutex = new Object();

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
            String apiKey = getApiKey(api);
            getMap().put(apiKey, api);
        }
        handler.handle(AsyncResultImpl.create((Void) null));
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#retireApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        Exception error = null;
        synchronized (mutex) {
            String apiKey = getApiKey(api);
            if (getMap().containsKey(apiKey)) {
                getMap().remove(apiKey);
            } else {
                error = new PublishingException(Messages.i18n.format("InMemoryRegistry.ApiNotFound")); //$NON-NLS-1$
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
                String apiKey = getApiKey(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
                if (!getMap().containsKey(apiKey)) {
                    error = new RegistrationException(Messages.i18n.format("InMemoryRegistry.ApiNotFoundInOrg", //$NON-NLS-1$
                            contract.getApiId(), contract.getApiOrgId()));
                    break;
                }
            }

            // Unregister the app (if it exists)
            IAsyncResultHandler<Void> unregisterHandler = new IAsyncResultHandler<Void>() {
                @Override
                public void handle(IAsyncResult<Void> result) {
                }
            };
            unregisterClient(client, unregisterHandler);

            // Now, register the app.
            String clientKey = getClientKey(client);
            getMap().put(clientKey, client);
            for (Contract contract : client.getContracts()) {
                String apiKey = getApiKey(contract.getApiOrgId(), contract.getApiId(), contract.getApiVersion());
                Api api = (Api) getMap().get(apiKey);
                ApiContract sc = new ApiContract(contract.getApiKey(), api, client, contract.getPlan(), contract.getPolicies());
                String contractKey = getContractKey(contract);
                getMap().put(contractKey, sc);
            }
        }
        if (error == null) {
            handler.handle(AsyncResultImpl.create((Void) null));
        } else {
            handler.handle(AsyncResultImpl.create(error, Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        Exception error = null;
        synchronized (mutex) {
            String clientKey = getClientKey(client);
            if (getMap().containsKey(clientKey)) {
                Client removed = (Client) getMap().remove(clientKey);
                for (Contract contract : removed.getContracts()) {
                    String contractKey = getContractKey(contract);
                    if (getMap().containsKey(contractKey)) {
                        getMap().remove(contractKey);
                    }
                }
            } else {
                error = new RegistrationException(Messages.i18n.format("InMemoryRegistry.ClientNotFound")); //$NON-NLS-1$
            }
        }
        if (error == null) {
            handler.handle(AsyncResultImpl.create((Void) null));
        } else {
            handler.handle(AsyncResultImpl.create(error, Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(io.apiman.gateway.engine.beans.ApiRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(ApiRequest request, IAsyncResultHandler<ApiContract> handler) {
        String contractKey = getContractKey(request);
        ApiContract contract = (ApiContract) getMap().get(contractKey);

        if (contract == null) {
            Exception error = new InvalidContractException(Messages.i18n.format("InMemoryRegistry.NoContractForAPIKey", request.getApiKey())); //$NON-NLS-1$
            handler.handle(AsyncResultImpl.create(error, ApiContract.class));
            return;
        }
        // Has the api been retired?
        Api api = contract.getApi();
        String apiKey = getApiKey(api);
        if (getMap().get(apiKey) == null) {
            Exception error = new InvalidContractException(Messages.i18n.format("InMemoryRegistry.ApiWasRetired", //$NON-NLS-1$
                    api.getApiId(), api.getOrganizationId()));
            handler.handle(AsyncResultImpl.create(error, ApiContract.class));
            return;
        }

        handler.handle(AsyncResultImpl.create(contract));
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getApi(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getApi(String organizationId, String apiId, String apiVersion,
            IAsyncResultHandler<Api> handler) {
        String key = getApiKey(organizationId, apiId, apiVersion);
        Api api = (Api) getMap().get(key);
        handler.handle(AsyncResultImpl.create(api));
    }

    /**
     * Generates an in-memory key for an api, used to index the app for later quick
     * retrieval.
     * @param api an api
     * @return a api key
     */
    private String getApiKey(Api api) {
        return getApiKey(api.getOrganizationId(), api.getApiId(), api.getVersion());
    }

    /**
     * Generates an in-memory key for an api, used to index the app for later quick
     * retrieval.
     * @param orgId
     * @param apiId
     * @param version
     * @return a api key
     */
    private String getApiKey(String orgId, String apiId, String version) {
        return "API::" + orgId + "|" + apiId + "|" + version; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Generates an in-memory key for an client, used to index the app for later quick
     * retrieval.
     * @param app an client
     * @return an client key
     */
    private String getClientKey(Client app) {
        return "CLIENT::" + app.getOrganizationId() + "|" + app.getClientId() + "|" + app.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Generates an in-memory key for a contract.
     * @param request
     */
    private String getContractKey(ApiRequest request) {
        return "CONTRACT::" + request.getApiKey(); //$NON-NLS-1$
    }

    /**
     * Generates an in-memory key for a api contract, used to index the app for later quick
     * retrieval.
     * @param contract
     */
    private String getContractKey(Contract contract) {
        return "CONTRACT::" + contract.getApiKey(); //$NON-NLS-1$
    }

    /**
     * @return the map to use when storing stuff
     */
    protected Map<String, Object> getMap() {
        return map;
    }

}
