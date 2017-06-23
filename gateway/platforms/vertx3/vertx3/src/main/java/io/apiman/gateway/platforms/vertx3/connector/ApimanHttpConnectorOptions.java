/*
 * Copyright 2017 JBoss Inc
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

package io.apiman.gateway.platforms.vertx3.connector;

import io.apiman.common.config.options.AbstractOptions;
import io.apiman.common.config.options.TLSOptions;
import io.apiman.gateway.engine.auth.RequiredAuthType;
import io.vertx.core.http.HttpClientOptions;

import java.net.URI;
import java.util.Map;

/**
 * Options for {@link HttpConnector}.
 *
 * @author Marc Savy {@literal <marc@rhymewithgravy.com>}
 */
public class ApimanHttpConnectorOptions extends AbstractOptions {

    private RequiredAuthType requiredAuthType = RequiredAuthType.DEFAULT;
    private boolean hasDataPolicy = false;
    private int connectionTimeout = HttpClientOptions.DEFAULT_CONNECT_TIMEOUT;
    private int idleTimeout = HttpClientOptions.DEFAULT_IDLE_TIMEOUT;
    private int requestTimeout = HttpClientOptions.DEFAULT_CONNECT_TIMEOUT * 2;
    private boolean keepAlive = HttpClientOptions.DEFAULT_KEEP_ALIVE;
    private boolean tryUseCompression = HttpClientOptions.DEFAULT_TRY_USE_COMPRESSION;
    private TLSOptions tlsOptions;
    private URI endpoint;
    private boolean isSsl;

    public ApimanHttpConnectorOptions() {
        super();
    }

    public ApimanHttpConnectorOptions(Map<String, String> options) {
        super();
        parse(options);
    }

    @Override
    protected void parse(Map<String, String> options) {
        connectionTimeout = parseInt(options, "http.timeouts.connect", HttpClientOptions.DEFAULT_CONNECT_TIMEOUT);
        requestTimeout = parseInt(options, "http.timeouts.read", HttpClientOptions.DEFAULT_CONNECT_TIMEOUT * 2);
    }

    /**
     * @return the requiredAuthType
     */
    public RequiredAuthType getRequiredAuthType() {
        return requiredAuthType;
    }
    /**
     * @param requiredAuthType the requiredAuthType to set
     * @return this
     */
    public ApimanHttpConnectorOptions setRequiredAuthType(RequiredAuthType requiredAuthType) {
        this.requiredAuthType = requiredAuthType;
        return this;
    }
    /**
     * @return the hasDataPolicy
     */
    public boolean hasDataPolicy() {
        return hasDataPolicy;
    }
    /**
     * @param hasDataPolicy the hasDataPolicy to set
     * @return this
     */
    public ApimanHttpConnectorOptions setHasDataPolicy(boolean hasDataPolicy) {
        this.hasDataPolicy = hasDataPolicy;
        return this;
    }
    /**
     * @return the connectionTimeout
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }
    /**
     * @param connectionTimeout the connectionTimeout to set
     * @return this
     */
    public ApimanHttpConnectorOptions setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }
    /**
     * @return the idleTimeout
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }
    /**
     * @param idleTimeout the idleTimeout to set
     * @return this
     */
    public ApimanHttpConnectorOptions setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
        return this;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
    /**
     * @return the keepAlive
     */
    public boolean isKeepAlive() {
        return keepAlive;
    }
    /**
     * @param keepAlive the keepAlive to set
     * @return this
     */
    public ApimanHttpConnectorOptions setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }
    /**
     * @return the tryUseCompression
     */
    public boolean isTryUseCompression() {
        return tryUseCompression;
    }
    /**
     * @param tryUseCompression the tryUseCompression to set
     * @return this
     */
    public ApimanHttpConnectorOptions setTryUseCompression(boolean tryUseCompression) {
        this.tryUseCompression = tryUseCompression;
        return this;
    }

    public TLSOptions getTlsOptions() {
        return tlsOptions;
    }

    public ApimanHttpConnectorOptions setTlsOptions(TLSOptions tlsOptions) {
        this.tlsOptions = tlsOptions;
        return this;
    }

    public URI getUri() {
        return endpoint;
    }

    public ApimanHttpConnectorOptions setUri(URI endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public ApimanHttpConnectorOptions setSsl(boolean isSsl) {
        this.isSsl = isSsl;
        return this;
    }

    public boolean isSsl() {
        return isSsl;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + connectionTimeout;
        result = prime * result + idleTimeout;
        result = prime * result + (keepAlive ? 1231 : 1237);
        result = prime * result + ((tlsOptions == null) ? 0 : tlsOptions.hashCode());
        result = prime * result + (tryUseCompression ? 1231 : 1237);
        result = prime * result + (isSsl ? 0 : 1);
        return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApimanHttpConnectorOptions other = (ApimanHttpConnectorOptions) obj;
        if (connectionTimeout != other.connectionTimeout)
            return false;
        if (idleTimeout != other.idleTimeout)
            return false;
        if (keepAlive != other.keepAlive)
            return false;
        if (tlsOptions == null) {
            if (other.tlsOptions != null)
                return false;
        } else if (!tlsOptions.equals(other.tlsOptions))
            return false;
        if (tryUseCompression != other.tryUseCompression)
            return false;
        return true;
    }


}
