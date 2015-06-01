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
package io.apiman.gateway.platforms.servlet.connectors;

import io.apiman.common.config.options.BasicAuthOptions;
import io.apiman.gateway.engine.IServiceConnection;
import io.apiman.gateway.engine.IServiceConnectionResponse;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.apiman.gateway.engine.beans.Service;
import io.apiman.gateway.engine.beans.ServiceRequest;
import io.apiman.gateway.engine.beans.ServiceResponse;
import io.apiman.gateway.engine.beans.exceptions.ConnectorException;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.platforms.servlet.GatewayThreadContext;
import io.apiman.gateway.platforms.servlet.connectors.ssl.SSLSessionStrategy;
import io.apiman.gateway.platforms.servlet.io.ByteBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Models a live connection to a back end service.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpServiceConnection implements IServiceConnection, IServiceConnectionResponse {

    private static final Set<String> SUPPRESSED_HEADERS = new HashSet<>();
    static {
        SUPPRESSED_HEADERS.add("Transfer-Encoding"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("Content-Length"); //$NON-NLS-1$
        SUPPRESSED_HEADERS.add("X-API-Key"); //$NON-NLS-1$
    }

    private ServiceRequest request;
    private Service service;
    private RequiredAuthType requiredAuthType;
    private SSLSessionStrategy sslStrategy;
    private IAsyncResultHandler<IServiceConnectionResponse> responseHandler;

    private boolean connected;
    private HttpURLConnection connection;
    private OutputStream outputStream;

    private IAsyncHandler<IApimanBuffer> bodyHandler;
    private IAsyncHandler<Void> endHandler;

    private ServiceResponse response;

    /**
     * Constructor.
     *
     * @param sslStrategy the SSL strategy
     * @param request the request
     * @param service the service
     * @param requiredAuthType the authorization type
     * @param handler the result handler
     * @throws ConnectorException when unable to connect
     */
    public HttpServiceConnection(ServiceRequest request, Service service, RequiredAuthType requiredAuthType,
            SSLSessionStrategy sslStrategy, IAsyncResultHandler<IServiceConnectionResponse> handler)
            throws ConnectorException {
        this.request = request;
        this.service = service;
        this.requiredAuthType = requiredAuthType;
        this.sslStrategy = sslStrategy;
        this.responseHandler = handler;

        try {
            connect();
        } catch (Exception e) {
            handler.handle(AsyncResultImpl.<IServiceConnectionResponse> create(e));
        }
    }
    /**
     * Connects to the back end system.
     */
    private void connect() throws ConnectorException {
        try {
            String endpoint = service.getEndpoint();
            if (endpoint.endsWith("/")) { //$NON-NLS-1$
                endpoint = endpoint.substring(0, endpoint.length() - 1);
            }
            if (request.getDestination() != null) {
                endpoint += request.getDestination();
            }
            if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
                String delim = "?"; //$NON-NLS-1$
                for (Entry<String, String> entry : request.getQueryParams().entrySet()) {
                    endpoint += delim + entry.getKey();
                    if (entry.getValue() != null) {
                        endpoint += "=" + URLEncoder.encode(entry.getValue(), "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    delim = "&"; //$NON-NLS-1$
                }
            }
            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();

            boolean isSsl = connection instanceof HttpsURLConnection;

            if(requiredAuthType == RequiredAuthType.MTLS && !isSsl) {
                throw new ConnectorException("Mutually authenticating TLS requested, but insecure endpoint protocol was indicated."); //$NON-NLS-1$
            }

            if (requiredAuthType == RequiredAuthType.BASIC) {
                BasicAuthOptions options = new BasicAuthOptions(service.getEndpointProperties());
                if (options.getUsername() != null && options.getPassword() != null) {
                    if (options.isRequireSSL() && !isSsl) {
                        throw new ConnectorException("Endpoint security requested (BASIC auth) but endpoint is not secure (SSL)."); //$NON-NLS-1$
                    }

                    String up = options.getUsername() + ':' + options.getPassword();
                    StringBuilder builder = new StringBuilder();
                    builder.append("BASIC "); //$NON-NLS-1$
                    builder.append(Base64.encodeBase64String(up.getBytes()));
                    connection.setRequestProperty("Authorization", builder.toString()); //$NON-NLS-1$
                }
            }


            if (isSsl) {
                HttpsURLConnection https = (HttpsURLConnection) connection;
                SSLSocketFactory socketFactory = sslStrategy.getSocketFactory();
                https.setSSLSocketFactory(socketFactory);
                https.setHostnameVerifier(sslStrategy.getHostnameVerifier());
            }

            connection.setReadTimeout(15000);
            connection.setConnectTimeout(10000);
            if (request.getType().equalsIgnoreCase("PUT") || request.getType().equalsIgnoreCase("POST")) { //$NON-NLS-1$ //$NON-NLS-2$
                connection.setDoOutput(true);
            } else {
                connection.setDoOutput(false);
            }
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod(request.getType());

            // Set the request headers
            for (Entry<String, String> entry : request.getHeaders().entrySet()) {
                String hname = entry.getKey();
                String hval = entry.getValue();
                if (!SUPPRESSED_HEADERS.contains(hname)) {
                    connection.setRequestProperty(hname, hval);
                }
            }

            connection.connect();
            connected = true;
        } catch (IOException e) {
            throw new ConnectorException(e);
        }
    }

    /**
     * @see io.apiman.gateway.engine.io.IReadStream#bodyHandler(io.apiman.gateway.engine.async.IAsyncHandler)
     */
    @Override
    public void bodyHandler(IAsyncHandler<IApimanBuffer> bodyHandler) {
        this.bodyHandler = bodyHandler;
    }

    /**
     * @see io.apiman.gateway.engine.io.IReadStream#endHandler(io.apiman.gateway.engine.async.IAsyncHandler)
     */
    @Override
    public void endHandler(IAsyncHandler<Void> endHandler) {
        this.endHandler = endHandler;
    }

    /**
     * @see io.apiman.gateway.engine.io.IReadStream#getHead()
     */
    @Override
    public ServiceResponse getHead() {
        return response;
    }

    /**
     * @see io.apiman.gateway.engine.io.IStream#isFinished()
     */
    @Override
    public boolean isFinished() {
        return !connected;
    }

    /**
     * @see io.apiman.gateway.engine.IServiceConnection#isConnected()
     */
    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * @see io.apiman.gateway.engine.io.IAbortable#abort()
     */
    @Override
    public void abort() {
        try {
            if (!connected) {
                throw new IOException("Not connected."); //$NON-NLS-1$
            }
            if (connection != null) {
                IOUtils.closeQuietly(outputStream);
                IOUtils.closeQuietly(connection.getInputStream());
                connected = false;
                connection.disconnect();
            }
        } catch (IOException e) {
            // TODO log this error but don't rethrow it
        }
    }

    /**
     * @see io.apiman.gateway.engine.io.IWriteStream#write(io.apiman.gateway.engine.io.IApimanBuffer)
     */
    @Override
    public void write(IApimanBuffer chunk) {
        try {
            if (!connected) {
                throw new IOException("Not connected."); //$NON-NLS-1$
            }
            if (outputStream == null) {
                outputStream = connection.getOutputStream();
            }
            if (chunk instanceof ByteBuffer) {
                byte[] buffer = (byte[]) chunk.getNativeBuffer();
                outputStream.write(buffer, 0, chunk.length());
            } else {
                outputStream.write(chunk.getBytes());
            }
        } catch (IOException e) {
            // TODO log this error.
            throw new ConnectorException(e);
        }
    }

    /**
     * @see io.apiman.gateway.engine.io.IWriteStream#end()
     */
    @Override
    public void end() {
        try {
            if (!connected) {
                throw new IOException("Not connected."); //$NON-NLS-1$
            }
            IOUtils.closeQuietly(outputStream);
            outputStream = null;
            // Process the response, convert to a ServiceResponse object, and return it
            response = GatewayThreadContext.getServiceResponse();
            Map<String, List<String>> headerFields = connection.getHeaderFields();
            for (String headerName : headerFields.keySet()) {
                if (headerName != null) {
                    response.getHeaders().put(headerName, connection.getHeaderField(headerName));
                }
            }
            response.setCode(connection.getResponseCode());
            response.setMessage(connection.getResponseMessage());
            responseHandler.handle(AsyncResultImpl.<IServiceConnectionResponse> create(this));
        } catch (Exception e) {
            // TODO log this error
            throw new ConnectorException(e);
        }
    }

    /**
     * @see io.apiman.gateway.engine.io.ISignalReadStream#transmit()
     */
    @Override
    public void transmit() {
        try {
            if (!connected) {
                throw new IOException("Not connected."); //$NON-NLS-1$
            }
            InputStream is = connection.getInputStream();
            ByteBuffer buffer = new ByteBuffer(2048);
            int numBytes = buffer.readFrom(is);
            while (numBytes != -1) {
                bodyHandler.handle(buffer);
                numBytes = buffer.readFrom(is);
            }
            IOUtils.closeQuietly(is);
            connection.disconnect();
            connected = false;
            endHandler.handle(null);
        } catch (Throwable e) {
            // At this point we're sort of screwed, because we've already sent the response to
            // the originating client - and we're in the process of sending the body data. So
            // I guess the only thing to do is abort() the connection and cross our fingers.
            if (connected) {
                abort();
            }
            throw new RuntimeException(e);
        }
    }

}
