package io.apiman.gateway.platforms.vertx3.engine.proxy;

import io.apiman.gateway.platforms.vertx3.engine.AbstractCredentials;
import io.apiman.gateway.platforms.vertx3.engine.UsernamePasswordCredentials;

import java.util.Optional;
import java.util.StringJoiner;

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
        HTTP, SOCKS4, SOCKS5
    }
}
