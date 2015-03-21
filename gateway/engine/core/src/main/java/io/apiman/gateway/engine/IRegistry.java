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
package io.apiman.gateway.engine;

import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceContract;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;

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
     * @param handler
     * @throws InvalidContractException
     */
    public void getContract(ServiceRequest request, IAsyncResultHandler<ServiceContract> handler);

    /**
     * Publishes a new {@link Service} into the registry.
     * @param service the service being published
     * @param handler
     * @throws PublishingException
     */
    public void publishService(Service service, IAsyncResultHandler<Void> handler);
    
    /**
     * Retires (removes) a {@link Service} from the registry.
     * @param service
     * @param handler
     * @throws PublishingException
     */
    public void retireService(Service service, IAsyncResultHandler<Void> handler);
    
    /**
     * Registers a new {@link Application} with the registry.
     * @param application the application being registered
     * @param handler
     * @throws RegistrationException
     */
    public void registerApplication(Application application, IAsyncResultHandler<Void> handler);

    /**
     * Removes an {@link Application} from the registry.
     * @param application the application to remove
     * @param handler
     * @throws RegistrationException
     */
    public void unregisterApplication(Application application, IAsyncResultHandler<Void> handler);

    /**
     * Gets a service by its service coordinates.
     * @param organizationId
     * @param serviceId
     * @param serviceVersion
     * @param handler
     */
    public void getService(String organizationId, String serviceId, String serviceVersion, IAsyncResultHandler<Service> handler);

}
