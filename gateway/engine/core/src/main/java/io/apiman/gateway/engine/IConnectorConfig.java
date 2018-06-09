/*
 * Copyright 2018 JBoss Inc
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

package io.apiman.gateway.engine;

import java.util.Set;

/**
 * Allows policies to mutate certain HTTP connector attributes.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public interface IConnectorConfig {
    /**
     * Suppress the named request header
     *
     * @param headerName the header name
     */
    void suppressRequestHeader(String headerName);

    /**
     * Suppress the named response header
     *
     * @param headerName the header name
     */
    void suppressResponseHeader(String headerName);

    /**
     * Permit a request header that may be suppressed.
     *
     * @param headerName the header name
     */
    void permitRequestHeader(String headerName);

    /**
     * Permit a response header that may be suppressed.
     *
     * @param headerName the header name
     */
    void permitResponseHeader(String headerName);

    /**
     * <em>Unmodifiable</em> request headers.
     *
     * Use permit and suppress methods to mutate.
     *
     * @return the suppressed request headers
     */
    Set<String> getSuppressedRequestHeaders();

    /**
     * <em>Unmodifiable</em> response headers.
     *
     * Use permit and suppress methods to mutate.
     *
     * @return the suppressed response headers
     */
    Set<String> getSuppressedResponseHeaders();


}
