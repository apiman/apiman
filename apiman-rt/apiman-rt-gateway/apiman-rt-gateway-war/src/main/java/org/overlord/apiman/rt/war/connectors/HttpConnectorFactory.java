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
package org.overlord.apiman.rt.war.connectors;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.overlord.apiman.rt.engine.IConnectorFactory;
import org.overlord.apiman.rt.engine.IServiceConnector;
import org.overlord.apiman.rt.engine.async.AsyncResultImpl;
import org.overlord.apiman.rt.engine.async.IAsyncHandler;
import org.overlord.apiman.rt.engine.beans.Service;
import org.overlord.apiman.rt.engine.beans.ServiceRequest;
import org.overlord.apiman.rt.engine.beans.ServiceResponse;
import org.overlord.apiman.rt.engine.beans.exceptions.ConnectorException;
import org.overlord.apiman.rt.war.WarGatewayThreadContext;
import org.overlord.apiman.rt.war.i18n.Messages;

/**
 * Connector factory that uses HTTP to invoke back end systems.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpConnectorFactory implements IConnectorFactory {
    
    private static final Set<String> SUPPRESSED_HEADERS = new HashSet<String>();
    static {
        SUPPRESSED_HEADERS.add("Transfer-Encoding"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("Content-Length"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("X-API-Key"); //$NON-NLS-1$
    }
    private CloseableHttpClient httpclient = HttpClientBuilder.create().build();

    /**
     * Constructor.
     */
    public HttpConnectorFactory() {
    }

    /**
     * @see org.overlord.apiman.rt.engine.IConnectorFactory#createConnector(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.beans.Service)
     */
    @Override
    public IServiceConnector createConnector(ServiceRequest request, final Service service) {
        return new IServiceConnector() {
            /**
             * @see org.overlord.apiman.rt.engine.IServiceConnector#invoke(org.overlord.apiman.rt.engine.beans.ServiceRequest, org.overlord.apiman.rt.engine.async.IAsyncHandler)
             */
            @Override
            public void invoke(ServiceRequest request, IAsyncHandler<ServiceResponse> handler)
                    throws ConnectorException {
                try {
                    ServiceResponse response = doInvoke(request, service);
                    handler.handle(AsyncResultImpl.create(response));
                } catch (Exception e) {
                    handler.handle(AsyncResultImpl.<ServiceResponse>create(e));
                }
            }
        };
    }

    /**
     * Perform the invoke to the back end system using HTTP.
     * @param request the inbound service request
     * @param service the managed service being invoked
     * @return a service response
     * @throws ConnectorException
     */
    protected ServiceResponse doInvoke(ServiceRequest request, Service service) throws ConnectorException {
        String endpoint = service.getEndpoint();
        if (endpoint.endsWith("/")) { //$NON-NLS-1$
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        endpoint += request.getDestination();

        HttpRequestBase httpmethod = null;
        if ("GET".equals(request.getType())) { //$NON-NLS-1$
            httpmethod = new HttpGet(endpoint);
        } else if ("PUT".equals(request.getType())) { //$NON-NLS-1$
            httpmethod = new HttpPut(endpoint);
            HttpEntity entity = new InputStreamEntity(request.getBody(), getContentLength(request));
            ((HttpPut) httpmethod).setEntity(entity);
        } else if ("POST".equals(request.getType())) { //$NON-NLS-1$
            httpmethod = new HttpPost(endpoint);
            HttpEntity entity = new InputStreamEntity(request.getBody(), getContentLength(request));
            ((HttpPost) httpmethod).setEntity(entity);
        } else if ("DELETE".equals(request.getType())) { //$NON-NLS-1$
            httpmethod = new HttpDelete(endpoint);
        } else {
            throw new ConnectorException(Messages.i18n.format("HttpConnectorFactory.MethodNotSupported", request.getType())); //$NON-NLS-1$
        }

        // Set the request headers
        for (Entry<String, String> entry : request.getHeaders().entrySet()) {
            String hname = entry.getKey();
            String hval = entry.getValue();
            if (!SUPPRESSED_HEADERS.contains(hname)) {
                httpmethod.setHeader(hname, hval);
            }
        }

        ServiceResponse sresponse = null;
        try {
            // Do the actual invoke via HTTP
            HttpResponse response = httpclient.execute(httpmethod);
            
            // Process the response, convert to a ServiceResponse object, and return it
            StatusLine statusLine = response.getStatusLine();

            sresponse = WarGatewayThreadContext.getServiceResponse();
            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                sresponse.getHeaders().put(header.getName(), header.getValue());
            }
            sresponse.setCode(statusLine.getStatusCode());
            sresponse.setMessage(statusLine.getReasonPhrase());
            sresponse.setBody(response.getEntity().getContent());
            return sresponse;
        } catch (Exception e) {
            throw new ConnectorException(Messages.i18n.format("HttpConnectorFactory.ErrorInvokingService"), e); //$NON-NLS-1$
        }
    }

    /**
     * Gets the content-length from the inbound service request.
     * @param request the inbound service request
     * @return the content length
     */
    private long getContentLength(ServiceRequest request) {
        String cl = request.getHeaders().get("Content-Length"); //$NON-NLS-1$
        if (cl == null || cl.trim().length() == 0) {
            return -1;
        }
        return new Integer(cl).intValue();
    }

}
