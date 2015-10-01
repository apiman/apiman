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
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Get;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extends the {@link ESRegistry} to provide single-node caching.  This caching solution
 * will not work in a cluster.  If looking for cluster support, either go with the core
 * ESRegistry or find/implement a caching registry that works in a cluster (e.g. leverage
 * jgroups?).
 *
 * @author eric.wittmann@redhat.com
 */
public class CachingESRegistry extends ESRegistry {

    private Map<String, ServiceContract> contractCache = new ConcurrentHashMap<>();
    private Map<String, Service> serviceCache = new HashMap<>();
    private Map<String, Application> applicationCache = new HashMap<>();
    private Object mutex = new Object();

    /**
     * Constructor.
     */
    public CachingESRegistry(Map<String, String> config) {
        super(config);
    }

    /**
     * Called to invalidate the cache - clearing it so that subsequent calls to getService()
     * or getContract() will trigger a new fetch from the ES store.
     */
    protected void invalidateCache() {
        synchronized (mutex) {
            contractCache.clear();
            serviceCache.clear();
            applicationCache.clear();
        }
    }

    /**
     * @see io.apiman.gateway.engine.es.ESRegistry#publishService(io.apiman.gateway.engine.beans.Service, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishService(final Service service, final IAsyncResultHandler<Void> handler) {
        super.publishService(service, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isSuccess()) {
                    cacheService(service);
                }
                handler.handle(result);
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.es.ESRegistry#registerApplication(io.apiman.gateway.engine.beans.Application, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerApplication(final Application application, final IAsyncResultHandler<Void> handler) {
        final Set<Contract> contracts = application.getContracts();
        super.registerApplication(application, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isSuccess()) {
                    application.setContracts(contracts);
                    cacheApplication(application);
                }
                handler.handle(result);
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.es.ESRegistry#retireService(io.apiman.gateway.engine.beans.Service, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireService(final Service service, final IAsyncResultHandler<Void> handler) {
        super.retireService(service, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isSuccess()) {
                    decacheService(service);
                }
                handler.handle(result);
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.es.ESRegistry#unregisterApplication(io.apiman.gateway.engine.beans.Application, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterApplication(final Application application, final IAsyncResultHandler<Void> handler) {
        super.unregisterApplication(application, new IAsyncResultHandler<Void>() {
            @Override
            public void handle(IAsyncResult<Void> result) {
                if (result.isSuccess()) {
                    decacheApplication(application);
                }
                handler.handle(result);
            }
        });
    }

    /**
     * @see io.apiman.gateway.engine.es.ESRegistry#getContract(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(final ServiceRequest request, final IAsyncResultHandler<ServiceContract> handler) {
        String contractKey = getContractKey(request);
        synchronized (mutex) {
            ServiceContract contract = contractCache.get(contractKey);
            if (contract != null) {
                String serviceKey = getServiceKey(contract.getService());
                Service service = serviceCache.get(serviceKey);
                if (service == null) {
                    super.getContract(request, handler);
                } else {
                    handler.handle(AsyncResultImpl.create(contract));
                }
            } else {
                super.getContract(request, new IAsyncResultHandler<ServiceContract>() {
                    @Override
                    public void handle(IAsyncResult<ServiceContract> result) {
                        if (result.isSuccess()) {
                            loadAndCacheApp(result.getResult().getApplication());
                        }
                        handler.handle(result);
                    }
                });
            }
        }
    }

    /**
     * @see io.apiman.gateway.engine.es.ESRegistry#getService(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getService(final String organizationId, final String serviceId, final String serviceVersion,
            final IAsyncResultHandler<Service> handler) {
        synchronized (mutex) {
            String serviceKey = getServiceKey(organizationId, serviceId, serviceVersion);
            Service service = serviceCache.get(serviceKey);
            if (service != null) {
                handler.handle(AsyncResultImpl.create(service));
            } else {
                super.getService(organizationId, serviceId, serviceVersion, new IAsyncResultHandler<Service>() {
                    @Override
                    public void handle(IAsyncResult<Service> result) {
                        if (result.isSuccess()) {
                            Service svc = result.getResult();
                            cacheService(svc);
                        }
                        handler.handle(result);
                    }
                });
            }
        }
    }

    /**
     * Called to cache the service for fast lookup later.
     * @param service
     */
    protected void cacheService(Service service) {
        if (service != null) {
            String serviceKey = getServiceKey(service);
            synchronized (mutex) {
                serviceCache.put(serviceKey, service);
            }
        }
    }

