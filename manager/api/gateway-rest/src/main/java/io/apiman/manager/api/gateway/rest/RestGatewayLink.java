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
package io.apiman.manager.api.gateway.rest;

import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.manager.api.config.IConfig;
import io.apiman.manager.api.gateway.IGatewayLink;
import io.apiman.manager.api.gateway.rest.i18n.Messages;

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
     * Checks that the gateway is up.
     */
    private boolean isGatewayUp() {
        SystemStatus status = getClient().getStatus();
        return status.isUp();
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#publishService(io.apiman.gateway.engine.beans.Service)
     */
    @Override
    public void publishService(Service service) throws PublishingException {
        if (!isGatewayUp()) {
            throw new PublishingException(Messages.i18n.format("RestGatewayLink.GatewayNotRunning")); //$NON-NLS-1$
        }
        getClient().publish(service);
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#retireService(io.apiman.gateway.engine.beans.Service)
     */
    @Override
    public void retireService(Service service) throws PublishingException {
        if (!isGatewayUp()) {
            throw new PublishingException(Messages.i18n.format("RestGatewayLink.GatewayNotRunning")); //$NON-NLS-1$
        }
        getClient().retire(service.getOrganizationId(), service.getServiceId(), service.getVersion());
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#registerApplication(io.apiman.gateway.engine.beans.Application)
     */
    @Override
    public void registerApplication(Application application) throws RegistrationException {
        if (!isGatewayUp()) {
            throw new RegistrationException(Messages.i18n.format("RestGatewayLink.GatewayNotRunning")); //$NON-NLS-1$
        }
        getClient().register(application);
    }

    /**
     * @see io.apiman.manager.api.gateway.IGatewayLink#unregisterApplication(io.apiman.gateway.engine.beans.Application)
     */
    @Override
    public void unregisterApplication(Application application) throws RegistrationException {
        if (!isGatewayUp()) {
            throw new RegistrationException(Messages.i18n.format("RestGatewayLink.GatewayNotRunning")); //$NON-NLS-1$
        }
        getClient().unregister(application.getOrganizationId(), application.getApplicationId(), application.getVersion());
    }

    /**
     * Configures BASIC authentication for the request.
     * @param request
     */
    protected void configureBasicAuth(HttpRequest request) {
        try {
            String username = getConfig().getGatewayBasicAuthUsername();
            String password = getConfig().getGatewayBasicAuthPassword();
            String up = username + ":" + password; //$NON-NLS-1$
            String base64 = new String(Base64.encodeBase64(up.getBytes("UTF-8"))); //$NON-NLS-1$
            String authHeader = "Basic " + base64; //$NON-NLS-1$
            request.setHeader("Authorization", authHeader); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the config
     */
    public IConfig getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(IConfig config) {
        this.config = config;
    }

    /**
     * @return the gateway client
     */
    protected GatewayClient getClient() {
        if (gatewayClient == null) {
            String gatewayEndpoint = getConfig().getGatewayRestEndpoint();
            gatewayClient = new GatewayClient(gatewayEndpoint, httpClient);
        }
        return gatewayClient;
    }

}
