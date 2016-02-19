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
package io.apiman.gateway.platforms.servlet.components;

import io.apiman.gateway.engine.components.http.IHttpClientResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;

/**
 * A synchronous implementation of {@link IHttpClientResponse}.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpClientResponseImpl implements IHttpClientResponse {
    
    private HttpURLConnection connection;

    /**
     * Constructor.
     * @param connection the connection
     */
    public HttpClientResponseImpl(HttpURLConnection connection) {
        this.connection = connection;
    }

    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientResponse#getResponseCode()
     */
    @Override
    public int getResponseCode() {
        try {
            return connection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientResponse#getResponseMessage()
     */
    @Override
    public String getResponseMessage() {
        try {
            return connection.getResponseMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientResponse#getHeader(java.lang.String)
     */
    @Override
    public String getHeader(String headerName) {
        return connection.getHeaderField(headerName);
    }

    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientResponse#getBody()
     */
    @Override
    public String getBody() {
        try (InputStream body = connection.getInputStream();
             StringBuilderWriter writer = new StringBuilderWriter()) {
            if (body != null) {
                try {
                    IOUtils.copy(body, writer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return writer.getBuilder().toString();
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * @see io.apiman.gateway.engine.components.http.IHttpClientResponse#close()
     */
    @Override
    public void close() {
        try {
            IOUtils.closeQuietly(connection.getInputStream());
            connection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
