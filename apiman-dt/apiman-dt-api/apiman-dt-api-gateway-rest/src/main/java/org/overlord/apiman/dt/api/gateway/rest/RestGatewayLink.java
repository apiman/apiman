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
package org.overlord.apiman.dt.api.gateway.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.overlord.apiman.dt.api.config.IConfig;
import org.overlord.apiman.dt.api.gateway.IGatewayLink;
import org.overlord.apiman.rt.engine.beans.Application;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.SystemStatus;
import org.overlord.apiman.rt.engine.beans.exceptions.PublishingException;
import org.overlord.apiman.rt.engine.beans.exceptions.RegistrationException;

/**
 * An implementation of a Gateway Link that uses the Gateway's simple REST 
 * API to publish Services.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class RestGatewayLink implements IGatewayLink {
    
    @Inject
    private IConfig config;
    
    private CloseableHttpClient httpClient;
    private GatewayClient gatewayClient;

    /**
     * Constructor.
     */
    public RestGatewayLink() {
    }

    /**
     * Called after the bean is created.
     */
    @PostConstruct
    public void postConstruct() {
        httpClient = HttpClientBuilder.create().addInterceptorFirst(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                configureBasicAuth(request);
            }
        }).build();
        
        String gatewayEndpoint = config.getGatewayRestEndpoint();
        gatewayClient = new GatewayClient(gatewayEndpoint, httpClient);
        
        SystemStatus status = gatewayClient.getStatus();
        if (!status.isUp()) {
            throw new RuntimeException("Gateway is not running!"); //$NON-NLS-1$
        }
    }

    /**
     * Called on destruction of the bean.
     */
    @PreDestroy
    public void preDestroy() {
        try {
            httpClient.close();
        } catch (IOException e) {
            // TODO log the error?
        }
    }

    /**
     * @see org.overlord.apiman.dt.api.gateway.IGatewayLink#publishService(org.overlord.apiman.rt.engine.beans.Service)
     */
    @Override
    public void publishService(Service service) throws PublishingException {
        gatewayClient.publish(service);
    }

    /**
     * @see org.overlord.apiman.dt.api.gateway.IGatewayLink#retireService(org.overlord.apiman.rt.engine.beans.Service)
     */
    @Override
    public void retireService(Service service) throws PublishingException {
        gatewayClient.retire(service.getOrganizationId(), service.getServiceId(), service.getVersion());
    }

    /**
     * @see org.overlord.apiman.dt.api.gateway.IGatewayLink#registerApplication(org.overlord.apiman.rt.engine.beans.Application)
     */
    @Override
    public void registerApplication(Application application) throws RegistrationException {
        gatewayClient.register(application);
    }

    /**
     * @see org.overlord.apiman.dt.api.gateway.IGatewayLink#unregisterApplication(org.overlord.apiman.rt.engine.beans.Application)
     */
    @Override
    public void unregisterApplication(Application application) throws RegistrationException {
        gatewayClient.unregister(application.getOrganizationId(), application.getApplicationId(), application.getVersion());
    }

    /**
     * Configures BASIC authentication for the request.
     * @param request
     */
    protected void configureBasicAuth(HttpRequest request) {
        try {
            String username = config.getGatewayBasicAuthUsername();
            String password = config.getGatewayBasicAuthPassword();
            String up = username + ":" + password; //$NON-NLS-1$
            String base64 = new String(Base64.encodeBase64(up.getBytes("UTF-8"))); //$NON-NLS-1$
            String authHeader = "Basic " + base64; //$NON-NLS-1$
            request.setHeader("Authorization", authHeader); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
