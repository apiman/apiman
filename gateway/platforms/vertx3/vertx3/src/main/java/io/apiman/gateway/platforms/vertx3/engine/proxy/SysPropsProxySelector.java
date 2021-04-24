package io.apiman.gateway.platforms.vertx3.engine.proxy;

import java.net.URI;
import java.util.Optional;

public class SysPropsProxySelector {
    JavaSystemPropertiesProxySettings httpProxy = JavaSystemPropertiesProxySettings.createHttpProxy("http", 80);
    JavaSystemPropertiesProxySettings httpsProxy = JavaSystemPropertiesProxySettings.createHttpProxy("https", 443);
    JavaSystemPropertiesProxySettings socksProxy = JavaSystemPropertiesProxySettings.createSocksProxy(1080);

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
}