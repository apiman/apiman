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
package org.overlord.apiman.dt.ui.client.local.services;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.InitBallot;
import org.overlord.apiman.dt.ui.client.shared.beans.ConfigurationBean;
import org.overlord.apiman.dt.ui.server.servlets.ConfigurationServlet;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;

/**
 * An application configuration service. This service is responsible for getting
 * and maintaining app configuration, such as current user, application
 * meta-data, etc. Works with the {@link ConfigurationServlet} to determine
 * initial application config state.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ConfigurationService {

    @Inject
    InitBallot<ConfigurationService> ballot;
    
    private ConfigurationBean configuration;

    /**
     * Constructor.
     */
    public ConfigurationService() {
    }

    @PostConstruct
    private void postConstruct() {
        RestClient.setJacksonMarshallingActive(true);

        JavaScriptObject configData = getInitialConfigurationData();
        if (configData == null) {
            throw new RuntimeException("Could not find initial configuration!"); //$NON-NLS-1$
        }
        String data = new JSONObject(configData).toString();
        configuration = MarshallingWrapper.fromJSON(data, ConfigurationBean.class);
        
        RestClient.setApplicationRoot(configuration.getApi().getEndpoint());

        ballot.voteForInit();
    }
    
    /**
     * @return the current configuration
     */
    public ConfigurationBean getCurrentConfig() {
        return configuration;
    }

    /**
     * Gets the initial app configuration data.
     */
    private static native JavaScriptObject getInitialConfigurationData() /*-{
		if (!$wnd.APIMAN_CONFIG_DATA) {
			return null;
		} else {
			return $wnd.APIMAN_CONFIG_DATA;
		}
    }-*/;
}
