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
package io.apiman.manager.ui.client.shared.beans;

import java.io.Serializable;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Models some meta-information about the application itself, including version 
 * information.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class AppConfigurationBean implements Serializable {
    
    private static final long serialVersionUID = 643975919834773546L;
    
    private String version;
    private String builtOn;
    private String logoutUrl;
    private String gatewayBaseUrl;

    /**
     * Constructor.
     */
    public AppConfigurationBean() {
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return the builtOn
     */
    public String getBuiltOn() {
        return builtOn;
    }

    /**
     * @param builtOn the builtOn to set
     */
    public void setBuiltOn(String builtOn) {
        this.builtOn = builtOn;
    }

    /**
     * @return the gatewayBaseUrl
     */
    public String getGatewayBaseUrl() {
        return gatewayBaseUrl;
    }

    /**
     * @param gatewayBaseUrl the gatewayBaseUrl to set
     */
    public void setGatewayBaseUrl(String gatewayBaseUrl) {
        this.gatewayBaseUrl = gatewayBaseUrl;
    }

    /**
     * @return the logoutUrl
     */
    public String getLogoutUrl() {
        return logoutUrl;
    }

    /**
     * @param logoutUrl the logoutUrl to set
     */
    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }
}
