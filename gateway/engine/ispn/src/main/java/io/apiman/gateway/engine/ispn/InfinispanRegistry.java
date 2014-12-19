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
package io.apiman.gateway.engine.ispn;

import io.apiman.gateway.engine.IRegistry;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.ispn.i18n.Messages;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;

/**
 * An implementation of the Registry that uses infinispan as a storage
 * mechanism.  This is useful because an ISPN cache can be configured
 * in many different ways.
 *
 * @author eric.wittmann@redhat.com
 */
public class InfinispanRegistry implements IRegistry {
    
    private static final String DEFAULT_CACHE_CONTAINER = "java:jboss/infinispan/container/apiman-gateway"; //$NON-NLS-1$
    private static final String DEFAULT_CACHE = "registry"; //$NON-NLS-1$
    
    private String cacheContainer;
    private String cacheName;
    
    private Cache<Object, Object> cache;
    
    /**
     * Constructor.
     */
    public InfinispanRegistry() {
        cacheContainer = DEFAULT_CACHE_CONTAINER;
        cacheName = DEFAULT_CACHE;
    }
    
    /**
     * Constructor.
     * @param cacheContainer
     * @param cacheName
     */
    public InfinispanRegistry(String cacheContainer, String cacheName) {
        this.cacheContainer = cacheContainer;
        this.cacheName = cacheName;
    }
    
    /**
     * @see io.apiman.gateway.engine.IRegistry#publishService(io.apiman.gateway.engine.beans.Service)
     */
    @Override
    public synchronized void publishService(Service service) throws PublishingException {
        String serviceKey = getServiceKey(service);
        if (getCache().containsKey(serviceKey)) {
            throw new PublishingException(Messages.i18n.format("InfinispanRegistry.ServiceAlreadyPublished")); //$NON-NLS-1$
        }
        getCache().put(serviceKey, service);
    }
    
    /**
     * @see io.apiman.gateway.engine.IRegistry#retireService(io.apiman.gateway.engine.beans.Service)
     */
    @Override
    public synchronized void retireService(Service service) throws PublishingException {
        String serviceKey = getServiceKey(service);
        if (getCache().containsKey(serviceKey)) {
            getCache().remove(serviceKey);
        } else {
            throw new PublishingException(Messages.i18n.format("InfinispanRegistry.ServiceNotFound")); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#registerApplication(io.apiman.gateway.engine.beans.Application)
     */
    @Override
    public synchronized void registerApplication(Application application) throws RegistrationException {
        // Validate the application first - we need to be able to resolve all the contracts.
        for (Contract contract : application.getContracts()) {
            String contractKey = getContractKey(contract);
            if (getCache().containsKey(contractKey)) {
                throw new RegistrationException(Messages.i18n.format("InfinispanRegistry.ContractAlreadyPublished", //$NON-NLS-1$
                        contract.getApiKey()));
            }
            String svcKey = getServiceKey(contract.getServiceOrgId(), contract.getServiceId(), contract.getServiceVersion());
            if (!getCache().containsKey(svcKey)) {
                throw new RegistrationException(Messages.i18n.format("InfinispanRegistry.ServiceNotFoundInOrg", //$NON-NLS-1$
                        contract.getServiceId(), contract.getServiceOrgId()));
            }
        }
        
        String applicationKey = getApplicationKey(application);
        if (getCache().containsKey(applicationKey)) {
            throw new RegistrationException(Messages.i18n.format("InfinispanRegistry.AppAlreadyRegistered")); //$NON-NLS-1$
        }
        getCache().put(applicationKey, application);
        for (Contract contract : application.getContracts()) {
            String svcKey = getServiceKey(contract.getServiceOrgId(), contract.getServiceId(), contract.getServiceVersion());
            Service service = (Service) getCache().get(svcKey);
            ServiceContract sc = new ServiceContract(contract.getApiKey(), service, application, contract.getPolicies());
            String contractKey = getContractKey(contract);
            getCache().put(contractKey, sc);
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#unregisterApplication(io.apiman.gateway.engine.beans.Application)
     */
    @Override
    public synchronized void unregisterApplication(Application application) throws RegistrationException {
        String applicationKey = getApplicationKey(application);
        if (getCache().containsKey(applicationKey)) {
            Application removed = (Application) getCache().remove(applicationKey);
            for (Contract contract : removed.getContracts()) {
                String contractKey = getContractKey(contract);
                if (getCache().containsKey(contractKey)) {
                    getCache().remove(contractKey);
                }
            }
        } else {
            throw new RegistrationException(Messages.i18n.format("InfinispanRegistry.AppNotFound")); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.gateway.engine.IRegistry#getContract(io.apiman.gateway.engine.beans.ServiceRequest)
     */
    @Override
    public ServiceContract getContract(ServiceRequest request) throws InvalidContractException {
        String contractKey = getContractKey(request);
        ServiceContract contract = (ServiceContract) getCache().get(contractKey);
        if (contract == null) {
            throw new InvalidContractException(Messages.i18n.format("InfinispanRegistry.NoContractForAPIKey", request.getApiKey())); //$NON-NLS-1$
        }
        // Has the service been retired?
        Service service = contract.getService();
        String serviceKey = getServiceKey(service);
        if (getCache().get(serviceKey) == null) {
            throw new InvalidContractException(Messages.i18n.format("InfinispanRegistry.ServiceWasRetired", //$NON-NLS-1$ 
                    service.getServiceId(), service.getOrganizationId()));
        };
        return contract;
    }
    
    /**
     * @see io.apiman.gateway.engine.IRegistry#getService(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Service getService(String organizationId, String serviceId, String serviceVersion) {
        String key = getServiceKey(organizationId, serviceId, serviceVersion);
        return (Service) cache.get(key);
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
     * @return gets the registry cache
     */
    private Cache<Object, Object> getCache() {
        if (cache != null) {
            return cache;
        }
        
        try {
            InitialContext ic = new InitialContext();
            CacheContainer container = (CacheContainer) ic.lookup(cacheContainer);
            cache = container.getCache(cacheName);
            return cache;
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

}
