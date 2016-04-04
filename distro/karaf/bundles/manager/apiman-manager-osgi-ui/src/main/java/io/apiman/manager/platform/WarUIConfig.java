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
package io.apiman.manager.platform;

import io.apiman.manager.ui.server.IUIConfig;
import io.apiman.manager.ui.server.beans.ApiAuthType;
import io.apiman.manager.ui.server.impl.UIConfig;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Global access to configuration information.
 *
 */
public class WarUIConfig implements IUIConfig {

    public static final String APIMAN_MANAGER_UI_API_ENDPOINT = "apiman-manager-ui.api.endpoint"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_UI_API_AUTH_TYPE = "apiman-manager-ui.api.authentication.type"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_UI_API_BASIC_AUTH_USER = "apiman-manager-ui.api.authentication.basic.user"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_UI_API_BASIC_AUTH_PASS = "apiman-manager-ui.api.authentication.basic.password"; //$NON-NLS-1$
    public static final String APIMAN_MANAGER_UI_API_AUTH_TOKEN_GENERATOR = "apiman-manager-ui.api.authentication.token.generator"; //$NON-NLS-1$

    public static final String APIMAN_MANAGER_UI_ENABLE_METRICS = "apiman-manager-ui.metrics.enable"; //$NON-NLS-1$

    public static final String APIMAN_MANAGER_UI_LOGOUT_URL = "apiman-manager-ui.logout-url"; //$NON-NLS-1$

    private static Configuration config;

    public static void setConfig(Dictionary dict) {
        config = new BaseConfiguration();
        Map<String, Object> map = new HashMap<String, Object>(dict.size());
        Enumeration<String> keys = dict.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            config.addProperty(key, dict.get(key));
        }
    }

    /**
     * Constructor.
     */
    public WarUIConfig() {
    }

    /**
     * @see IUIConfig#isMetricsEnabled()
     */
    @Override
    public boolean isMetricsEnabled() {
        return config.getBoolean(WarUIConfig.APIMAN_MANAGER_UI_ENABLE_METRICS, true);
    }

    /**
     * @see IUIConfig#getManagementApiEndpoint()
     */
    @Override
    public String getManagementApiEndpoint() {
        return config.getString(WarUIConfig.APIMAN_MANAGER_UI_API_ENDPOINT);
    }

    /**
     * @see IUIConfig#getManagementApiAuthType()
     */
    @Override
    public ApiAuthType getManagementApiAuthType() {
        String at = config.getString(WarUIConfig.APIMAN_MANAGER_UI_API_AUTH_TYPE);
        try {
            return ApiAuthType.valueOf(at);
        } catch (Exception e) {
            throw new RuntimeException("Invalid API authentication type: " + at); //$NON-NLS-1$
        }
    }

    /**
     * @see IUIConfig#getLogoutUrl()
     */
    @Override
    public String getLogoutUrl() {
        return config.getString(WarUIConfig.APIMAN_MANAGER_UI_LOGOUT_URL, "/apimanui/logout"); //$NON-NLS-1$
    }

    /**
     * @see io.apiman.manager.ui.server.IUIConfig#getPlatform()
     */
    @Override
    public String getPlatform() {
        return config.getString(UIConfig.APIMAN_MANAGER_UI_PLATFORM);
    }

    /**
     * @see IUIConfig#getManagementApiAuthUsername()
     */
    @Override
    public String getManagementApiAuthUsername() {
        return config.getString(WarUIConfig.APIMAN_MANAGER_UI_API_BASIC_AUTH_USER);
    }

    /**
     * @see IUIConfig#getManagementApiAuthPassword()
     */
    @Override
    public String getManagementApiAuthPassword() {
        return config.getString(WarUIConfig.APIMAN_MANAGER_UI_API_BASIC_AUTH_PASS);
    }

    /**
     * @see IUIConfig#getManagementApiAuthTokenGenerator()
     */
    @Override
    public String getManagementApiAuthTokenGenerator() {
        return config.getString(WarUIConfig.APIMAN_MANAGER_UI_API_AUTH_TOKEN_GENERATOR);
    }

    /**
     * @return the configuration
     */
    public Configuration getConfig() {
        return config;
    }
}
