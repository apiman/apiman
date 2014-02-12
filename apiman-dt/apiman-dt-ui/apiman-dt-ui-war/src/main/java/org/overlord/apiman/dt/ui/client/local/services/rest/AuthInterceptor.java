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
package org.overlord.apiman.dt.ui.client.local.services.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.interceptor.InterceptsRemoteCall;
import org.jboss.errai.enterprise.client.jaxrs.api.interceptor.RestCallContext;
import org.jboss.errai.enterprise.client.jaxrs.api.interceptor.RestClientInterceptor;
import org.overlord.apiman.dt.api.rest.contract.IOrganizationResource;
import org.overlord.apiman.dt.api.rest.contract.ISystemResource;
import org.overlord.apiman.dt.api.rest.contract.IUserResource;
import org.overlord.apiman.dt.ui.client.local.services.ConfigurationService;
import org.overlord.apiman.dt.ui.client.local.util.Base64Util;
import org.overlord.apiman.dt.ui.client.shared.beans.ApiAuthConfigurationBean;
import org.overlord.apiman.dt.ui.client.shared.beans.ApiAuthType;

/**
 * A REST interceptor that adds authentication to the REST invokation
 * pipeline.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
@InterceptsRemoteCall({ ISystemResource.class, IUserResource.class, IOrganizationResource.class })
public class AuthInterceptor implements RestClientInterceptor {
    
    @Inject
    ConfigurationService config;
    
    /**
     * Constructor.
     */
    public AuthInterceptor() {
//        IOC.getAsyncBeanManager().lookupBean(ConfigurationService.class).getInstance(new CreationalCallback<ConfigurationService>() {
//            @Override
//            public void callback(ConfigurationService beanInstance) {
//                config = beanInstance;
//            }
//        });
    }

    /**
     * @see org.jboss.errai.common.client.api.interceptor.RemoteCallInterceptor#aroundInvoke(org.jboss.errai.common.client.api.interceptor.RemoteCallContext)
     */
    @Override
    public void aroundInvoke(RestCallContext context) {
        if (config == null) {
            throw new RuntimeException("Configuration service not available.");
        }
        ApiAuthConfigurationBean auth = config.getCurrentConfig().getApi().getAuth();
        if (auth.getType() == ApiAuthType.basic) {
            doBasicAuth(context, auth);
        } else if (auth.getType() == ApiAuthType.bearerToken) {
            doBearerTokenAuth(context, auth);
        }
        context.proceed();
    }

    /**
     * Implementation of basic auth.
     * @param context
     * @param auth
     */
    private void doBasicAuth(RestCallContext context, ApiAuthConfigurationBean auth) {
        String encoded = Base64Util.b64encode(auth.getBasic().getUsername() + ":" + auth.getBasic().getPassword());
        context.getRequestBuilder().setIncludeCredentials(true);
        context.getRequestBuilder().setHeader("Authorization", "Basic " + encoded);
    }

    /**
     * Implementation of bearer token auth.
     * @param context
     * @param auth
     */
    private void doBearerTokenAuth(RestCallContext context, ApiAuthConfigurationBean auth) {
        // TODO implement bearer token authentication
        throw new RuntimeException("Not yet implemented (Bearer Token Auth).");
    }

}
