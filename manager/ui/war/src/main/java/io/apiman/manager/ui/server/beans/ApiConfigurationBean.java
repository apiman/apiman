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
package io.apiman.manager.ui.server.beans;

import java.io.Serializable;

/**
 * Models configuration information about the Apiman DT API, including
 * endpoint and auth information.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiConfigurationBean implements Serializable {
    
    private static final long serialVersionUID = -6012802307441423455L;
    
    private String endpoint;
    private ApiAuthConfigurationBean auth;
    
    /**
     * Constructor.
     */
    public ApiConfigurationBean() {
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return the auth
     */
    public ApiAuthConfigurationBean getAuth() {
        return auth;
    }

    /**
     * @param auth the auth to set
     */
    public void setAuth(ApiAuthConfigurationBean auth) {
        this.auth = auth;
    }

}
