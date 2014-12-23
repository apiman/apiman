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

import io.apiman.gateway.api.rest.contract.exceptions.NotAuthorizedException;
import io.apiman.gateway.engine.beans.Application;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceEndpoint;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.manager.api.gateway.i18n.Messages;

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * A REST client for accessing the Gateway API.
 * 
 * @author eric.wittmann@redhat.com
 */
public class GatewayClient /*implements ISystemResource, IServiceResource, IApplicationResource*/ {
    
    private static final String SYSTEM_STATUS = "/system/status"; //$NON-NLS-1$
    private static final String SERVICES = "/services"; //$NON-NLS-1$
    private static final String APPLICATIONS = "/applications"; //$NON-NLS-1$
    
    private static final ObjectMapper mapper = new ObjectMapper();
    
    private String endpoint;
    private CloseableHttpClient httpClient;
    
    /**
     * Constructor.
     * @param gatewayEndpoint
     * @param httpClient
     */
    public GatewayClient(String endpoint, CloseableHttpClient httpClient) {
        this.endpoint = endpoint;
        this.httpClient = httpClient;
        
        if (this.endpoint.endsWith("/")) { //$NON-NLS-1$
            this.endpoint = this.endpoint.substring(0, this.endpoint.length() - 1);
        }
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.ISystemResource#getStatus()
     */
    public SystemStatus getStatus() {
        InputStream is = null;
        try {
            URI uri = new URI(this.endpoint + SYSTEM_STATUS);
            HttpGet get = new HttpGet(uri);
            HttpResponse response = httpClient.execute(get);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode != 200) {
                throw new Exception("System status check failed: " + actualStatusCode); //$NON-NLS-1$
            }
            is = response.getEntity().getContent();
            return mapper.reader(SystemStatus.class).readValue(is);
        } catch (Exception e) {
            // TODO log this error
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IServiceResource#getServiceEndpoint(java.lang.String, java.lang.String, java.lang.String)
     */
    public ServiceEndpoint getServiceEndpoint(String organizationId, String serviceId, String version)
            throws NotAuthorizedException {
        InputStream is = null;
        try {
            @SuppressWarnings("nls")
            URI uri = new URI(this.endpoint + SERVICES + "/" + organizationId + "/" + serviceId + "/" + version + "/endpoint");
            HttpGet get = new HttpGet(uri);
            HttpResponse response = httpClient.execute(get);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode != 200) {
                throw new Exception("Failed to get the service endpoint: " + actualStatusCode); //$NON-NLS-1$
            }
            is = response.getEntity().getContent();
            return mapper.reader(ServiceEndpoint.class).readValue(is);
        } catch (Exception e) {
            // TODO log this error
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IApplicationResource#register(io.apiman.gateway.engine.beans.Application)
     */
    public void register(Application application) throws RegistrationException, NotAuthorizedException {
        try {
            URI uri = new URI(this.endpoint + APPLICATIONS);
            HttpPut put = new HttpPut(uri);
            put.setHeader("Content-Type", "application/json; charset=utf-8"); //$NON-NLS-1$ //$NON-NLS-2$
            String jsonPayload = mapper.writer().writeValueAsString(application);
            HttpEntity entity = new StringEntity(jsonPayload);
            put.setEntity(entity);
            HttpResponse response = httpClient.execute(put);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode >= 300) {
                throw new Exception(Messages.i18n.format("GatewayClient.AppRegistrationFailed", actualStatusCode)); //$NON-NLS-1$
            }
        } catch (Exception e) {
            // TODO log this error
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IApplicationResource#unregister(java.lang.String, java.lang.String, java.lang.String)
     */
    public void unregister(String organizationId, String applicationId, String version)
            throws RegistrationException, NotAuthorizedException {
        try {
            @SuppressWarnings("nls")
            URI uri = new URI(this.endpoint + APPLICATIONS + "/" + organizationId + "/" + applicationId + "/" + version);
            HttpDelete put = new HttpDelete(uri);
            HttpResponse response = httpClient.execute(put);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode >= 300) {
                throw new Exception(Messages.i18n.format("GatewayClient.AppUnregistrationFailed", actualStatusCode)); //$NON-NLS-1$
            }
        } catch (Exception e) {
            // TODO log this error
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IServiceResource#publish(io.apiman.gateway.engine.beans.Service)
     */
    public void publish(Service service) throws PublishingException, NotAuthorizedException {
        try {
            URI uri = new URI(this.endpoint + SERVICES);
            HttpPut put = new HttpPut(uri);
            put.setHeader("Content-Type", "application/json; charset=utf-8"); //$NON-NLS-1$ //$NON-NLS-2$
            String jsonPayload = mapper.writer().writeValueAsString(service);
            HttpEntity entity = new StringEntity(jsonPayload);
            put.setEntity(entity);
            HttpResponse response = httpClient.execute(put);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode >= 300) {
                throw new Exception(Messages.i18n.format("GatewayClient.ServicePublishingFailed", actualStatusCode)); //$NON-NLS-1$
            }
        } catch (Exception e) {
            // TODO log this error
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.gateway.api.rest.contract.IServiceResource#retire(java.lang.String, java.lang.String, java.lang.String)
     */
    public void retire(String organizationId, String serviceId, String version) throws RegistrationException,
            NotAuthorizedException {
        try {
            @SuppressWarnings("nls")
            URI uri = new URI(this.endpoint + SERVICES + "/" + organizationId + "/" + serviceId + "/" + version);
            HttpDelete put = new HttpDelete(uri);
            HttpResponse response = httpClient.execute(put);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode >= 300) {
                throw new Exception(Messages.i18n.format("GatewayClient.ServiceRetiringFailed", actualStatusCode)); //$NON-NLS-1$
            }
        } catch (Exception e) {
            // TODO log this error
            throw new RuntimeException(e);
        }
    }

}
