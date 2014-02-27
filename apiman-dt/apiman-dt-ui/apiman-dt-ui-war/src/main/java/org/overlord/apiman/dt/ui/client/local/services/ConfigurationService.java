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

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.enterprise.client.jaxrs.MarshallingWrapper;
import org.jboss.errai.enterprise.client.jaxrs.api.RestClient;
import org.jboss.errai.ioc.client.api.InitBallot;
import org.overlord.apiman.dt.ui.client.shared.beans.ApiAuthType;
import org.overlord.apiman.dt.ui.client.shared.beans.BearerTokenCredentialsBean;
import org.overlord.apiman.dt.ui.client.shared.beans.ConfigurationBean;
import org.overlord.apiman.dt.ui.client.shared.services.ITokenRefreshService;
import org.overlord.apiman.dt.ui.server.servlets.ConfigurationServlet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

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
    
    @Inject
    Caller<ITokenRefreshService> tokenRefresh;
    
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
        
        ApiAuthType apiAuthType = configuration.getApi().getAuth().getType();
        if (apiAuthType == ApiAuthType.bearerToken || apiAuthType == ApiAuthType.samlBearerToken) {
            startTokenRefreshTimer();
        }

        ballot.voteForInit();
    }

    /**
     * Starts a timer that will refresh the configuration's bearer token
     * periodically.
     */
    private void startTokenRefreshTimer() {
        Timer timer = new Timer() {
            @SuppressWarnings("rawtypes")
            @Override
            public void run() {
                GWT.log("Refreshing auth token."); //$NON-NLS-1$
                tokenRefresh.call(new RemoteCallback<BearerTokenCredentialsBean>() {
                    @Override
                    public void callback(BearerTokenCredentialsBean response) {
                        configuration.getApi().getAuth().setBearerToken(response);
                        startTokenRefreshTimer();
                    }
                }, new ErrorCallback() {
                    @Override
                    public boolean error(Object message, Throwable throwable) {
                        // TODO do something more interesting with this error
                        Window.alert("Authentication token refresh failed!"); //$NON-NLS-1$
                        return true;
                    }
                }).refreshToken();
            }
        };
        timer.schedule(configuration.getApi().getAuth().getBearerToken().getRefreshPeriod() * 1000);
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
