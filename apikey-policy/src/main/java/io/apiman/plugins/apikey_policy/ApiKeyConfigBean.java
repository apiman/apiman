/*
 * Copyright 2016 JBoss Inc
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
package io.apiman.plugins.apikey_policy;

import java.io.Serializable;

/**
 * A simple javabean used for configuration of the API Key policy.
 *
 * @author eric.wittmann@redhat.com
 */
public class ApiKeyConfigBean implements Serializable {

    private static final long serialVersionUID = -1473728342121083306L;
    
    private String requestHeader;
    
    /**
     * Constructor.
     */
    public ApiKeyConfigBean() {
    }

    /**
     * @return the requestHeader
     */
    public String getRequestHeader() {
        return requestHeader;
    }

    /**
     * @param requestHeader the requestHeader to set
     */
    public void setRequestHeader(String requestHeader) {
        this.requestHeader = requestHeader;
    }

}
