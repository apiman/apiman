/*
 * Copyright 2015 JBoss Inc
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
package io.apiman.plugins.config_policy;

import java.io.Serializable;

/**
 * A simple javabean used for configuration.  The configuration is stored
 * as JSON data, and is mapped to this bean using Jackson.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConfigBean implements Serializable {

    private static final long serialVersionUID = 683486516910591477L;
    
    private String requestHeader;
    private String responseHeader;
    
    /**
     * Constructor.
     */
    public ConfigBean() {
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

    /**
     * @return the responseHeader
     */
    public String getResponseHeader() {
        return responseHeader;
    }

    /**
     * @param responseHeader the responseHeader to set
     */
    public void setResponseHeader(String responseHeader) {
        this.responseHeader = responseHeader;
    }

}
