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

import io.apiman.common.util.AesEncrypter;
import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.async.IAsyncResult;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Policy;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;
import io.apiman.gateway.engine.beans.ServiceRequest;

import java.util.List;
import java.util.Set;

/**
 * Wraps any {@link IRegistry} implementation to provide some encryption to
 * sensitive information.
 *
 * @author eric.wittmann@redhat.com
 */
public class SecureRegistryWrapper implements IRegistry {

    private final IRegistry delegate;

    /**
     * Constructor.
     * @param delegate the delegated Registry
     */
    public SecureRegistryWrapper(IRegistry delegate) {
        this.delegate = delegate;
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#publishService(io.apiman.gateway.engine.beans.Service, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishService(Service service, IAsyncResultHandler<Void> handler) {
        List<Policy> policies = service.getServicePolicies();
        encryptPolicies(policies);
        delegate.publishService(service, handler);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#registerApplication(io.apiman.gateway.engine.beans.Application, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerApplication(Application application, IAsyncResultHandler<Void> handler) {
        Set<Contract> contracts = application.getContracts();
        if (contracts != null) {
            for (Contract contract : contracts) {
                List<Policy> policies = contract.getPolicies();
                encryptPolicies(policies);
            }
        }
        delegate.registerApplication(application, handler);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#retireService(io.apiman.gateway.engine.beans.Service, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireService(Service service, IAsyncResultHandler<Void> handler) {
        delegate.retireService(service, handler);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterApplication(io.apiman.gateway.engine.beans.Application, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterApplication(Application application, IAsyncResultHandler<Void> handler) {
        delegate.unregisterApplication(application, handler);
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getService(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getService(String organizationId, String serviceId, String serviceVersion,
            final IAsyncResultHandler<Service> handler) {
        delegate.getService(organizationId, serviceId, serviceVersion, new IAsyncResultHandler<Service>() {
            @Override
            public void handle(IAsyncResult<Service> result) {
                if (result.isSuccess()) {
                    Service service = result.getResult();
                    if (service != null) {
                        List<Policy> policies = service.getServicePolicies();
                        decryptPolicies(policies);
                    }
                }
                handler.handle(result);
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(ServiceRequest request, final IAsyncResultHandler<ServiceContract> handler) {
        delegate.getContract(request, new IAsyncResultHandler<ServiceContract>() {
            @Override
            public void handle(IAsyncResult<ServiceContract> result) {
                if (result.isSuccess()) {
                    ServiceContract contract = result.getResult();
                    List<Policy> policies = contract.getPolicies();
                    decryptPolicies(policies);
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
                policy.setPolicyJsonConfig(AesEncrypter.encrypt(jsonConfig));
            }
        }
    }

    /**
     * @param policies
     */
    protected void decryptPolicies(List<Policy> policies) {
        if (policies != null) {
            for (Policy policy : policies) {
                String encryptedConfig = policy.getPolicyJsonConfig();
                policy.setPolicyJsonConfig(AesEncrypter.decrypt(encryptedConfig));
            }
        }
    }

}
