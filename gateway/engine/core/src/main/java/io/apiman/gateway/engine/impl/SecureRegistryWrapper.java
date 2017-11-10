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
package io.apiman.gateway.engine.impl;

import io.apiman.common.util.crypt.DataEncryptionContext;
import io.apiman.common.util.crypt.DataEncryptionContext.EntityType;
import io.apiman.common.util.crypt.IDataEncrypter;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Wraps any {@link IRegistry} implementation to provide some encryption to
 * sensitive information.
 *
 * @author eric.wittmann@redhat.com
 */
public class SecureRegistryWrapper implements IRegistry {

    private final IRegistry delegate;
    private final IDataEncrypter encrypter;

    /**
     * Constructor.
     * @param delegate the delegated Registry
     * @param encrypter the data encrypter to use
     */
    public SecureRegistryWrapper(IRegistry delegate, IDataEncrypter encrypter) {
        this.delegate = delegate;
        this.encrypter = encrypter;
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#publishApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishApi(Api api, IAsyncResultHandler<Void> handler) {
        List<Policy> policies = api.getApiPolicies();
        encryptPolicies(api.getOrganizationId(), api.getApiId(), api.getVersion(), EntityType.Api, policies);
        encryptEndpointProperties(api.getOrganizationId(), api.getApiId(), api.getVersion(), EntityType.Api, api.getEndpointProperties());
        delegate.publishApi(api, handler);
        decryptPolicies(api.getOrganizationId(), api.getApiId(), api.getVersion(), EntityType.Api, policies);
        decryptEndpointProperties(api.getOrganizationId(), api.getApiId(), api.getVersion(), EntityType.Api, api.getEndpointProperties());
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#registerClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerClient(Client client, IAsyncResultHandler<Void> handler) {
        Set<Contract> contracts = client.getContracts();
        if (contracts != null) {
            for (Contract contract : contracts) {
                List<Policy> policies = contract.getPolicies();
                encryptPolicies(client.getOrganizationId(), client.getClientId(), client.getVersion(), EntityType.ClientApp, policies);
            }
        }
        delegate.registerClient(client, handler);
        if (contracts != null) {
            for (Contract contract : contracts) {
                List<Policy> policies = contract.getPolicies();
                decryptPolicies(client.getOrganizationId(), client.getClientId(), client.getVersion(), EntityType.ClientApp, policies);
            }
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#retireApi(io.apiman.gateway.engine.beans.Api, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireApi(Api api, IAsyncResultHandler<Void> handler) {
        delegate.retireApi(api, handler);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterClient(io.apiman.gateway.engine.beans.Client, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterClient(Client client, IAsyncResultHandler<Void> handler) {
        delegate.unregisterClient(client, handler);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getApi(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getApi(String organizationId, String apiId, String apiVersion,
            final IAsyncResultHandler<Api> handler) {
        delegate.getApi(organizationId, apiId, apiVersion, new IAsyncResultHandler<Api>() {
            @Override
            public void handle(IAsyncResult<Api> result) {
                if (result.isSuccess()) {
                    Api api = result.getResult();
                    if (api != null) {
                        List<Policy> policies = api.getApiPolicies();
                        decryptPolicies(organizationId, apiId, apiVersion, EntityType.Api, policies);
                        decryptEndpointProperties(organizationId, apiId, apiVersion, EntityType.Api, api.getEndpointProperties());
                    }
                }
                handler.handle(result);
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getClient(java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getClient(String apiKey, IAsyncResultHandler<Client> handler) {
        delegate.getClient(apiKey, result -> {
            if (result.isSuccess()) {
                Client client = result.getResult();
                if (client != null) {
                    for (Contract contract : client.getContracts()) {
                        decryptPolicies(client.getOrganizationId(), client.getClientId(), client.getVersion(),
                                EntityType.ClientApp, contract.getPolicies());
                    }
                }
            }
            handler.handle(result);
        });
    }


    @Override
    public void getClient(String organizationId, String clientId, String clientVersion, IAsyncResultHandler<Client> handler) {
        delegate.getClient(organizationId, clientId, clientVersion, result -> {
            if (result.isSuccess()) {
                Client client = result.getResult();
                if (client != null) {
                    for (Contract contract : client.getContracts()) {
                        decryptPolicies(client.getOrganizationId(), client.getClientId(), client.getVersion(),
                                EntityType.ClientApp, contract.getPolicies());
                    }
                }
            }
            handler.handle(result);
        });
    }


    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(java.lang.String, java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(String apiOrganizationId, String apiId, String apiVersion, String apiKey,
            IAsyncResultHandler<ApiContract> handler) {
        delegate.getContract(apiOrganizationId, apiId, apiVersion, apiKey, new IAsyncResultHandler<ApiContract>() {
            @Override
            public void handle(IAsyncResult<ApiContract> result) {
                if (result.isSuccess()) {
                    ApiContract contract = result.getResult();
                    List<Policy> policies = contract.getPolicies();
                    decryptPolicies(contract.getClient().getOrganizationId(),
                            contract.getClient().getClientId(), contract.getClient().getVersion(),
                            EntityType.ClientApp, policies);
                    Api api = contract.getApi();
                    if (api != null) {
                        List<Policy> apiPolicies = api.getApiPolicies();
                        decryptPolicies(api.getOrganizationId(), api.getApiId(), api.getVersion(),
                                EntityType.Api, apiPolicies);
                        decryptEndpointProperties(api.getOrganizationId(), api.getApiId(),
                                api.getVersion(), EntityType.Api, api.getEndpointProperties());
                    }
                }
                handler.handle(result);
            }
        });
    }

    @Override
    public void listApis(String organizationId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        delegate.listApis(organizationId, page, pageSize, handler);
    }

    @Override
    public void listOrgs(IAsyncResultHandler<List<String>> handler) {
        delegate.listOrgs(handler);
    }

    /**
     * @param entityType
     * @param entityVersion
     * @param entityId
     * @param orgId
     * @param entityType
     * @param policies
     */
    protected void encryptPolicies(String orgId, String entityId, String entityVersion, EntityType entityType,
            List<Policy> policies) {
        if (policies != null) {
            DataEncryptionContext ctx = new DataEncryptionContext(orgId, entityId, entityVersion, entityType);
            for (Policy policy : policies) {
                String jsonConfig = policy.getPolicyJsonConfig();
                policy.setPolicyJsonConfig(encrypter.encrypt(jsonConfig, ctx));
            }
        }
    }

    /**
     * @param endpointProperties
     */
    protected void encryptEndpointProperties(String orgId, String entityId, String entityVersion,
            EntityType entityType, Map<String, String> endpointProperties) {
        DataEncryptionContext ctx = new DataEncryptionContext(orgId, entityId, entityVersion, entityType);
        for (Entry<String, String> entry : endpointProperties.entrySet()) {
            entry.setValue(encrypter.encrypt(entry.getValue(), ctx));
        }
    }

    /**
     * @param policies
     */
    protected void decryptPolicies(String orgId, String entityId, String entityVersion, EntityType entityType, List<Policy> policies) {
        if (policies != null) {
            DataEncryptionContext ctx = new DataEncryptionContext(orgId, entityId, entityVersion, entityType);
            for (Policy policy : policies) {
                String encryptedConfig = policy.getPolicyJsonConfig();
                policy.setPolicyJsonConfig(encrypter.decrypt(encryptedConfig, ctx));
            }
        }
    }

    /**
     * @param endpointProperties
     */
    protected void decryptEndpointProperties(String orgId, String entityId, String entityVersion, EntityType entityType, Map<String, String> endpointProperties) {
        if (endpointProperties != null) {
            DataEncryptionContext ctx = new DataEncryptionContext(orgId, entityId, entityVersion, entityType);
            for (Entry<String, String> entry : endpointProperties.entrySet()) {
                entry.setValue(encrypter.decrypt(entry.getValue(), ctx));
            }
        }
    }

    @Override
    public void listApiVersions(String organizationId, String apiId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        delegate.listApiVersions(organizationId, apiId, page, pageSize, handler);
    }

    @Override
    public void listClients(String organizationId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        delegate.listClients(organizationId, page, pageSize, handler);
    }

    @Override
    public void listClientVersions(String organizationId, String clientId, int page, int pageSize, IAsyncResultHandler<List<String>> handler) {
        delegate.listClientVersions(organizationId, clientId, page, pageSize, handler);
    }

}
