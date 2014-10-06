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
package org.overlord.apiman.dt.api.fuse6.config;

import org.overlord.apiman.dt.api.config.IConfig;


/**
 * A fuse specific implementation of the apiman design time layer's
 * config interface.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseConfig implements IConfig {

    private String gatewayRestEndpoint;
    private String gatewayAuthenticationType;
    private String gatewayBasicAuthUsername;
    private String gatewayBasicAuthPassword;

    /**
     * Constructor.
     */
    public FuseConfig() {
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayRestEndpoint()
     */
    @Override
    public String getGatewayRestEndpoint() {
        return gatewayRestEndpoint;
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayAuthenticationType()
     */
    @Override
    public String getGatewayAuthenticationType() {
        return gatewayAuthenticationType;
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayBasicAuthUsername()
     */
    @Override
    public String getGatewayBasicAuthUsername() {
        return gatewayBasicAuthUsername;
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayBasicAuthPassword()
     */
    @Override
    public String getGatewayBasicAuthPassword() {
        return gatewayBasicAuthPassword;
    }

    /**
     * @param gatewayRestEndpoint the gatewayRestEndpoint to set
     */
    public void setGatewayRestEndpoint(String gatewayRestEndpoint) {
        this.gatewayRestEndpoint = gatewayRestEndpoint;
    }

    /**
     * @param gatewayAuthenticationType the gatewayAuthenticationType to set
     */
    public void setGatewayAuthenticationType(String gatewayAuthenticationType) {
        this.gatewayAuthenticationType = gatewayAuthenticationType;
    }

    /**
     * @param gatewayBasicAuthUsername the gatewayBasicAuthUsername to set
     */
    public void setGatewayBasicAuthUsername(String gatewayBasicAuthUsername) {
        this.gatewayBasicAuthUsername = gatewayBasicAuthUsername;
    }

    /**
     * @param gatewayBasicAuthPassword the gatewayBasicAuthPassword to set
     */
    public void setGatewayBasicAuthPassword(String gatewayBasicAuthPassword) {
        this.gatewayBasicAuthPassword = gatewayBasicAuthPassword;
    }

}
