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
package org.overlord.apiman.dt.test.server;

import javax.enterprise.context.ApplicationScoped;

import org.overlord.apiman.dt.api.config.IConfig;
import org.overlord.apiman.dt.api.war.config.Config;

/**
 * A simple test config.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class TestConfig implements IConfig {
    
    /**
     * Constructor.
     */
    public TestConfig() {
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayRestEndpoint()
     */
    @Override
    public String getGatewayRestEndpoint() {
        return System.getProperty(Config.APIMAN_DT_API_GATEWAY_REST_ENDPOINT);
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayAuthenticationType()
     */
    @Override
    public String getGatewayAuthenticationType() {
        return System.getProperty(Config.APIMAN_DT_API_GATEWAY_AUTH_TYPE);
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayBasicAuthUsername()
     */
    @Override
    public String getGatewayBasicAuthUsername() {
        return System.getProperty(Config.APIMAN_DT_API_GATEWAY_BASIC_AUTH_USER);
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayBasicAuthPassword()
     */
    @Override
    public String getGatewayBasicAuthPassword() {
        return System.getProperty(Config.APIMAN_DT_API_GATEWAY_BASIC_AUTH_PASS);
    }

}
