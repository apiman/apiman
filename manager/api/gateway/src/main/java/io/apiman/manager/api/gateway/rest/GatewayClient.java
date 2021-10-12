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

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;
import io.apiman.common.util.MediaType;
import io.apiman.gateway.api.rest.IApiResource;
import io.apiman.gateway.api.rest.IClientResource;
import io.apiman.gateway.api.rest.ISystemResource;
import io.apiman.gateway.api.rest.exceptions.GatewayApiErrorBean;
import io.apiman.gateway.engine.beans.Api;
import io.apiman.gateway.engine.beans.ApiEndpoint;
import io.apiman.gateway.engine.beans.Client;
import io.apiman.gateway.engine.beans.GatewayEndpoint;
import io.apiman.gateway.engine.beans.IPolicyProbeResponse;
import io.apiman.gateway.engine.beans.SystemStatus;
import io.apiman.gateway.engine.beans.exceptions.PublishingException;
import io.apiman.gateway.engine.beans.exceptions.RegistrationException;
import io.apiman.gateway.engine.policies.probe.ProbeRegistry;
import io.apiman.manager.api.gateway.GatewayAuthenticationException;
import io.apiman.manager.api.gateway.i18n.Messages;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.UriBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * A REST client for accessing the Gateway API.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("javadoc") // class is temporarily delinked from its interfaces
public class GatewayClient /*implements ISystemResource, IApiResource, IClientResource*/ {

    private static final String SYSTEM_STATUS = "/system/status"; //$NON-NLS-1$
    private static final String SYSTEM_ENDPOINT = "/system/endpoint"; //$NON-NLS-1$
    private static final String APIs = "/apis"; //$NON-NLS-1$
    private static final String CLIENTS = "/clients"; //$NON-NLS-1$

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(GatewayClient.class);

    private String endpoint;
    private CloseableHttpClient httpClient;

    /**
     * Constructor.
     * @param endpoint the endpoint
     * @param httpClient the http client
     */
    public GatewayClient(String endpoint, CloseableHttpClient httpClient) {
        this.endpoint = endpoint;
        this.httpClient = httpClient;

        if (this.endpoint.endsWith("/")) { //$NON-NLS-1$
            this.endpoint = this.endpoint.substring(0, this.endpoint.length() - 1);
        }
    }

    public IPolicyProbeResponse probePolicy(String orgId, String apiId, String apiVersion, int idx) throws GatewayAuthenticationException {
        return probePolicy(orgId, apiId, apiVersion, idx, "", "");
    }

