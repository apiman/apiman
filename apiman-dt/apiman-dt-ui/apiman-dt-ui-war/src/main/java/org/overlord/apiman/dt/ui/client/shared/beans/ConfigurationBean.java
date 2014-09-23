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
package org.overlord.apiman.dt.ui.client.shared.beans;

import java.io.Serializable;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.overlord.apiman.dt.ui.server.servlets.ConfigurationServlet;

/**
 * Encapsulates initial app configuration data sent from the server (via the
 * {@link ConfigurationServlet} servlet) to the client (via the
 * {@link org.overlord.apiman.dt.ui.client.local.services.ConfigurationService}
 * service).
 * 
 * @author eric.wittmann@redhat.com
 */
@Portable
public class ConfigurationBean implements Serializable {

    private static final long serialVersionUID = -6342457151615532102L;

    private AppConfigurationBean apiman;
    private UserConfigurationBean user;
    private ApiConfigurationBean api;

    /**
     * Constructor.
     */
    public ConfigurationBean() {
    }

    /**
     * @return the apiman
     */
    public AppConfigurationBean getApiman() {
        return apiman;
    }

    /**
     * @param apiman
     *            the apiman to set
     */
    public void setApiman(AppConfigurationBean apiman) {
        this.apiman = apiman;
    }

    /**
     * @return the user
     */
    public UserConfigurationBean getUser() {
        return user;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(UserConfigurationBean user) {
        this.user = user;
    }

    /**
     * @return the api
     */
    public ApiConfigurationBean getApi() {
        return api;
    }

    /**
     * @param api
     *            the api to set
     */
    public void setApi(ApiConfigurationBean api) {
        this.api = api;
    }
}
