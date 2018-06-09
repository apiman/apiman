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

package io.apiman.gateway.engine.impl;

import io.apiman.gateway.engine.IConnectorConfig;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Connector configuration with lazy initialisation of customised suppressions.
 *
 * Implementors should provide a static immutable default maps to {@link AbstractConnectorConfig}.
 * This will be copied when needed into a mutable map (i.e. when the user adds/removes something).
 *
 * Future work could include caching common suppression maps, potentially.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public abstract class AbstractConnectorConfig implements IConnectorConfig {

    protected Set<String> suppressedRequestHeaders;
    private boolean modifiedDefaultRequestHeaders = false;

    protected Set<String> suppressedResponseHeaders;
    private boolean modifiedDefaultResponseHeaders = false;

    /**
     * Suppressed request headers
     * Suppressed response headers
     *
     * @param suppressedRequestHeaders request headers to suppress
     * @param suppressedResponseHeaders response headers to suppress
     */
    public AbstractConnectorConfig(Set<String> suppressedRequestHeaders, Set<String> suppressedResponseHeaders) {
        this.suppressedRequestHeaders = suppressedRequestHeaders;
        this.suppressedResponseHeaders = suppressedResponseHeaders;
    }

    /**
     * No suppressed request nor response headers.
     */
    public AbstractConnectorConfig() {
        this.suppressedRequestHeaders = Collections.emptySet();
        this.suppressedResponseHeaders = Collections.emptySet();
    }

    @Override
    public void suppressRequestHeader(String headerName) {
        if (!suppressedRequestHeaders.contains(headerName)) {
            copyRequestMap();
            suppressedRequestHeaders.add(headerName);
        }
    }

    @Override
    public void suppressResponseHeader(String headerName) {
        if (!suppressedResponseHeaders.contains(headerName)) {
            copyResponseMap();
            suppressedResponseHeaders.add(headerName);
        }
    }

    @Override
    public void permitRequestHeader(String headerName) {
        if (suppressedRequestHeaders.contains(headerName)) {
            copyRequestMap();
            suppressedRequestHeaders.remove(headerName);
        }
    }

    @Override
    public void permitResponseHeader(String headerName) {
        if (suppressedResponseHeaders.contains(headerName)) {
            copyResponseMap();
            suppressedResponseHeaders.remove(headerName);
        }
    }

    @Override
    public Set<String> getSuppressedRequestHeaders() {
        return Collections.unmodifiableSet(suppressedRequestHeaders);
    }

    @Override
    public Set<String> getSuppressedResponseHeaders() {
        return Collections.unmodifiableSet(suppressedResponseHeaders);
    }

    private void copyRequestMap() {
        if (!modifiedDefaultRequestHeaders) {
            TreeSet<String> reqCopy = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            reqCopy.addAll(suppressedRequestHeaders);
            modifiedDefaultRequestHeaders = true;
            this.suppressedRequestHeaders = reqCopy;
        }
    }

    private void copyResponseMap() {
        if (!modifiedDefaultResponseHeaders) {
            TreeSet<String> resCopy = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
            resCopy.addAll(suppressedResponseHeaders);
            modifiedDefaultResponseHeaders = true;
            this.suppressedResponseHeaders = resCopy;
        }
    }
}
