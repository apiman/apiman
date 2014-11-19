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
import org.overlord.apiman.rt.engine.beans.ServiceContract;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.exceptions.InvalidContractException;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;
import org.overlord.apiman.rt.engine.beans.exceptions.RegistrationException;

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
     * Gets the {@link Contract} to use based on information included in the inbound
     * service request.
     * 
     * @param request an inbound service request
     * @return a Contract or null if not found
     */
    public ServiceContract getContract(ServiceRequest request) throws InvalidContractException;

    /**
     * Publishes a new {@link Service} into the registry.
     * @param service the service being published
     * @throws PublishingException
     */
    public void publishService(Service service) throws PublishingException;
    
    /**
     * Retires (removes) a {@link Service} from the registry.
     * @param service
     * @throws PublishingException
     */
    public void retireService(Service service) throws PublishingException;
    
    /**
     * Registers a new {@link Application} with the registry.
     * @param application the application being registered
     * @throws RegistrationException
     */
    public void registerApplication(Application application) throws RegistrationException;
    
    /**
     * Removes an {@link Application} from the registry.
     * @param application the application to remove
     * @throws RegistrationException
     */
    public void unregisterApplication(Application application) throws RegistrationException;

}
