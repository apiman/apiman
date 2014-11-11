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
package org.overlord.apiman.dt.ui.server.fuse;

import org.overlord.apiman.dt.ui.client.shared.beans.ApiAuthType;
import org.overlord.apiman.dt.ui.server.IUIConfig;

/**
 * A fuse implementation of the UI config.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseUIConfig implements IUIConfig {
    
    private String apiEndpoint;
    private ApiAuthType apiAuthType;
    private String apiAuthUsername;
    private String apiAuthPassword;
    private String apiAuthTokenGenerator;
    private String logoutUrl;
    private String gatewayUrl;
    
    /**
     * Constructor.
     */
    public FuseUIConfig() {
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getManagementApiEndpoint()
     */
    @Override
    public String getManagementApiEndpoint() {
        return apiEndpoint;
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getManagementApiAuthType()
     */
    @Override
    public ApiAuthType getManagementApiAuthType() {
        return apiAuthType;
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getGatewayUrl()
     */
    @Override
    public String getGatewayUrl() {
        return gatewayUrl;
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getManagementApiAuthUsername()
     */
    @Override
    public String getManagementApiAuthUsername() {
        return apiAuthUsername;
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getManagementApiAuthPassword()
     */
    @Override
    public String getManagementApiAuthPassword() {
        return apiAuthPassword;
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getManagementApiAuthTokenGenerator()
     */
    @Override
    public String getManagementApiAuthTokenGenerator() {
        return apiAuthTokenGenerator;
    }

    /**
     * @return the apiEndpoint
     */
    public String getApiEndpoint() {
        return apiEndpoint;
    }

    /**
     * @param apiEndpoint the apiEndpoint to set
     */
    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    /**
     * @return the apiAuthType
     */
    public String getApiAuthType() {
        return apiAuthType == null ? null : apiAuthType.toString();
    }

    /**
     * @param apiAuthType the apiAuthType to set
     */
    public void setApiAuthType(String apiAuthType) {
        this.apiAuthType = apiAuthType == null ? null : ApiAuthType.valueOf(apiAuthType);
    }

    /**
     * @return the apiAuthUsername
     */
    public String getApiAuthUsername() {
        return apiAuthUsername;
    }

    /**
     * @param apiAuthUsername the apiAuthUsername to set
     */
    public void setApiAuthUsername(String apiAuthUsername) {
        this.apiAuthUsername = apiAuthUsername;
    }

    /**
     * @return the apiAuthPassword
     */
    public String getApiAuthPassword() {
        return apiAuthPassword;
    }

    /**
     * @param apiAuthPassword the apiAuthPassword to set
     */
    public void setApiAuthPassword(String apiAuthPassword) {
        this.apiAuthPassword = apiAuthPassword;
    }

    /**
     * @return the apiAuthTokenGenerator
     */
    public String getApiAuthTokenGenerator() {
        return apiAuthTokenGenerator;
    }

    /**
     * @param apiAuthTokenGenerator the apiAuthTokenGenerator to set
     */
    public void setApiAuthTokenGenerator(String apiAuthTokenGenerator) {
        this.apiAuthTokenGenerator = apiAuthTokenGenerator;
    }

    /**
     * @param gatewayUrl the gatewayUrl to set
     */
    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getLogoutUrl()
     */
    @Override
    public String getLogoutUrl() {
        return logoutUrl;
    }
    
    /**
     * @param logoutUrl
     */
    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

}
