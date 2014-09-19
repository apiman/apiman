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
        throw new RuntimeException("FuseConfig: Not yet implemented."); //$NON-NLS-1$
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayAuthenticationType()
     */
    @Override
    public String getGatewayAuthenticationType() {
        throw new RuntimeException("FuseConfig: Not yet implemented."); //$NON-NLS-1$
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayBasicAuthUsername()
     */
    @Override
    public String getGatewayBasicAuthUsername() {
        throw new RuntimeException("FuseConfig: Not yet implemented."); //$NON-NLS-1$
    }

    /**
     * @see org.overlord.apiman.dt.api.config.IConfig#getGatewayBasicAuthPassword()
     */
    @Override
    public String getGatewayBasicAuthPassword() {
        throw new RuntimeException("FuseConfig: Not yet implemented."); //$NON-NLS-1$
    }

}
