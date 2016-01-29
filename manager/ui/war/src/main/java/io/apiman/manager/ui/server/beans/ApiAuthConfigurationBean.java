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
 * Models configuration information related to Apiman DT API authentication.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiAuthConfigurationBean implements Serializable {
    
    private static final long serialVersionUID = -6821127066893153296L;
    
    private ApiAuthType type;
    private BasicAuthCredentialsBean basic;
    private BearerTokenCredentialsBean bearerToken;

    /**
     * Constructor.
     */
    public ApiAuthConfigurationBean() {
    }

    /**
     * @return the type
     */
    public ApiAuthType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(ApiAuthType type) {
        this.type = type;
    }

    /**
     * @return the basic
     */
    public BasicAuthCredentialsBean getBasic() {
        return basic;
    }

    /**
     * @param basic the basic to set
     */
    public void setBasic(BasicAuthCredentialsBean basic) {
        this.basic = basic;
    }

    /**
     * @return the bearerToken
     */
    public BearerTokenCredentialsBean getBearerToken() {
        return bearerToken;
    }

    /**
     * @param bearerToken the bearerToken to set
     */
    public void setBearerToken(BearerTokenCredentialsBean bearerToken) {
        this.bearerToken = bearerToken;
    }

}
