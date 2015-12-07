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
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiContract;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Contract;
import io.apiman.gateway.engine.beans.exceptions.InvalidContractException;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;

/**
 * A registry that maintains a collection of APIs and Contracts that have
 * been published to the API Management runtime engine. This registry provides a
 * mechanism to both publish new ones (and remove/retire old ones) as well
 * as retrieve them.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IRegistry {

    /**
     * Gets the {@link Contract} to use based on information included in the inbound
     * API request.
     *
     * @param request an inbound API request
     * @param handler the result handler
     * @throws InvalidContractException when contract is invalid
     */
    public void getContract(ApiRequest request, IAsyncResultHandler<ApiContract> handler);

    /**
     * Publishes a new {@link Api} into the registry.
     * @param api the api being published
     * @param handler the result handler
     * @throws PublishingException when unable to publish api
     */
    public void publishApi(Api api, IAsyncResultHandler<Void> handler);

    /**
     * Retires (removes) a {@link Api} from the registry.
     * @param api the api
     * @param handler the result handler
     * @throws PublishingException when unable to retire api
     */
    public void retireApi(Api api, IAsyncResultHandler<Void> handler);

    /**
     * Registers a new {@link Application} with the registry.
     * @param application the application being registered
     * @param handler the result handler
     * @throws RegistrationException when unable to register entity
     */
    public void registerApplication(Application application, IAsyncResultHandler<Void> handler);

    /**
     * Removes an {@link Application} from the registry.
     * @param application the application to remove
     * @param handler the result handler
     * @throws RegistrationException when unable to register entity
     */
    public void unregisterApplication(Application application, IAsyncResultHandler<Void> handler);

    /**
     * Gets an API by its coordinates.
     * @param organizationId the org id
     * @param apiId the api id
     * @param apiVersion the api version
     * @param handler the result handler
     */
    public void getApi(String organizationId, String apiId, String apiVersion, IAsyncResultHandler<Api> handler);

}
