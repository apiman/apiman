package io.apiman.gateway.platforms.vertx3.engine.proxy;

import io.apiman.common.logging.ApimanLoggerFactory;
import io.apiman.common.logging.IApimanLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import inet.ipaddr.IPAddressString;
import io.netty.util.internal.SystemPropertyUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * Get proxy settings from Java System Properties
 * <p>
 * Based upon the behaviour of the in-built {@link sun.net.spi.DefaultProxySelector}, but supports prefixes
 * for IPv4 addresses. There doesn't seem to be any special treatment of IPv6 prefixes or ranges, but we could
 * easily add this if it's needed via the {@link IPAddressString#contains(IPAddressString)}. We would need
 * store an array of nonProxy hosts and IP addresses, rather than the simple regex approach.
 * <p>
 * See: https://bugs.openjdk.java.net/browse/JDK-8023648
 * <p>
 * Some of this was also inspired Gradle's system property proxy handling code.
 */
public class JavaSystemPropertiesProxySettings implements HttpProxySettings {
    private static final String DEFAULT_NON_PROXY_HOSTS = "localhost|127.*|[::1]|0.0.0.0|[::0]";
    private static final IApimanLogger LOGGER = ApimanLoggerFactory.getLogger(JavaSystemPropertiesProxySettings.class);
    private final List<String> nonProxyHosts = new ArrayList<>();
    private final HttpProxy proxy;
    private Pattern noProxyRegex;

    /**
     * Initialise SOCKS proxy from Java System Properties
     */
    public static JavaSystemPropertiesProxySettings createSocksProxy(int defaultPort) {
        return new JavaSystemPropertiesProxySettings(
            ProxyType.toSocksVersion(System.getProperty("socksProxyVersion")),
            "socksNonProxyHosts",
            System.getProperty("socksProxyHost"),
            SystemPropertyUtil.getInt("socksProxyPort", defaultPort),
            SystemPropertyUtil.get("java.net.socks.username"),
            SystemPropertyUtil.get("java.net.socks.password")
        );
    }

    /**
     * Initialise HTTP proxy settings from Java System Properties.
     *
     * @param propertyPrefix the property prefix (e.g. `foo.proxyhost`, `foo` is prefix).
     *                       Likely should be `http` or `https` unless you are doing something esoteric.
     * @param defaultPort the default proxy port (should one not be found in the system properties)
     */
    public static JavaSystemPropertiesProxySettings createHttpProxy(String propertyPrefix, int defaultPort) {
        return new JavaSystemPropertiesProxySettings(
            ProxyType.HTTP,
            "http.nonProxyHosts",
            System.getProperty(propertyPrefix + ".proxyHost"),
            SystemPropertyUtil.getInt(propertyPrefix + ".proxyPort", defaultPort),
            SystemPropertyUtil.get(propertyPrefix + ".proxyUser"),
            SystemPropertyUtil.get(propertyPrefix + ".proxyPassword")
        );
    }

    /**
     * Initialise proxy settings from from provided values
     *
     * @param host the hostname
     * @param port the port
     * @param proxyUser the proxy username
     * @param proxyPassword the proxy password
     */
    public JavaSystemPropertiesProxySettings(ProxyType proxyType, String nonProxyProperty, String host, int port, String proxyUser, String proxyPassword) {
        // If property prefix is null or host is null, then no useful proxy information has been provided, so we just assume empty.
        if (host == null) {
            this.proxy = null;
            return;
        }
        parseProxyHosts(SystemPropertyUtil.get(nonProxyProperty));
        this.proxy = new HttpProxy(host, port, proxyUser, proxyPassword);
        LOGGER.debug("Proxy configuration found in Java System Properties for {0}:\n"
                + "Proxy host: {1}\n"
                + "Proxy port: {2}\n"
                + "Proxy credentials: {3}\n"
                + "Non-proxy hosts: {4}",
            proxyType, host, port, proxy.getCredentials(), this.nonProxyHosts);
    }

    /**
     * Seems it is common to support wildcards at only the start or end of the entry, so we'll do that for
     * now. If anyone wants wildcard support elsewhere they can ask for it.
     *
     * We use Pattern.quote to make the rest of the string 'safe'.
     */
    private void parseProxyHosts(String userProvidedNonProxy) {
        // Glue together default non-proxy entries and user-provided.
        String noProxyList = String.join("|", DEFAULT_NON_PROXY_HOSTS, userProvidedNonProxy);
        StringJoiner pattern = new StringJoiner("|");
        String[] split = noProxyList.split("\\|");
        nonProxyHosts.addAll(Arrays.asList(split));
        for (String noProxyElement : split) {
            /*
             * To match with Java-style regex we need to add any/`.` to `*` (i.e. .*)
             */
            if (noProxyElement.startsWith("*") && noProxyElement.endsWith("*")) {
                String toQuote = noProxyElement.substring(1, noProxyElement.length() - 1);
                pattern.add(".*" + determineQuote(toQuote) + ".*");
            }
            else if (noProxyElement.endsWith("*")) {
                // Snip off * at end, replace with .*
                String toQuote = noProxyElement.substring(0, noProxyElement.length() - 1);
                pattern.add(determineQuote(toQuote) + ".*");
            } else if (noProxyElement.startsWith("*")) {
                // Snip off * at start, replace with .*
                String toQuote = noProxyElement.substring(1);
                pattern.add(".*" + determineQuote(toQuote));
            } else {
                pattern.add(determineQuote(noProxyElement));
            }
        }
        this.noProxyRegex = Pattern.compile(pattern.toString());
        LOGGER.debug("Pattern built for noProxy: {0}", noProxyRegex);
    }

    // Start or end star is already removed by this point.
    private String determineQuote(String candidate) {
        if (isIpAddress(candidate)) {
            /*
             * If the IP address has a mask (for example 1.2.3/8), then it seems the convention is to just
             * to chop off the /slash part and replace it with .*
             */
            IPAddressString ipString = new IPAddressString(candidate);
            if (ipString.isPrefixed()) {
                // Snip off the prefix segment replace it with .*
                String withoutPrefix = StringUtils.removeEnd(candidate, "/" + ipString.getNetworkPrefixLength());
                LOGGER.debug("Found prefixed IP address {0} -> {1}", ipString, withoutPrefix + ".*");
                return Pattern.quote(withoutPrefix) + ".*";

            }
        }
        return Pattern.quote(candidate);
    }

    // Start or end star already removed by this point.
    private boolean isIpAddress(String candidate) {
        try {
            return new IPAddressString(candidate).isIPAddress();
        } catch (RuntimeException rte) {
            LOGGER.trace("Ignored error when parsing IP", rte);
            return false;
        }
    }

    /**
     * Determine whether the given host should not be proxied.
     *
     * @param host hostname to test
     * @return true if is a `non proxy` host
     */
    public boolean isNonProxyHost(String host) {
        return proxy != null && noProxyRegex.matcher(host).matches();
    }

    @Override
    public HttpProxy getProxy() {
        return proxy;
    }

    @Override
    public Optional<HttpProxy> getProxy(String host) {
        return proxy == null || isNonProxyHost(host) ? Optional.empty() : Optional.of(proxy);
    }

    /**
     * Whether a proxy has been defined given the parameters it was constructed with.
     *
     * For example, if the provided system properties keys resolve null, then this will return {@code false}.
     *
     * @return true, if a proxy has been defined, otherwise false.
     */
    public boolean isProxyDefined() {
        return proxy != null;
    }
}
