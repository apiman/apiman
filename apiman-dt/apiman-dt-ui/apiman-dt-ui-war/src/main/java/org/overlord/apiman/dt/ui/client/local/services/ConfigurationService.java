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
import org.overlord.apiman.dt.ui.client.shared.beans.ApiAuthType;
import org.overlord.apiman.dt.ui.client.shared.beans.BearerTokenCredentialsBean;
import org.overlord.apiman.dt.ui.client.shared.beans.ConfigurationBean;
import org.overlord.apiman.dt.ui.server.servlets.ConfigurationServlet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Timer;

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
        
        ApiAuthType apiAuthType = configuration.getApi().getAuth().getType();
        if (apiAuthType == ApiAuthType.bearerToken || apiAuthType == ApiAuthType.samlBearerToken || apiAuthType == ApiAuthType.authToken) {
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
            @Override
            public void run() {
                GWT.log("Refreshing auth token."); //$NON-NLS-1$

                final String url = GWT.getHostPageBaseURL() + "rest/tokenRefresh"; //$NON-NLS-1$
                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, URL.encode(url));
                try {
                    builder.sendRequest(null, new RequestCallback() {
                        @Override
                        public void onResponseReceived(Request request, Response response) {
                            if (response.getStatusCode() != 200) {
                                GWT.log("[001] Authentication token refresh failure: " + url); //$NON-NLS-1$
                            } else {
                                BearerTokenCredentialsBean bean = new BearerTokenCredentialsBean();
                                JSONObject root = JSONParser.parseStrict(response.getText()).isObject();
                                bean.setToken(root.get("token").isString().stringValue()); //$NON-NLS-1$
                                bean.setRefreshPeriod((int) root.get("refreshPeriod").isNumber().doubleValue()); //$NON-NLS-1$
                                configuration.getApi().getAuth().setBearerToken(bean);
                            }
                            startTokenRefreshTimer();
                        }
                        @Override
                        public void onError(Request request, Throwable exception) {
                            GWT.log("[002] Authentication token refresh failure: " + url); //$NON-NLS-1$
                        }
                    });
                } catch (RequestException e) {
                    GWT.log("Authentication token refresh failed!"); //$NON-NLS-1$
                }
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
