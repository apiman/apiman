package io.apiman.gateway.platforms.vertx3.engine.proxy;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

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

    public enum ProxyType {
        HTTP, SOCKS4, SOCKS5;

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
