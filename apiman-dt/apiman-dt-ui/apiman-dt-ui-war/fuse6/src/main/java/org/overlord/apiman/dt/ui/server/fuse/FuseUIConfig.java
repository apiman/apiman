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
import org.overlord.apiman.dt.ui.server.auth.AuthTokenGenerator;

/**
 * A fuse implementation of the UI config.
 *
 * @author eric.wittmann@redhat.com
 */
public class FuseUIConfig implements IUIConfig {
    
    /**
     * Constructor.
     */
    public FuseUIConfig() {
    }
    
    // TODO remove hard-coded values, get config from appropriate fuse-y place

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getManagementApiEndpoint()
     */
    @Override
    public String getManagementApiEndpoint() {
        return "http://localhost:8181/cxf/apiman-dt-api"; //$NON-NLS-1$
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getManagementApiAuthType()
     */
    @Override
    public ApiAuthType getManagementApiAuthType() {
        return ApiAuthType.authToken;
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getGatewayUrl()
     */
    @Override
    public String getGatewayUrl() {
        return "http://localhost:8181/gateway"; //$NON-NLS-1$
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getManagementApiAuthUsername()
     */
    @Override
    public String getManagementApiAuthUsername() {
        return null;
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getManagementApiAuthPassword()
     */
    @Override
    public String getManagementApiAuthPassword() {
        return null;
    }

    /**
     * @see org.overlord.apiman.dt.ui.server.IUIConfig#getManagementApiAuthTokenGenerator()
     */
    @Override
    public String getManagementApiAuthTokenGenerator() {
        return AuthTokenGenerator.class.getName();
    }

}
