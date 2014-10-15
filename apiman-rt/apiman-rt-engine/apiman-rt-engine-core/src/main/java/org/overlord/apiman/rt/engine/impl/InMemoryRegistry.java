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
package org.overlord.apiman.rt.engine.impl;

import java.util.HashMap;
import java.util.Map;

import org.overlord.apiman.rt.engine.IRegistry;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Contract;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceContract;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.exceptions.InvalidContractException;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;
import org.overlord.apiman.rt.engine.beans.exceptions.RegistrationException;
import org.overlord.apiman.rt.engine.i18n.Messages;

/**
 * An in-memory implementation of the registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class InMemoryRegistry implements IRegistry {

    private Map<String, Service> services = new HashMap<String, Service>();
    private Map<String, Application> applications = new HashMap<String, Application>();
    private Map<String, ServiceContract> contracts = new HashMap<String, ServiceContract>();

    /**
     * Constructor.
     */
    public InMemoryRegistry() {
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IRegistry#publishService(org.overlord.apiman.rt.engine.beans.Service)
     */
    @Override
    public synchronized void publishService(Service service) throws PublishingException {
        String serviceKey = getServiceKey(service);
        if (services.containsKey(serviceKey)) {
            throw new PublishingException(Messages.i18n.format("InMemoryRegistry.ServiceAlreadyPublished")); //$NON-NLS-1$
        }
        services.put(serviceKey, service);
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IRegistry#retireService(org.overlord.apiman.rt.engine.beans.Service)
     */
    @Override
    public synchronized void retireService(Service service) throws PublishingException {
        String serviceKey = getServiceKey(service);
        if (services.containsKey(serviceKey)) {
            services.remove(serviceKey);
        } else {
            throw new PublishingException(Messages.i18n.format("InMemoryRegistry.ServiceNotFound")); //$NON-NLS-1$
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.IRegistry#registerApplication(org.overlord.apiman.rt.engine.beans.Application)
     */
    @Override
    public synchronized void registerApplication(Application application) throws RegistrationException {
        // Validate the application first - we need to be able to resolve all the contracts.
        for (Contract contract : application.getContracts()) {
            if (contracts.containsKey(contract.getApiKey())) {
                throw new RegistrationException(Messages.i18n.format("InMemoryRegistry.ContractAlreadyPublished", //$NON-NLS-1$
                        contract.getApiKey()));
            }
            String svcKey = getServiceKey(contract.getServiceOrgId(), contract.getServiceId(), contract.getServiceVersion());
            if (!services.containsKey(svcKey)) {
                throw new RegistrationException(Messages.i18n.format("InMemoryRegistry.ServiceNotFoundInOrg", //$NON-NLS-1$
                        contract.getServiceId(), contract.getServiceOrgId()));
            }
        }
        
        String applicationKey = getApplicationKey(application);
        if (applications.containsKey(applicationKey)) {
            throw new RegistrationException(Messages.i18n.format("InMemoryRegistry.AppAlreadyRegistered")); //$NON-NLS-1$
        }
        applications.put(applicationKey, application);
        for (Contract contract : application.getContracts()) {
            String svcKey = getServiceKey(contract.getServiceOrgId(), contract.getServiceId(), contract.getServiceVersion());
            ServiceContract sc = new ServiceContract(contract.getApiKey(), services.get(svcKey), application, contract.getPolicies());
            contracts.put(contract.getApiKey(), sc);
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.IRegistry#unregisterApplication(org.overlord.apiman.rt.engine.beans.Application)
     */
    @Override
    public synchronized void unregisterApplication(Application application) throws RegistrationException {
        String applicationKey = getApplicationKey(application);
        if (applications.containsKey(applicationKey)) {
            Application removed = applications.remove(applicationKey);
            for (Contract contract : removed.getContracts()) {
                if (contracts.containsKey(contract.getApiKey())) {
                    contracts.remove(contract.getApiKey());
                }
            }
        } else {
            throw new RegistrationException(Messages.i18n.format("InMemoryRegistry.AppNotFound")); //$NON-NLS-1$
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.IRegistry#getContract(org.overlord.apiman.rt.engine.beans.ServiceRequest)
     */
    @Override
    public ServiceContract getContract(ServiceRequest request) throws InvalidContractException {
        ServiceContract contract = contracts.get(request.getApiKey());
        if (contract == null) {
            throw new InvalidContractException(Messages.i18n.format("InMemoryRegistry.NoContractForAPIKey", request.getApiKey())); //$NON-NLS-1$
        }
        return contract;
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
        return orgId + "|" + serviceId + "|" + version; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Generates an in-memory key for an application, used to index the app for later quick
     * retrieval.
     * @param app an application
     * @return an application key
     */
    private String getApplicationKey(Application app) {
        return app.getOrganizationId() + "|" + app.getApplicationId() + "|" + app.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$
    }


}
