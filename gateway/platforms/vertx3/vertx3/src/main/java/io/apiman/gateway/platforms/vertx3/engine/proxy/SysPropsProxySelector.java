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

import java.net.URI;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Get proxy settings from various system properties.
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class SysPropsProxySelector {
    private final JavaSysPropsProxySettings httpProxy =
        JavaSysPropsProxySettings.createHttpProxy("http", 80);

    private final JavaSysPropsProxySettings httpsProxy =
        JavaSysPropsProxySettings.createHttpProxy("https", 443);

    private final JavaSysPropsProxySettings socksProxy = JavaSysPropsProxySettings.createSocksProxy(1080);

    public SysPropsProxySelector() {
    }

    /**
     * Resolve a proxy, if there is one.
     *
     * @param resourceUri the URI that will be assessed against the proxy configuration
     * @return the proxy, if there is an appropriate one configured and it is not on the non-proxy list.
     */
    public Optional<HttpProxy> resolveProxy(URI resourceUri) {
        String scheme = resourceUri.getScheme();
        // Convention on proxy priority if multiple defined is HTTPS/HTTP then Socks.
        if (httpsProxy.isProxyDefined() && "https".equalsIgnoreCase(scheme)) {
            return httpsProxy.getProxy(resourceUri.getHost());
        }
        if (httpProxy.isProxyDefined() && "http".equalsIgnoreCase(scheme) ) {
            return httpProxy.getProxy(resourceUri.getHost());
        }
        if (socksProxy.isProxyDefined()) {
            return socksProxy.getProxy(resourceUri.getHost());
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SysPropsProxySelector.class.getSimpleName() + "[", "]")
            .add("httpProxy=" + httpProxy)
            .add("httpsProxy=" + httpsProxy)
            .add("socksProxy=" + socksProxy)
            .toString();
    }
}