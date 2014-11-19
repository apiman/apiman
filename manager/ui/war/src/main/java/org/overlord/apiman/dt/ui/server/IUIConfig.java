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
package org.overlord.apiman.dt.ui.server;

import org.overlord.apiman.dt.ui.client.shared.beans.ApiAuthType;

/**
 * Interface providing UI configuration information.
 * 
 * @author eric.wittmann@redhat.com
 */
public interface IUIConfig {

    /**
     * Gets the management layer's API endpoint.
     */
    public String getManagementApiEndpoint();

    /**
     * Gets the management layer's API authentication type.
     */
    public ApiAuthType getManagementApiAuthType();

    /**
     * Gets the base url of the runtime gateway.
     */
    public String getGatewayUrl();

    /**
     * Gets the username to use when doing basic auth to the management layer
     * api.
     */
    public String getManagementApiAuthUsername();

    /**
     * Gets the password to use when doing basic auth to the management layer
     * api.
     */
    public String getManagementApiAuthPassword();

    /**
     * Gets the classname of the token generator to use when doing token auth to
     * the management layer api.
     */
    public String getManagementApiAuthTokenGenerator();

    /**
     * Gets the URL to use to logout of the UI.
     */
    public String getLogoutUrl();

}
