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
package org.overlord.apiman.rt.gateway.servlet.components;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.overlord.apiman.rt.engine.components.http.IHttpClientResponse;

/**
 * A synchronous implementation of {@link IHttpClientResponse}.
 *
 * @author eric.wittmann@redhat.com
 */
public class HttpClientResponseImpl implements IHttpClientResponse {
    
    private HttpResponse response;
    private InputStream body;

    /**
     * Constructor.
     * @param response
     * @param content
     */
    public HttpClientResponseImpl(HttpResponse response, InputStream content) {
        this.response = response;
        this.body = content;
    }

    /**
     * @see org.overlord.apiman.rt.engine.components.http.IHttpClientResponse#getResponseCode()
     */
    @Override
    public int getResponseCode() {
        return response.getStatusLine().getStatusCode();
    }

    /**
     * @see org.overlord.apiman.rt.engine.components.http.IHttpClientResponse#getResponseMessage()
     */
    @Override
    public String getResponseMessage() {
        return response.getStatusLine().getReasonPhrase();
    }

    /**
     * @see org.overlord.apiman.rt.engine.components.http.IHttpClientResponse#getHeader(java.lang.String)
     */
    @Override
    public String getHeader(String headerName) {
        Header header = response.getFirstHeader(headerName);
        if (header != null) {
            return header.getValue();
        } else {
            return null;
        }
    }

    /**
     * @see org.overlord.apiman.rt.engine.components.http.IHttpClientResponse#getBody()
     */
    @Override
    public String getBody() {
        if (body != null) {
            StringBuilderWriter writer = new StringBuilderWriter();
            try {
                IOUtils.copy(body, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return writer.getBuilder().toString();
        } else {
            return null;
        }
    }
    
    /**
     * @see org.overlord.apiman.rt.engine.components.http.IHttpClientResponse#close()
     */
    @Override
    public void close() {
        try {
            EntityUtils.consume(response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
