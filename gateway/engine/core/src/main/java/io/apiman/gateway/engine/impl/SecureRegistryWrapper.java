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
        encryptPolicies(policies);
        encryptEndpointProperties(api.getEndpointProperties());
        delegate.publishApi(api, handler);
        decryptPolicies(policies);
        decryptEndpointProperties(api.getEndpointProperties());
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
                encryptPolicies(policies);
            }
        }
        delegate.registerClient(client, handler);
        if (contracts != null) {
            for (Contract contract : contracts) {
                List<Policy> policies = contract.getPolicies();
                decryptPolicies(policies);
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
                        decryptPolicies(policies);
                        decryptEndpointProperties(api.getEndpointProperties());
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
        delegate.getClient(apiKey, new IAsyncResultHandler<Client>() {
            @Override
            public void handle(IAsyncResult<Client> result) {
                if (result.isSuccess()) {
                    Client client = result.getResult();
                    for (Contract contract : client.getContracts()) {
                        decryptPolicies(contract.getPolicies());
                    }
                }
                handler.handle(result);
            }
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
                    decryptPolicies(policies);
                    Api api = contract.getApi();
                    if (api != null) {
                        List<Policy> apiPolicies = api.getApiPolicies();
                        decryptPolicies(apiPolicies);
                        decryptEndpointProperties(api.getEndpointProperties());
                    }
                }
                handler.handle(result);
            }
        });
    }

    /**
     * @param policies
     */
    protected void encryptPolicies(List<Policy> policies) {
        if (policies != null) {
            for (Policy policy : policies) {
                String jsonConfig = policy.getPolicyJsonConfig();
                policy.setPolicyJsonConfig(encrypter.encrypt(jsonConfig));
            }
        }
    }

    /**
     * @param endpointProperties
     */
    protected void encryptEndpointProperties(Map<String, String> endpointProperties) {
        for (Entry<String, String> entry : endpointProperties.entrySet()) {
            entry.setValue(encrypter.encrypt(entry.getValue()));
        }
    }

    /**
     * @param policies
     */
    protected void decryptPolicies(List<Policy> policies) {
        if (policies != null) {
            for (Policy policy : policies) {
                String encryptedConfig = policy.getPolicyJsonConfig();
                policy.setPolicyJsonConfig(encrypter.decrypt(encryptedConfig));
            }
        }
    }

    /**
     * @param endpointProperties
     */
    protected void decryptEndpointProperties(Map<String, String> endpointProperties) {
        if (endpointProperties != null) {
            for (Entry<String, String> entry : endpointProperties.entrySet()) {
                entry.setValue(encrypter.decrypt(entry.getValue()));
            }
        }
    }

}
