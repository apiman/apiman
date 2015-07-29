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
package io.apiman.manager.ui.server;

import io.apiman.common.config.ConfigFactory;
import io.apiman.manager.ui.server.beans.ApiAuthType;

import org.apache.commons.configuration.Configuration;

/**
 * Global access to configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
public class UIConfig implements IUIConfig {

    public static final String APIMAN_MANAGER_UI_API_ENDPOINT = "apiman-manager-ui.api.endpoint"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_UI_API_AUTH_TYPE = "apiman-manager-ui.api.authentication.type"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_UI_API_BASIC_AUTH_USER = "apiman-manager-ui.api.authentication.basic.user"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_UI_API_BASIC_AUTH_PASS = "apiman-manager-ui.api.authentication.basic.password"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_UI_API_AUTH_TOKEN_GENERATOR = "apiman-manager-ui.api.authentication.token.generator"; //$NON-NLS-1$

    public static final String APIMAN_MANAGER_UI_LOGOUT_URL = "apiman-manager-ui.logout-url"; //$NON-NLS-1$

    private static Configuration config;
    static {
        config = ConfigFactory.createConfig();
    }

    /**
     * Constructor.
     */
    public UIConfig() {
    }

    /**
     * @see io.apiman.manager.ui.server.IUIConfig#getManagementApiEndpoint()
     */
    @Override
    public String getManagementApiEndpoint() {
        return config.getString(UIConfig.APIMAN_MANAGER_UI_API_ENDPOINT);
    }

    /**
     * @see io.apiman.manager.ui.server.IUIConfig#getManagementApiAuthType()
     */
    @Override
    public ApiAuthType getManagementApiAuthType() {
        String at = config.getString(UIConfig.APIMAN_MANAGER_UI_API_AUTH_TYPE);
        try {
            return ApiAuthType.valueOf(at);
        } catch (Exception e) {
            throw new RuntimeException("Invalid API authentication type: " + at); //$NON-NLS-1$
        }
    }

    /**
     * @see io.apiman.manager.ui.server.IUIConfig#getLogoutUrl()
     */
    @Override
    public String getLogoutUrl() {
        return config.getString(UIConfig.APIMAN_MANAGER_UI_LOGOUT_URL, "/apimanui/logout"); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.ui.server.IUIConfig#getManagementApiAuthUsername()
     */
    @Override
    public String getManagementApiAuthUsername() {
        return config.getString(UIConfig.APIMAN_MANAGER_UI_API_BASIC_AUTH_USER);
    }

    /**
     * @see io.apiman.manager.ui.server.IUIConfig#getManagementApiAuthPassword()
     */
    @Override
    public String getManagementApiAuthPassword() {
        return config.getString(UIConfig.APIMAN_MANAGER_UI_API_BASIC_AUTH_PASS);
    }

    /**
     * @see io.apiman.manager.ui.server.IUIConfig#getManagementApiAuthTokenGenerator()
     */
    @Override
    public String getManagementApiAuthTokenGenerator() {
        return config.getString(UIConfig.APIMAN_MANAGER_UI_API_AUTH_TOKEN_GENERATOR);
    }

    /**
     * @return the configuration
     */
    public Configuration getConfig() {
        return config;
    }
}
