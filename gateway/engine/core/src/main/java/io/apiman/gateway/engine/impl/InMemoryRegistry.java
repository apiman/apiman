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
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;
import io.apiman.gateway.engine.beans.ServiceRequest;
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
     * @see io.apiman.gateway.engine.IRegistry#publishService(io.apiman.gateway.engine.beans.Service, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void publishService(Service service, IAsyncResultHandler<Void> handler) {
        Exception error = null;
        synchronized (mutex) {
            String serviceKey = getServiceKey(service);
            if (getMap().containsKey(serviceKey)) {
                error = new PublishingException(Messages.i18n.format("InMemoryRegistry.ServiceAlreadyPublished")); //$NON-NLS-1$
            } else {
                getMap().put(serviceKey, service);
            }
        }
        if (error == null) {
            handler.handle(AsyncResultImpl.create((Void) null));
        } else {
            handler.handle(AsyncResultImpl.create(error, Void.class));
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.IRegistry#retireService(io.apiman.gateway.engine.beans.Service, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void retireService(Service service, IAsyncResultHandler<Void> handler) {
        Exception error = null;
        synchronized (mutex) {
            String serviceKey = getServiceKey(service);
            if (getMap().containsKey(serviceKey)) {
                getMap().remove(serviceKey);
            } else {
                error = new PublishingException(Messages.i18n.format("InMemoryRegistry.ServiceNotFound")); //$NON-NLS-1$
            }
        }
        if (error == null) {
            handler.handle(AsyncResultImpl.create((Void) null));
        } else {
            handler.handle(AsyncResultImpl.create(error, Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#registerApplication(io.apiman.gateway.engine.beans.Application, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void registerApplication(Application application, IAsyncResultHandler<Void> handler) {
        Exception error = null;
        synchronized (mutex) {
            // Validate the application first - we need to be able to resolve all the contracts.
            for (Contract contract : application.getContracts()) {
                String contractKey = getContractKey(contract);
                if (getMap().containsKey(contractKey)) {
                    error = new RegistrationException(Messages.i18n.format("InMemoryRegistry.ContractAlreadyPublished", //$NON-NLS-1$
                            contract.getApiKey()));
                    break;
                }
                String svcKey = getServiceKey(contract.getServiceOrgId(), contract.getServiceId(), contract.getServiceVersion());
                if (!getMap().containsKey(svcKey)) {
                    error = new RegistrationException(Messages.i18n.format("InMemoryRegistry.ServiceNotFoundInOrg", //$NON-NLS-1$
                            contract.getServiceId(), contract.getServiceOrgId()));
                    break;
                }
            }
            String applicationKey = getApplicationKey(application);
            if (getMap().containsKey(applicationKey)) {
                error = new RegistrationException(Messages.i18n.format("InMemoryRegistry.AppAlreadyRegistered")); //$NON-NLS-1$
            } else {
                getMap().put(applicationKey, application);
                for (Contract contract : application.getContracts()) {
                    String svcKey = getServiceKey(contract.getServiceOrgId(), contract.getServiceId(), contract.getServiceVersion());
                    Service service = (Service) getMap().get(svcKey);
                    ServiceContract sc = new ServiceContract(contract.getApiKey(), service, application, contract.getPolicies());
                    String contractKey = getContractKey(contract);
                    getMap().put(contractKey, sc);
                }
            }
        }
        if (error == null) {
            handler.handle(AsyncResultImpl.create((Void) null));
        } else {
            handler.handle(AsyncResultImpl.create(error, Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterApplication(io.apiman.gateway.engine.beans.Application, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void unregisterApplication(Application application, IAsyncResultHandler<Void> handler) {
        Exception error = null;
        synchronized (mutex) {
            String applicationKey = getApplicationKey(application);
            if (getMap().containsKey(applicationKey)) {
                Application removed = (Application) getMap().remove(applicationKey);
                for (Contract contract : removed.getContracts()) {
                    String contractKey = getContractKey(contract);
                    if (getMap().containsKey(contractKey)) {
                        getMap().remove(contractKey);
                    }
                }
            } else {
                error = new RegistrationException(Messages.i18n.format("InMemoryRegistry.AppNotFound")); //$NON-NLS-1$
            }
        }
        if (error == null) {
            handler.handle(AsyncResultImpl.create((Void) null));
        } else {
            handler.handle(AsyncResultImpl.create(error, Void.class));
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(io.apiman.gateway.engine.beans.ServiceRequest, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getContract(ServiceRequest request, IAsyncResultHandler<ServiceContract> handler) {
        String contractKey = getContractKey(request);
        ServiceContract contract = (ServiceContract) getMap().get(contractKey);
        
        if (contract == null) {
            Exception error = new InvalidContractException(Messages.i18n.format("InMemoryRegistry.NoContractForAPIKey", request.getApiKey())); //$NON-NLS-1$
            handler.handle(AsyncResultImpl.create(error, ServiceContract.class));
            return;
        }
        // Has the service been retired?
        Service service = contract.getService();
        String serviceKey = getServiceKey(service);
        if (getMap().get(serviceKey) == null) {
            Exception error = new InvalidContractException(Messages.i18n.format("InMemoryRegistry.ServiceWasRetired", //$NON-NLS-1$ 
                    service.getServiceId(), service.getOrganizationId()));
            handler.handle(AsyncResultImpl.create(error, ServiceContract.class));
            return;
        }
        
        handler.handle(AsyncResultImpl.create(contract));
    }
    
    /**
     * @see io.apiman.gateway.engine.IRegistry#getService(java.lang.String, java.lang.String, java.lang.String, io.apiman.gateway.engine.async.IAsyncResultHandler)
     */
    @Override
    public void getService(String organizationId, String serviceId, String serviceVersion,
            IAsyncResultHandler<Service> handler) {
        String key = getServiceKey(organizationId, serviceId, serviceVersion);
        Service service = (Service) getMap().get(key);
        handler.handle(AsyncResultImpl.create(service));
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
    
    /**
     * @return the map to use when storing stuff
     */
    protected Map<String, Object> getMap() {
        return map;
    }

}
