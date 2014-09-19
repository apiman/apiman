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
package org.overlord.apiman.dt.api.config;


/**
 * Configuration interface.  The design time layer of apiman uses this to make
 * config decisions at runtime.  Platforms may provide alternative implementations
 * of this interface that are platform-specific.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IConfig {

    /**
     * Returns the endpoint to use when publishing applications and services to the Gateway
     * via the REST protocol.
     * @return the currently configured gateway REST endpoint
     */
    public String getGatewayRestEndpoint();
    
    /**
     * Gets the authentication type to use when publishing to the Gateway.
     * @return the authentication type
     */
    public String getGatewayAuthenticationType();

    /**
     * When using BASIC auth, returns the configured username.
     * @return the basic auth username
     */
    public String getGatewayBasicAuthUsername();

    /**
     * When using BASIC auth, returns the configured password to use.
     * @return the basic auth password
     */
    public String getGatewayBasicAuthPassword();

}
