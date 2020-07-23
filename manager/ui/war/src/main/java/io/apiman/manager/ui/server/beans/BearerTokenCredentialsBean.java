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
 * The credentials to use for bearer token auth.
 *
 * @author eric.wittmann@redhat.com
 */
public class BearerTokenCredentialsBean implements Serializable {

    private static final long serialVersionUID = -876646690486553629L;
    
    private String token;
    private long refreshPeriod; // in seconds

    /**
     * Constructor.
     */
    public BearerTokenCredentialsBean() {
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the refreshPeriod
     */
    public long getRefreshPeriod() {
        return refreshPeriod;
    }

    /**
     * @param refreshPeriod the refreshPeriod to set
     */
    public void setRefreshPeriod(long refreshPeriod) {
        this.refreshPeriod = refreshPeriod;
    }
    
}
