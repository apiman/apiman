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

import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.exceptions.ServiceInvocationException;

/**
 * A connector to a back end service.  An implementation of this interface
 * is used to invoke a back-end service in a specific way.  For example, 
 * one impelmentation of this interface may be able to invoke REST endpoints
 * over HTTP.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IServiceInvoker {
    
    /**
     * Asynchronously invoke a back end service.
     * @param service the service being invoked
     * @param request the inbound service request
     * @param handler the handler to use when reporting success or failure of the invokation
     */
    public void invokeAsync(Service service, ServiceRequest request, IServiceInvocationHandler handler);

    /**
     * Synchronously invoke a back end service.
     * @param service the service being invoked
     * @param request the inbound service request
     * @return the response from the back end service
     * @throws ServiceInvocationException
     */
    public ServiceResponse invoke(Service service, ServiceRequest request) throws ServiceInvocationException;

}
