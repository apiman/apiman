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
package org.overlord.apiman.rt.engine.mem;

import java.util.HashMap;
import java.util.Map;

import org.overlord.apiman.rt.engine.IRegistry;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Contract;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.exceptions.InvalidContractException;
import org.overlord.apiman.rt.engine.beans.exceptions.InvalidServiceException;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;
import org.overlord.apiman.rt.engine.beans.exceptions.RegistrationException;
import org.overlord.apiman.rt.engine.mem.i18n.Messages;

/**
 * An in-memory implementation of the registry.
 *
 * @author eric.wittmann@redhat.com
 */
public class InMemoryRegistry implements IRegistry {

    private Map<String, Service> services = new HashMap<String, Service>();
    private Map<String, Application> applications = new HashMap<String, Application>();
    private Map<String, Contract> contracts = new HashMap<String, Contract>();

    /**
     * Constructor.
     */
    public InMemoryRegistry() {
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IRegistry#getService(org.overlord.apiman.rt.engine.beans.ServiceRequest)
     */
    @Override
    public Service getService(ServiceRequest request) throws InvalidServiceException {
        String serviceKey = request.getOrganization() + "|" + request.getService() + "|" + request.getVersion(); //$NON-NLS-1$ //$NON-NLS-2$
        Service service = services.get(serviceKey);
        if (service == null) {
            throw new InvalidServiceException(Messages.i18n.format("InMemoryRegistry.ServiceNotFoundInOrg",  //$NON-NLS-1$
                    request.getService(), request.getOrganization()));
        }
        return service;
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IRegistry#publishService(org.overlord.apiman.rt.engine.beans.Service)
     */
    @Override
    public synchronized void publishService(Service service) throws PublishingException {
        String serviceKey = service.getServiceKey();
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
        String serviceKey = service.getServiceKey();
        if (services.containsKey(serviceKey)) {
            services.remove(serviceKey);
        }
        throw new PublishingException(Messages.i18n.format("InMemoryRegistry.ServiceNotFound")); //$NON-NLS-1$
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IRegistry#registerApplication(org.overlord.apiman.rt.engine.beans.Application)
     */
    @Override
    public synchronized void registerApplication(Application application) throws RegistrationException {
        String applicationKey = application.getApplicationKey();
        if (applications.containsKey(applicationKey)) {
            throw new RegistrationException(Messages.i18n.format("InMemoryRegistry.AppAlreadyRegistered")); //$NON-NLS-1$
        }
        applications.put(applicationKey, application);
        for (Contract contract : application.getContracts()) {
            registerContract(contract);
        }
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.IRegistry#unregisterApplication(org.overlord.apiman.rt.engine.beans.Application)
     */
    @Override
    public void unregisterApplication(Application application) throws RegistrationException {
        String applicationKey = application.getApplicationKey();
        if (applications.containsKey(applicationKey)) {
            Application removed = applications.remove(applicationKey);
            for (Contract contract : removed.getContracts()) {
                removeContract(contract);
            }
        }
        throw new RegistrationException(Messages.i18n.format("InMemoryRegistry.AppNotFound")); //$NON-NLS-1$
    }

    /**
     * Removes a contract from the registry.
     * @param contract
     */
    private void removeContract(Contract contract) {
        if (contracts.containsKey(contract.getApiKey())) {
            contracts.remove(contract.getApiKey());
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.IRegistry#getContract(org.overlord.apiman.rt.engine.beans.ServiceRequest)
     */
    @Override
    public Contract getContract(ServiceRequest request) throws InvalidContractException {
        Contract contract = contracts.get(request.getApiKey());
        if (contract == null) {
            throw new InvalidContractException(Messages.i18n.format("InMemoryRegistry.NoContractForAPIKey", request.getApiKey())); //$NON-NLS-1$
        }
        String serviceId = contract.getServiceId();
        String orgId = contract.getServiceOrgId();
        if (!serviceId.equals(request.getService()) || !orgId.equals(request.getOrganization())) {
            throw new InvalidContractException(Messages.i18n.format("InMemoryRegistry.InvalidContract")); //$NON-NLS-1$
        }
        return contract;
    }

    /**
     * Registers a contract.  Potentially many of these will happen per application.
     * @param contract the contract being registered
     * @throws PublishingException
     */
    private void registerContract(Contract contract) throws RegistrationException {
        if (contracts.containsKey(contract.getApiKey())) {
            throw new RegistrationException(Messages.i18n.format("InMemoryRegistry.ContractAlreadyPublished", //$NON-NLS-1$
                    contract.getApiKey()));
        }
        contracts.put(contract.getApiKey(), contract);
    }

}
