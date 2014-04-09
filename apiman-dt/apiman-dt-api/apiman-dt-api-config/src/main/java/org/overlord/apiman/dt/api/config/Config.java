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

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.configuration.Configuration;
import org.overlord.commons.config.ConfigurationFactory;

/**
 * Global access to configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class Config {
    
    public static final String APIMAN_DT_API_CONFIG_FILE_NAME     = "apiman-dt-api.config.file.name"; //$NON-NLS-1$
    public static final String APIMAN_DT_API_CONFIG_FILE_REFRESH  = "apiman-dt-api.config.file.refresh"; //$NON-NLS-1$

    public static final String APIMAN_DT_API_GATEWAY_REST_ENDPOINT = "apiman-dt-api.gateway.rest-endpoint"; //$NON-NLS-1$
    public static final String APIMAN_DT_API_GATEWAY_AUTH_TYPE = "apiman-dt-api.gateway.authentication.type"; //$NON-NLS-1$
    public static final String APIMAN_DT_API_GATEWAY_BASIC_AUTH_USER = "apiman-dt-api.gateway.authentication.basic.user"; //$NON-NLS-1$
    public static final String APIMAN_DT_API_GATEWAY_BASIC_AUTH_PASS = "apiman-dt-api.gateway.authentication.basic.password"; //$NON-NLS-1$

    public static Configuration config;
    static {
        String configFile = System.getProperty(APIMAN_DT_API_CONFIG_FILE_NAME);
        String refreshDelayStr = System.getProperty(APIMAN_DT_API_CONFIG_FILE_REFRESH);
        Long refreshDelay = 5000l;
        if (refreshDelayStr != null) {
            refreshDelay = new Long(refreshDelayStr);
        }

        config = ConfigurationFactory.createConfig(
                configFile,
                "apiman-dt-api.properties", //$NON-NLS-1$
                refreshDelay,
                null,
                Config.class);
    }

    /**
     * Constructor.
     */
    public Config() {
    }
    
    /**
     * Returns the endpoint to use when publishing applications and services to the Gateway
     * via the REST protocol.
     * @return the currently configured gateway REST endpoint
     */
    public String getGatewayRestEndpoint() {
        return config.getString(APIMAN_DT_API_GATEWAY_REST_ENDPOINT);
    }
    
    /**
     * Gets the authentication type to use when publishing to the Gateway.
     * @return the authentication type
     */
    public String getGatewayAuthenticationType() {
        return config.getString(APIMAN_DT_API_GATEWAY_AUTH_TYPE);
    }

    /**
     * When using BASIC auth, returns the configured username.
     * @return the basic auth username
     */
    public String getGatewayBasicAuthUsername() {
        return config.getString(APIMAN_DT_API_GATEWAY_BASIC_AUTH_USER);
    }

    /**
     * When using BASIC auth, returns the configured password to use.
     * @return the basic auth password
     */
    public String getGatewayBasicAuthPassword() {
        return config.getString(APIMAN_DT_API_GATEWAY_BASIC_AUTH_PASS);
    }

    
}
