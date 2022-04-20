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
package io.apiman.gateway.api.rest.impl;

import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.GatewayEndpoint;

/**
 * An interface used by the REST layer when getting information that must
 * be provided by the platform.  For example, when the gateway is running
 * within a WAR (e.g. running on wildfly) then that particular implementation
 * must provide certain information.  This interface allows the REST impl
 * itself to not care about those details.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IPlatform {

    /**
     * Gets the endpoint for the given API.  It is up to the platform to determine
     * this endpoint.
     * @param organizationId the org id
     * @param apiId the API id
     * @param version the version id
     * @return the API endpoint
     */
    public ApiEndpoint getApiEndpoint(String organizationId, String apiId, String version);

    /**
     * Gets the gateway endpoint.
     * @return the gateway endpoint
     */
    @Deprecated(since = "1.3.0.Final", forRemoval = true)
    public GatewayEndpoint getEndpoint();

}