    /**
     * @param application
     */
    protected void cacheApplication(Application application) {
        String applicationKey = getApplicationKey(application);
        synchronized (mutex) {
            applicationCache.put(applicationKey, application);
            if (application.getContracts() != null) {
                for (Contract contract : application.getContracts()) {
                    String svcKey = getServiceKey(contract.getServiceOrgId(), contract.getServiceId(), contract.getServiceVersion());
                    Service service = serviceCache.get(svcKey);
                    ServiceContract sc = new ServiceContract(contract.getApiKey(), service, application, contract.getPlan(), contract.getPolicies());
                    String contractKey = getContractKey(contract);
                    contractCache.put(contractKey, sc);
                }
            }
        }
    }

    /**
     * @param application
     */
    protected void loadAndCacheApp(Application application) {
        String id = getApplicationKey(application);
        Get get = new Get.Builder(getIndexName(), id).type("application").build(); //$NON-NLS-1$
        getClient().executeAsync(get, new JestResultHandler<JestResult>() {
            @Override
            public void completed(JestResult result) {
                if (result.isSucceeded()) {
                    Map<String, Object> source = result.getSourceAsObject(Map.class);
                    Application app = ESRegistryMarshalling.unmarshallApplication(source);
                    cacheApplication(app);
                }
            }
            @Override
            public void failed(Exception e) {
            }
        });
    }

    /**
     * @param service
     */
    protected void decacheService(Service service) {
        String serviceKey = getServiceKey(service);
        synchronized (mutex) {
            if (serviceCache.containsKey(serviceKey)) {
                serviceCache.remove(serviceKey);
            }
        }
    }

    /**
     * @param application
     */
    protected void decacheApplication(Application application) {
        String applicationKey = getApplicationKey(application);
        synchronized (mutex) {
            if (applicationCache.containsKey(applicationKey)) {
                Application app = applicationCache.remove(applicationKey);
                for (Contract contract : app.getContracts()) {
                    String contractKey = getContractKey(contract);
                    if (contractCache.containsKey(contractKey)) {
                        contractCache.remove(contractKey);
                    }
                }
            }
        }
    }

    /**
     * Generates an in-memory key for an service, used to index the app for later quick
     * retrieval.
     * @param service an service
     * @return a service key
     */
    private String getServiceKey(Service service) {
        return getServiceKey(service.getOrganizationId(), service.getServiceId(), service.getVersion());
    }

    /**
     * Generates an in-memory key for an service, used to index the app for later quick
     * retrieval.
     * @param orgId
     * @param serviceId
     * @param version
     * @return a service key
     */
    private String getServiceKey(String orgId, String serviceId, String version) {
        return "SVC::" + orgId + "|" + serviceId + "|" + version; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Generates an in-memory key for an application, used to index the app for later quick
     * retrieval.
     * @param app an application
     * @return an application key
     */
    private String getApplicationKey(Application app) {
        return "APP::" + app.getOrganizationId() + "|" + app.getApplicationId() + "|" + app.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
     * Generates an in-memory key for a contract.
     * @param request
     */
    private String getContractKey(ServiceRequest request) {
        return "CONTRACT::" + request.getApiKey(); //$NON-NLS-1$
    }

    /**
     * Generates an in-memory key for a service contract, used to index the app for later quick
     * retrieval.
     * @param contract
     */
    private String getContractKey(Contract contract) {
        return "CONTRACT::" + contract.getApiKey(); //$NON-NLS-1$
    }

}
