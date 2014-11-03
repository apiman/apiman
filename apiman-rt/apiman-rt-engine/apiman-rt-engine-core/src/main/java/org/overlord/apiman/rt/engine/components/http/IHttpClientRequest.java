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
package org.overlord.apiman.rt.engine.components.http;

/**
 * An async client request returned by the http client component.
 * 
 * @author eric.wittmann@redhat.com
 */
public interface IHttpClientRequest {

    /**
     * Adds a header to the request.
     * 
     * @param headerName
     * @param headerValue
     */
    public void addHeader(String headerName, String headerValue);

    /**
     * Removes a header.
     * 
     * @param headerName
     */
    public void removeHeader(String headerName);

    /**
     * Writes some data to the request. This data represents the body of the
     * http request. Obviously this is optional depending on the type of request
     * being made.
     * 
     * @param data
     */
    public void write(byte[] data);

    /**
     * Writes some data to the request, forming the body. Obviously this is
     * optional depending on the type of request being made.
     * 
     * @param body
     */
    public void write(String body);

    /**
     * Called once all headers have been set and all data has been written.
     */
    public void end();
}
