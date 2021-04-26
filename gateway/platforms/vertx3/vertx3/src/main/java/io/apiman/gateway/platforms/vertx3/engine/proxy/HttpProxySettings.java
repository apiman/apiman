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

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * HTTP proxy settings
 *
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public interface HttpProxySettings {

    /**
     * Get the proxy
     */
    HttpProxy getProxy();

    /**
     * Get the proxy if this settings accepts the specified host
     *
     * @param host the hostname to test
     * @return empty if host is not accepted, otherwise Optional contains {@link HttpProxy}
     */
    Optional<HttpProxy> getProxy(String host);

    /**
     * Proxy types
     */
    public enum ProxyType {
        HTTP, SOCKS4, SOCKS5;

        /**
         * Convert socks version string into corresponding ProxyType enum
         *
         * @param version the socks version
         * @return the corresponding SOCKS ProxyType enum
         */
        public static ProxyType toSocksVersion(String version) {
            // Default is SOCKS5 if version undefined or blank.
            if (version == null || StringUtils.isBlank(version)) {
                return SOCKS5;
            }
            String trimmed = version.trim();
            if ("4".equalsIgnoreCase(trimmed)) {
                return SOCKS4;
            } else if ("5".equalsIgnoreCase(trimmed)) {
                return SOCKS4;
            } else {
                throw new IllegalArgumentException("Unrecognised SOCKS version defined: " + version);
            }
        }
    }
}