    public IPolicyProbeResponse probePolicy(String orgId, String apiId, String apiVersion, int idx, String apiKey, String rawPayload) throws GatewayAuthenticationException {
        InputStream probeResponseIs = null;
        try {
            UriBuilder probeUrl = UriBuilder.fromUri(endpoint)
                    .path("organizations")
                    .path(orgId)
                    .path("apis")
                    .path(apiId)
                    .path("versions")
                    .path(apiVersion)
                    .path("policies")
                    .path(String.valueOf(idx));
            if (apiKey != null && !apiKey.isBlank()) {
                probeUrl.queryParam("apiKey", apiKey);
            }
            HttpPost post = new HttpPost(probeUrl.build());
            post.setHeader("Content-Type", MediaType.APPLICATION_JSON);
            HttpEntity entity = new ByteArrayEntity(rawPayload.getBytes(StandardCharsets.UTF_8), ContentType.APPLICATION_JSON);
            post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode == 401 || actualStatusCode == 403) {
                throw new GatewayAuthenticationException();
            } else if (!(actualStatusCode / 100 == 2)) {
                throw new RuntimeException("System status check failed: " + actualStatusCode + ": " + response.getStatusLine()); //$NON-NLS-1$
            }
            probeResponseIs = response.getEntity().getContent();
            return ProbeRegistry.deserialize(probeResponseIs);
        } catch (GatewayAuthenticationException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(probeResponseIs);
        }
    }

    /**
     * @see ISystemResource#getStatus()
     */
    public SystemStatus getStatus() throws GatewayAuthenticationException {
        InputStream is = null;
        try {
            URI uri = new URI(this.endpoint + SYSTEM_STATUS);
            HttpGet get = new HttpGet(uri);
            HttpResponse response = httpClient.execute(get);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode == 401 || actualStatusCode == 403) {
                throw new GatewayAuthenticationException();
            } else if (actualStatusCode != 200) {
                throw new RuntimeException("System status check failed: " + actualStatusCode); //$NON-NLS-1$
            }
            is = response.getEntity().getContent();
            return mapper.reader(SystemStatus.class).readValue(is);
        } catch (GatewayAuthenticationException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * @see ISystemResource#getEndpoint()
     */
    public GatewayEndpoint getGatewayEndpoint() throws GatewayAuthenticationException {
        InputStream is = null;
        try {
            @SuppressWarnings("nls")
            URI uri = new URI(this.endpoint + SYSTEM_ENDPOINT);
            HttpGet get = new HttpGet(uri);
            HttpResponse response = httpClient.execute(get);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode == 401 || actualStatusCode == 403) {
                throw new GatewayAuthenticationException();
            }
            if (actualStatusCode != 200) {
                throw new RuntimeException("Failed to get the API endpoint: " + actualStatusCode); //$NON-NLS-1$
            }
            is = response.getEntity().getContent();
            return mapper.reader(GatewayEndpoint.class).readValue(is);
        } catch (GatewayAuthenticationException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * @see IApiResource#getApiEndpoint(java.lang.String, java.lang.String, java.lang.String)
     */
    public ApiEndpoint getApiEndpoint(String organizationId, String apiId, String version)
            throws GatewayAuthenticationException {
        InputStream is = null;
        try {
            @SuppressWarnings("nls")
            URI uri = new URI(this.endpoint + APIs + "/" + organizationId + "/" + apiId + "/" + version + "/endpoint");
            HttpGet get = new HttpGet(uri);
            HttpResponse response = httpClient.execute(get);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode == 401 || actualStatusCode == 403) {
                throw new GatewayAuthenticationException();
            }
            if (actualStatusCode != 200) {
                throw new RuntimeException("Failed to get the API endpoint: " + actualStatusCode); //$NON-NLS-1$
            }
            is = response.getEntity().getContent();
            return mapper.reader(ApiEndpoint.class).readValue(is);
        } catch (GatewayAuthenticationException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * @see IClientResource#register(io.apiman.gateway.engine.beans.Client)
     */
    public void register(Client client) throws RegistrationException, GatewayAuthenticationException {
        try {
            URI uri = new URI(this.endpoint + CLIENTS);
            HttpPut put = new HttpPut(uri);
            put.setHeader("Content-Type", "application/json; charset=utf-8"); //$NON-NLS-1$ //$NON-NLS-2$
            String jsonPayload = mapper.writer().writeValueAsString(client);
            HttpEntity entity = new StringEntity(jsonPayload);
            put.setEntity(entity);
            HttpResponse response = httpClient.execute(put);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode == 401 || actualStatusCode == 403) {
                throw new GatewayAuthenticationException();
            }
            if (actualStatusCode == 500) {
                Header[] headers = response.getHeaders("X-API-Gateway-Error"); //$NON-NLS-1$
                if (headers != null && headers.length > 0) {
                    throw readRegistrationException(response);
                }
            }
            if (actualStatusCode >= 300) {
                throw new RuntimeException(Messages.i18n.format("GatewayClient.ClientRegistrationFailed", actualStatusCode)); //$NON-NLS-1$
            }
        } catch (GatewayAuthenticationException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see IClientResource#unregister(java.lang.String, java.lang.String, java.lang.String)
     */
    public void unregister(String organizationId, String clientId, String version)
            throws RegistrationException, GatewayAuthenticationException {
        try {
            @SuppressWarnings("nls")
            URI uri = new URI(this.endpoint + CLIENTS + "/" + organizationId + "/" + clientId + "/" + version);
            HttpDelete put = new HttpDelete(uri);
            HttpResponse response = httpClient.execute(put);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode == 401 || actualStatusCode == 403) {
                throw new GatewayAuthenticationException();
            }
            if (actualStatusCode == 500) {
                Header[] headers = response.getHeaders("X-API-Gateway-Error"); //$NON-NLS-1$
                if (headers != null && headers.length > 0) {
                    RegistrationException re = readRegistrationException(response);
                    throw re;
                }
            }
            if (actualStatusCode >= 300) {
                throw new Exception(Messages.i18n.format("GatewayClient.ClientUnregistrationFailed", actualStatusCode)); //$NON-NLS-1$
            }
        } catch (RegistrationException|GatewayAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see IApiResource#publish(io.apiman.gateway.engine.beans.Api)
     */
    public void publish(Api api) throws PublishingException, GatewayAuthenticationException {
        try {
            URI uri = new URI(this.endpoint + APIs);
            HttpPut put = new HttpPut(uri);
            put.setHeader("Content-Type", "application/json; charset=utf-8"); //$NON-NLS-1$ //$NON-NLS-2$
            String jsonPayload = mapper.writer().writeValueAsString(api);
            HttpEntity entity = new StringEntity(jsonPayload);
            put.setEntity(entity);
            HttpResponse response = httpClient.execute(put);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode == 401 || actualStatusCode == 403) {
                throw new GatewayAuthenticationException();
            }
            if (actualStatusCode == 500) {
                Header[] headers = response.getHeaders("X-API-Gateway-Error"); //$NON-NLS-1$
                if (headers != null && headers.length > 0) {
                    PublishingException pe = readPublishingException(response);
                    throw pe;
                }
            }
            if (actualStatusCode >= 300) {
                throw new Exception(Messages.i18n.format("GatewayClient.ApiPublishingFailed", actualStatusCode)); //$NON-NLS-1$
            }
        } catch (PublishingException|GatewayAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see IApiResource#retire(java.lang.String, java.lang.String, java.lang.String)
     */
    public void retire(String organizationId, String apiId, String version) throws RegistrationException, GatewayAuthenticationException {
        try {
            @SuppressWarnings("nls")
            URI uri = new URI(this.endpoint + APIs + "/" + organizationId + "/" + apiId + "/" + version);
            HttpDelete put = new HttpDelete(uri);
            HttpResponse response = httpClient.execute(put);
            int actualStatusCode = response.getStatusLine().getStatusCode();
            if (actualStatusCode == 401 || actualStatusCode == 403) {
                throw new GatewayAuthenticationException();
            }
            if (actualStatusCode == 500) {
                Header[] headers = response.getHeaders("X-API-Gateway-Error"); //$NON-NLS-1$
                if (headers != null && headers.length > 0) {
                    PublishingException pe = readPublishingException(response);
                    throw pe;
                }
            }
            if (actualStatusCode >= 300) {
                throw new Exception(Messages.i18n.format("GatewayClient.ApiRetiringFailed", actualStatusCode)); //$NON-NLS-1$
            }
        } catch (PublishingException|GatewayAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a publishing exception from the response.
     * @param response
     */
    private PublishingException readPublishingException(HttpResponse response) {
        InputStream is = null;
        PublishingException exception;
        try {
            is = response.getEntity().getContent();
            GatewayApiErrorBean error = mapper.reader(GatewayApiErrorBean.class).readValue(is);
            exception = new PublishingException(error.getMessage());
            StackTraceElement[] stack = parseStackTrace(error.getStacktrace());
            if (stack != null) {
                exception.setStackTrace(stack);
            }
        } catch (Exception e) {
            exception = new PublishingException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return exception;
    }

    /**
     * Reads a registration exception from the response body.
     * @param response
     */
    private RegistrationException readRegistrationException(HttpResponse response) {
        InputStream is = null;
        RegistrationException exception;
        try {
            is = response.getEntity().getContent();
            GatewayApiErrorBean error = mapper.reader(GatewayApiErrorBean.class).readValue(is);
            exception = new RegistrationException(error.getMessage());
            StackTraceElement[] stack = parseStackTrace(error.getStacktrace());
            if (stack != null) {
                exception.setStackTrace(stack);
            }
        } catch (Exception e) {
            exception = new RegistrationException(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
        return exception;
    }

    /**
     * Parses a stack trace from the given string.
     * @param stacktrace
     */
    protected static StackTraceElement[] parseStackTrace(String stacktrace) {
        try (BufferedReader reader = new BufferedReader(new StringReader(stacktrace))) {
            List<StackTraceElement> elements = new ArrayList<>();
            String line;
            // Example lines:
            // \tat io.apiman.gateway.engine.es.EsRegistry$1.completed(EsRegistry.java:79)
            // \tat org.apache.http.impl.nio.client.InternalIODispatch.onInputReady(InternalIODispatch.java:81)\r\n
            while ( (line = reader.readLine()) != null) {
                if (line.startsWith("\tat ")) { //$NON-NLS-1$
                    int openParenIdx = line.indexOf('(');
                    int closeParenIdx = line.indexOf(')');
                    String classAndMethod = line.substring(4, openParenIdx);
                    String fileAndLineNum = line.substring(openParenIdx + 1, closeParenIdx);
                    String className = classAndMethod.substring(0, classAndMethod.lastIndexOf('.'));
                    String methodName = classAndMethod.substring(classAndMethod.lastIndexOf('.') + 1);
                    String [] split = fileAndLineNum.split(":"); //$NON-NLS-1$
                    if (split.length == 1) {
                        elements.add(new StackTraceElement(className, methodName, fileAndLineNum, -1));
                    } else {
                        String fileName = split[0];
                        String lineNum = split[1];
                        elements.add(new StackTraceElement(className, methodName, fileName, Integer.parseInt(lineNum)));
                    }
                }
            }
            return elements.toArray(new StackTraceElement[elements.size()]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
