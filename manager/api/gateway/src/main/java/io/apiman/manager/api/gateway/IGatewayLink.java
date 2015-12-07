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
package io.apiman.manager.api.gateway;

import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;

/**
 * Links the design time API with a Gateway.  This allows the design time API
 * to interface with the runtime Gateway in order to do things like publishing
 * APIs and Contracts.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IGatewayLink {

    /**
     * Gets the current status of the gateway.
     * @return the system status
     * @throws GatewayAuthenticationException when unable to authenticate with gateway
     */
    public SystemStatus getStatus() throws GatewayAuthenticationException;

    /**
     * Publishes a new {@link Api}.
     * @param api the api being published
     * @throws PublishingException when unable to publish api
     * @throws GatewayAuthenticationException when unable to authenticate with gateway
     */
    public void publishApi(Api api) throws PublishingException, GatewayAuthenticationException;

    /**
     * Retires (removes) a {@link Api} from the registry.
     * @param api the api to retire/remove
     * @throws PublishingException when unable to retire api
     * @throws GatewayAuthenticationException when unable to authenticate with gateway
     */
    public void retireApi(Api api) throws PublishingException, GatewayAuthenticationException;

    /**
     * Registers a new {@link Application}.  An application is ultimately a collection of
     * contracts to managed apis.
     * @param application the application being registered
     * @throws RegistrationException when unable to register application
     * @throws GatewayAuthenticationException when unable to authenticate with gateway
     * @throws PublishingException when unable to publish application
     */
    public void registerApplication(Application application) throws RegistrationException, GatewayAuthenticationException;

    /**
     * Removes an {@link Application} from the registry.
     * @param application the application to remove
     * @throws RegistrationException when unable to register
     * @throws GatewayAuthenticationException when unable to authenticate with gateway
     */
    public void unregisterApplication(Application application) throws RegistrationException, GatewayAuthenticationException;

    /**
     * Gets the api endpoint from the gateway.
     * @param organizationId the org id
     * @param apiId the api id
     * @param version the version
     * @return the api endpoint
     * @throws GatewayAuthenticationException when unable to authenticate with gateway
     * @throws NotAuthorizedException when not authorized to perform action
     */
    public ApiEndpoint getApiEndpoint(String organizationId, String apiId, String version)
            throws GatewayAuthenticationException;

    /**
     * Close down the gateway link when it's no longer needed.
     */
    public void close();

}
