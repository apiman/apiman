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

import org.apache.commons.configuration.Configuration;
import org.overlord.commons.config.ConfigurationFactory;

/**
 * Global access to configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
public class UIConfig {

    public static final String APIMAN_DT_UI_CONFIG_FILE_NAME     = "apiman-dt-ui.config.file.name"; //$NON-NLS-1$
    public static final String APIMAN_DT_UI_CONFIG_FILE_REFRESH  = "apiman-dt-ui.config.file.refresh"; //$NON-NLS-1$

    public static final String APIMAN_DT_UI_API_ENDPOINT = "apiman-dt-ui.dt-api.endpoint"; //$NON-NLS-1$
    public static final String APIMAN_DT_UI_API_AUTH_TYPE = "apiman-dt-ui.dt-api.authentication.type"; //$NON-NLS-1$
    public static final String APIMAN_DT_UI_API_BASIC_AUTH_USER = "apiman-dt-ui.dt-api.authentication.basic.user"; //$NON-NLS-1$
    public static final String APIMAN_DT_UI_API_BASIC_AUTH_PASS = "apiman-dt-ui.dt-api.authentication.basic.password"; //$NON-NLS-1$
    public static final String APIMAN_DT_UI_API_AUTH_TOKEN_GENERATOR = "apiman-dt-ui.dt-api.authentication.token.generator"; //$NON-NLS-1$
    
    public static final String APIMAN_DT_UI_GATEWAY_URL = "apiman-dt-ui.gateway.base-url"; //$NON-NLS-1$

    public static Configuration config;
    static {
        String configFile = System.getProperty(APIMAN_DT_UI_CONFIG_FILE_NAME);
        String refreshDelayStr = System.getProperty(APIMAN_DT_UI_CONFIG_FILE_REFRESH);
        Long refreshDelay = 5000l;
        if (refreshDelayStr != null) {
            refreshDelay = new Long(refreshDelayStr);
        }

        config = ConfigurationFactory.createConfig(
                configFile,
                "apiman.properties", //$NON-NLS-1$
                refreshDelay,
                null,
                UIConfig.class);
    }

    /**
     * Constructor.
     */
    public UIConfig() {
    }

    /**
     * @return the configuration
     */
    public Configuration getConfig() {
        return config;
    }
}
