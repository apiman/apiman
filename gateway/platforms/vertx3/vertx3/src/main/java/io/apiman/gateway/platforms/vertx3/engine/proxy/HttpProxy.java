/*
 * Copyright 2021 Scheer PAS Schweiz AG
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
package io.apiman.gateway.platforms.vertx3.engine.proxy;

import io.apiman.gateway.platforms.vertx3.engine.proxy.HttpProxySettings.ProxyType;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Simple Http Proxy information holder.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class HttpProxy {
    protected final ProxyType proxyType = ProxyType.HTTP;
    protected final String host;
    protected final int port;
    protected final AbstractCredentials credentials;

    public HttpProxy(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        if (username == null || username.length() == 0) {
            credentials = null;
        } else {
            credentials = new UsernamePasswordCredentials(username, password);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Optional<AbstractCredentials> getCredentials() {
        return Optional.of(credentials);
    }

    public boolean hasCredentials() {
        return credentials != null;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HttpProxy.class.getSimpleName() + "[", "]")
            .add("proxyType=" + proxyType)
            .add("host='" + host + "'")
            .add("port=" + port)
            .add("credentials=" + credentials)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpProxy httpProxy = (HttpProxy) o;
        return port == httpProxy.port && proxyType == httpProxy.proxyType && Objects
            .equals(host, httpProxy.host) && Objects.equals(credentials, httpProxy.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proxyType, host, port, credentials);
    }
}
