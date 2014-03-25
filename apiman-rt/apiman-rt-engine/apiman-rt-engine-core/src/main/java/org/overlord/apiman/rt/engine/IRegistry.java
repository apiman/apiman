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
package org.overlord.apiman.rt.engine;

import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Contract;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.exceptions.InvalidContractException;
import org.overlord.apiman.rt.engine.exceptions.InvalidServiceException;
import org.overlord.apiman.rt.engine.exceptions.PublishingException;
import org.overlord.apiman.rt.engine.exceptions.RegistrationException;

/**
 * A registry that maintains a collection of Services and Contracts that have
 * been published to the API Management runtime engine. This registry provides a
 * mechanism to both publish new ones (and remove/retire old ones) as well
 * as retrieve them.
 * 
 * @author eric.wittmann@redhat.com
 */
public interface IRegistry {

    /**
     * Gets the Service being invoked based on information included in the inbound
     * service request.
     * @param request an inbound service request
     * @return a Service or null if not found
     */
    public Service getService(ServiceRequest request) throws InvalidServiceException;
    
    /**
     * Publishes a new Service into the registry.
     * @param service the service being published
     * @throws PublishingException
     */
    public void publishService(Service service) throws PublishingException;
    
    /**
     * Registers a new application with the registry.
     * @param application the application being registered
     * @throws RegistrationException
     */
    public void registerApplication(Application application) throws RegistrationException;

    /**
     * Gets the Contract to use based on information included in the inbound
     * service request.
     * 
     * @param request an inbound service request
     * @return a Contract or null if not found
     */
    public Contract getContract(ServiceRequest request) throws InvalidContractException;

}
