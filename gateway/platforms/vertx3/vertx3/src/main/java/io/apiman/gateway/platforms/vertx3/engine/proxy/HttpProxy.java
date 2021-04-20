package io.apiman.gateway.platforms.vertx3.engine.proxy;

import io.apiman.gateway.platforms.vertx3.engine.AbstractCredentials;
import io.apiman.gateway.platforms.vertx3.engine.UsernamePasswordCredentials;
import io.apiman.gateway.platforms.vertx3.engine.proxy.HttpProxySettings.ProxyType;

import java.util.Optional;
import java.util.StringJoiner;

/**
 * Default implementation, but we could have one for SOCKS for example.
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
            .add("host='" + host + "'")
            .add("port=" + port)
            .add("credentials=" + credentials)
            .toString();
    }
}
