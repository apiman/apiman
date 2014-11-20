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
package io.apiman.manager.api.war.config;

import io.apiman.manager.api.config.IConfig;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.configuration.Configuration;
import org.overlord.commons.config.ConfigurationFactory;

/**
 * An overlord-config implementation of the APIMan Design Time config
 * interface.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class Config implements IConfig {
    
    private static final String APIMAN_DT_API_CONFIG_FILE_NAME     = "apiman-manager-api.config.file.name"; //$NON-NLS-1$
    private static final String APIMAN_DT_API_CONFIG_FILE_REFRESH  = "apiman-manager-api.config.file.refresh"; //$NON-NLS-1$

    public static final String APIMAN_DT_API_GATEWAY_REST_ENDPOINT = "apiman-manager-api.gateway.rest-endpoint"; //$NON-NLS-1$
    public static final String APIMAN_DT_API_GATEWAY_AUTH_TYPE = "apiman-manager-api.gateway.authentication.type"; //$NON-NLS-1$
    public static final String APIMAN_DT_API_GATEWAY_BASIC_AUTH_USER = "apiman-manager-api.gateway.authentication.basic.user"; //$NON-NLS-1$
    public static final String APIMAN_DT_API_GATEWAY_BASIC_AUTH_PASS = "apiman-manager-api.gateway.authentication.basic.password"; //$NON-NLS-1$

    private static Configuration config;
    static {
        String configFile = System.getProperty(APIMAN_DT_API_CONFIG_FILE_NAME);
        String refreshDelayStr = System.getProperty(APIMAN_DT_API_CONFIG_FILE_REFRESH);
        Long refreshDelay = 5000l;
        if (refreshDelayStr != null) {
            refreshDelay = new Long(refreshDelayStr);
        }

        config = ConfigurationFactory.createConfig(
                configFile,
                "apiman.properties", //$NON-NLS-1$
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
     * @see io.apiman.manager.api.config.IConfig#getGatewayRestEndpoint()
     */
    @Override
    public String getGatewayRestEndpoint() {
        return config.getString(APIMAN_DT_API_GATEWAY_REST_ENDPOINT);
    }
    
    /**
     * @see io.apiman.manager.api.config.IConfig#getGatewayAuthenticationType()
     */
    @Override
    public String getGatewayAuthenticationType() {
        return config.getString(APIMAN_DT_API_GATEWAY_AUTH_TYPE);
    }

    /**
     * @see io.apiman.manager.api.config.IConfig#getGatewayBasicAuthUsername()
     */
    @Override
    public String getGatewayBasicAuthUsername() {
        return config.getString(APIMAN_DT_API_GATEWAY_BASIC_AUTH_USER);
    }

    /**
     * @see io.apiman.manager.api.config.IConfig#getGatewayBasicAuthPassword()
     */
    @Override
    public String getGatewayBasicAuthPassword() {
        return config.getString(APIMAN_DT_API_GATEWAY_BASIC_AUTH_PASS);
    }
    
}
