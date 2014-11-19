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

import org.overlord.apiman.rt.engine.async.IAsyncResultHandler;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;
import org.overlord.apiman.rt.engine.beans.exceptions.RegistrationException;
import org.overlord.apiman.rt.engine.impl.EngineResultImpl;

/**
 * The API Management runtime engine.  This engine can either be embedded or used as part
 * of a web app gateway.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IEngine {

    /**
     * @return the version of the engine
     */
    String getVersion();

    /**
     * Executes an asynchronous request for a managed service, with the provided
     * handler being passed an {@link EngineResultImpl} with the status and result
     * of the policy chain invocation.
     *
     * @param request a request for a managed service
     * @param handler an async handler called when a response is returned or an
     *            exception is captured.
     */
    IServiceRequestExecutor executor(ServiceRequest request, IAsyncResultHandler<IEngineResult> resultHandler);

    /**
     * Publishes a new {@link Service}.
     * @param service the service being published
     * @throws PublishingException
     */
    void publishService(Service service) throws PublishingException;

    /**
     * Retires (removes) a {@link Service} from the registry.
     * @param organizationId
     * @param serviceId
     * @param version
     * @throws PublishingException
     */
    void retireService(String organizationId, String serviceId, String version) throws PublishingException;

    /**
     * Registers a new {@link Application}.  An application is ultimately a collection of
     * contracts to managed services.
     * @param application the application being registered
     * @throws PublishingException
     */
    void registerApplication(Application application) throws RegistrationException;

    /**
     * Removes an {@link Application} from the registry.
     * @param organizationId
     * @param applicationId
     * @param version
     * @throws RegistrationException
     */
    void unregisterApplication(String organizationId, String applicationId, String version) throws RegistrationException;
}
